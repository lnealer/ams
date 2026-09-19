CREATE OR REPLACE PACKAGE BODY AMS_NCR_SCHEDULING_PG AS

  resource_busy EXCEPTION;
  PRAGMA EXCEPTION_INIT(resource_busy, -30006);
  lock_nowait   EXCEPTION;
  PRAGMA EXCEPTION_INIT(lock_nowait, -54);

  -- Region used when a postcode maps to nothing in AMS_INSTALL_REGIONS.
  C_FALLBACK_REGION CONSTANT VARCHAR2(20 CHAR) := 'ALL';

  /*
   * Locks the request and books the change date.
   *
   * Shared by both scheduling procedures: the complex one runs this first and then layers the
   * circuit booking on top, so the two paths cannot drift apart on validation.
   *
   * Assumes the caller has already taken a SAVEPOINT.
   */
  PROCEDURE book_change_date(p_ncr_id       IN  NUMBER,
                             p_scheduled_dt IN  TIMESTAMP,
                             p_user_id      IN  VARCHAR2,
                             p_customer_id  OUT NUMBER,
                             p_asset_id     OUT NUMBER,
                             p_status_cd    OUT VARCHAR2) IS
    l_status    AMS_NETWORK_CHANGE_REQUESTS.NCR_STATUS_CD%TYPE;
    l_blackouts NUMBER;
  BEGIN
    p_status_cd   := C_ERROR;
    p_customer_id := NULL;
    p_asset_id    := NULL;

    IF NVL(p_ncr_id, 0) = 0 OR p_scheduled_dt IS NULL THEN
      p_status_cd := C_INVALID_INPUT;
      RETURN;
    END IF;

    -- The request is locked before any timeslot row, always. That fixed order is what stops
    -- RescheduleNcrAction's cancel-then-reserve deadlocking against a concurrent booking.
    BEGIN
      SELECT NCR_STATUS_CD, CUSTOMER_ID, ASSET_ID
        INTO l_status, p_customer_id, p_asset_id
        FROM AMS_NETWORK_CHANGE_REQUESTS
       WHERE NCR_ID = p_ncr_id
         FOR UPDATE WAIT 5;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN p_status_cd := C_NOT_FOUND; RETURN;
      WHEN resource_busy OR lock_nowait THEN p_status_cd := C_BUSY; RETURN;
    END;

    IF l_status IN ('COMPLETED', 'CANCELLED') THEN
      p_status_cd := C_NOT_OPEN;
      RETURN;
    END IF;
    IF l_status = 'INPROG' THEN
      p_status_cd := C_IN_PROGRESS;
      RETURN;
    END IF;

    IF TRUNC(CAST(p_scheduled_dt AS DATE)) < TRUNC(SYSDATE) THEN
      p_status_cd := C_DATE_IN_PAST;
      RETURN;
    END IF;

    SELECT COUNT(*) INTO l_blackouts
      FROM AMS_CUSTOMER_SCHEDULES
     WHERE CUSTOMER_ID = p_customer_id
       AND TRUNC(BLACKOUT_DT) = TRUNC(CAST(p_scheduled_dt AS DATE));

    IF l_blackouts > 0 THEN
      p_status_cd := C_CUSTOMER_BLACKOUT;
      RETURN;
    END IF;

    UPDATE AMS_NETWORK_CHANGE_REQUESTS
       SET SCHEDULED_DT = p_scheduled_dt,
           NCR_STATUS_CD = 'SCHEDULED',
           MODIFIED_DT = SYSTIMESTAMP,
           MODIFIED_BY = p_user_id
     WHERE NCR_ID = p_ncr_id;

    p_status_cd := C_OK;
  END book_change_date;

  /* Resolves the engineering region through the address chain that already exists. */
  FUNCTION resolve_region(p_asset_id IN NUMBER) RETURN VARCHAR2 IS
    l_region AMS_INSTALL_REGIONS.REGION_CD%TYPE;
  BEGIN
    SELECT R.REGION_CD INTO l_region
      FROM AMS_ASSETS A
      JOIN AMS_ADDRESSES D ON D.ADDRESS_ID = A.INSTALL_ADDRESS_ID
      JOIN AMS_INSTALL_REGIONS R ON R.ZIP_CODE = D.ZIP_CODE
     WHERE A.ASSET_ID = p_asset_id;
    RETURN l_region;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN C_FALLBACK_REGION;
    WHEN TOO_MANY_ROWS THEN RETURN C_FALLBACK_REGION;
  END resolve_region;

  /* Releases every move-timeslot reservation the request holds. Returns how many. */
  FUNCTION release_move_timeslots(p_ncr_id  IN NUMBER,
                                  p_reason  IN VARCHAR2,
                                  p_user_id IN VARCHAR2) RETURN NUMBER IS
    l_count NUMBER := 0;
  BEGIN
    -- Ascending timeslot id, so concurrent callers touch rows in the same order.
    FOR r IN (SELECT RESERVATION_ID, TIMESLOT_ID
                FROM AMS_TIMESLOT_RESERVATIONS
               WHERE ENTITY_TYPE_CD = 'NCR'
                 AND ENTITY_ID = p_ncr_id
                 AND RESERVATION_STATUS_CD = 'HELD'
               ORDER BY TIMESLOT_ID) LOOP

      UPDATE AMS_TIMESLOTS
         SET RESERVED_COUNT = GREATEST(NVL(RESERVED_COUNT, 0) - 1, 0)
       WHERE TIMESLOT_ID = r.TIMESLOT_ID;

      UPDATE AMS_TIMESLOT_RESERVATIONS
         SET RESERVATION_STATUS_CD = 'RELEASED',
             RELEASE_REASON = SUBSTR(p_reason, 1, 400),
             RELEASED_DT = SYSTIMESTAMP,
             MODIFIED_DT = SYSTIMESTAMP,
             MODIFIED_BY = p_user_id
       WHERE RESERVATION_ID = r.RESERVATION_ID;

      l_count := l_count + 1;
    END LOOP;
    RETURN l_count;
  END release_move_timeslots;

  -- ------------------------------------------------------------------
  -- schedule_ncr
  -- ------------------------------------------------------------------
  PROCEDURE schedule_ncr(p_ncr_id       IN  NUMBER,
                         p_scheduled_dt IN  TIMESTAMP,
                         p_user_id      IN  VARCHAR2,
                         p_status_cd    OUT VARCHAR2) IS
    l_customer_id NUMBER;
    l_asset_id    NUMBER;
  BEGIN
    p_status_cd := C_ERROR;
    SAVEPOINT ams_sched_ncr;

    book_change_date(p_ncr_id, p_scheduled_dt, p_user_id,
                     l_customer_id, l_asset_id, p_status_cd);

    IF p_status_cd <> C_OK THEN
      ROLLBACK TO ams_sched_ncr;
    END IF;
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK TO ams_sched_ncr;
      RAISE;
  END schedule_ncr;

  -- ------------------------------------------------------------------
  -- schedule_site_type_ncr
  -- ------------------------------------------------------------------
  PROCEDURE schedule_site_type_ncr(p_ncr_id            IN  NUMBER,
                                   p_asset_id          IN  NUMBER,
                                   p_site_type_cd      IN  VARCHAR2,
                                   p_scheduled_dt      IN  TIMESTAMP,
                                   p_user_id           IN  VARCHAR2,
                                   p_status_cd         OUT VARCHAR2,
                                   p_circuit_window_id OUT NUMBER) IS
    l_customer_id  NUMBER;
    l_ncr_asset_id NUMBER;
    l_asset_id     NUMBER;
    l_site_type    VARCHAR2(20 CHAR) := UPPER(TRIM(p_site_type_cd));
    l_region       VARCHAR2(20 CHAR);
    l_circuit_id   AMS_ASSET_CONFIGS.CIRCUIT_ID%TYPE;
    l_timeslot_id  AMS_TIMESLOTS.TIMESLOT_ID%TYPE;
    l_capacity     AMS_TIMESLOTS.CAPACITY%TYPE;
    l_reserved     AMS_TIMESLOTS.RESERVED_COUNT%TYPE;
    l_available    AMS_TIMESLOTS.AVAILABLE_FL%TYPE;
    l_existing     NUMBER;
    l_window_id    NUMBER;
  BEGIN
    p_status_cd         := C_ERROR;
    p_circuit_window_id := NULL;

    SAVEPOINT ams_sched_site;

    book_change_date(p_ncr_id, p_scheduled_dt, p_user_id,
                     l_customer_id, l_ncr_asset_id, p_status_cd);
    IF p_status_cd <> C_OK THEN
      ROLLBACK TO ams_sched_site;
      RETURN;
    END IF;

    -- CalendarServiceImpl passes 0 where the Java value was null.
    l_asset_id := CASE WHEN NVL(p_asset_id, 0) = 0 THEN l_ncr_asset_id ELSE p_asset_id END;
    IF l_asset_id IS NULL THEN
      ROLLBACK TO ams_sched_site;
      p_status_cd := C_NO_ASSET;
      RETURN;
    END IF;

    -- Only validated when supplied. A null site type is a legitimate booking: the request may not
    -- have a proposed configuration yet, and CalendarServiceImplTest exercises exactly that.
    IF l_site_type IS NOT NULL
       AND l_site_type NOT IN ('LANA', 'LANB', 'LANC', 'WAN', 'DUALWAN', 'HAPAIR') THEN
      ROLLBACK TO ams_sched_site;
      p_status_cd := C_INVALID_SITE_TYPE;
      RETURN;
    END IF;

    BEGIN
      SELECT CIRCUIT_ID INTO l_circuit_id
        FROM (SELECT CIRCUIT_ID FROM AMS_ASSET_CONFIGS
               WHERE ASSET_ID = l_asset_id ORDER BY REVISION_NUM DESC)
       WHERE ROWNUM <= 1;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN l_circuit_id := NULL;   -- not fatal; the window is still bookable
    END;

    l_region := resolve_region(l_asset_id);

    -- Circuit capacity lives on the CIRCUIT calendar in AMS_TIMESLOTS.
    BEGIN
      SELECT TIMESLOT_ID, CAPACITY, RESERVED_COUNT, AVAILABLE_FL
        INTO l_timeslot_id, l_capacity, l_reserved, l_available
        FROM AMS_TIMESLOTS
       WHERE CALL_TYPE_CD = 'CIRCUIT'
         AND REGION_CD = l_region
         AND TRUNC(START_TM) = TRUNC(CAST(p_scheduled_dt AS DATE))
         FOR UPDATE WAIT 5;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        ROLLBACK TO ams_sched_site;
        p_status_cd := C_NO_CIRCUIT_WINDOW;
        RETURN;
      WHEN TOO_MANY_ROWS THEN
        ROLLBACK TO ams_sched_site;
        p_status_cd := C_ERROR;
        RETURN;
      WHEN resource_busy OR lock_nowait THEN
        ROLLBACK TO ams_sched_site;
        p_status_cd := C_BUSY;
        RETURN;
    END;

    IF NVL(l_available, 'N') <> 'Y' THEN
      ROLLBACK TO ams_sched_site;
      p_status_cd := C_CIRCUIT_CLOSED;
      RETURN;
    END IF;

    -- Already booked for this request on this day? Reuse it rather than taking a second place.
    BEGIN
      SELECT CIRCUIT_WINDOW_ID INTO l_window_id
        FROM AMS_CIRCUIT_WINDOWS
       WHERE NCR_ID = p_ncr_id
         AND TIMESLOT_ID = l_timeslot_id
         AND WINDOW_STATUS_CD = 'RESERVED'
         AND ROWNUM <= 1;
      p_circuit_window_id := l_window_id;
      p_status_cd := C_OK;
      RETURN;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN NULL;
    END;

    IF NVL(l_reserved, 0) >= NVL(l_capacity, 0) THEN
      -- The rollback is the point: the change date written by book_change_date is undone, so the
      -- request is left exactly as it was rather than scheduled with no circuit behind it.
      ROLLBACK TO ams_sched_site;
      p_status_cd := C_NO_CAPACITY;
      RETURN;
    END IF;

    UPDATE AMS_TIMESLOTS
       SET RESERVED_COUNT = NVL(RESERVED_COUNT, 0) + 1
     WHERE TIMESLOT_ID = l_timeslot_id;

    l_window_id := AMS_CIRCUIT_WINDOWS_SQ.NEXTVAL;
    INSERT INTO AMS_CIRCUIT_WINDOWS
           (CIRCUIT_WINDOW_ID, NCR_ID, ASSET_ID, TIMESLOT_ID, CIRCUIT_ID, SITE_TYPE_CD,
            WINDOW_DT, WINDOW_STATUS_CD, CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY)
    VALUES (l_window_id, p_ncr_id, l_asset_id, l_timeslot_id, l_circuit_id, l_site_type,
            p_scheduled_dt, 'RESERVED', SYSTIMESTAMP, p_user_id, SYSTIMESTAMP, p_user_id);

    p_circuit_window_id := l_window_id;
    p_status_cd := C_OK;
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK TO ams_sched_site;
      p_circuit_window_id := NULL;
      RAISE;
  END schedule_site_type_ncr;

  -- ------------------------------------------------------------------
  -- cancel_ncr_date
  -- ------------------------------------------------------------------
  PROCEDURE cancel_ncr_date(p_ncr_id    IN  NUMBER,
                            p_reason    IN  VARCHAR2,
                            p_user_id   IN  VARCHAR2,
                            p_status_cd OUT VARCHAR2) IS
    l_status   AMS_NETWORK_CHANGE_REQUESTS.NCR_STATUS_CD%TYPE;
    l_sched    AMS_NETWORK_CHANGE_REQUESTS.SCHEDULED_DT%TYPE;
    l_released NUMBER;
  BEGIN
    p_status_cd := C_ERROR;

    IF NVL(p_ncr_id, 0) = 0 THEN
      p_status_cd := C_INVALID_INPUT;
      RETURN;
    END IF;

    SAVEPOINT ams_cancel_ncr;

    BEGIN
      SELECT NCR_STATUS_CD, SCHEDULED_DT INTO l_status, l_sched
        FROM AMS_NETWORK_CHANGE_REQUESTS
       WHERE NCR_ID = p_ncr_id
         FOR UPDATE WAIT 5;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN p_status_cd := C_NOT_FOUND; RETURN;
      WHEN resource_busy OR lock_nowait THEN p_status_cd := C_BUSY; RETURN;
    END;

    IF l_status IN ('COMPLETED', 'CANCELLED') THEN
      p_status_cd := C_NOT_OPEN;
      RETURN;
    END IF;
    IF l_status = 'INPROG' THEN
      p_status_cd := C_IN_PROGRESS;
      RETURN;
    END IF;

    l_released := release_move_timeslots(p_ncr_id, p_reason, p_user_id);

    IF l_sched IS NULL AND l_released = 0 THEN
      -- Nothing was holding anything. Not an error: RescheduleNcrAction cancels unconditionally
      -- before re-reserving, so this is the ordinary case there.
      p_status_cd := C_NOT_SCHEDULED;
      RETURN;
    END IF;

    -- Back to SUBMITTED, never CANCELLED. The Java owns cancelling the request itself.
    UPDATE AMS_NETWORK_CHANGE_REQUESTS
       SET SCHEDULED_DT = NULL,
           NCR_STATUS_CD = CASE WHEN NCR_STATUS_CD = 'SCHEDULED' THEN 'SUBMITTED'
                                ELSE NCR_STATUS_CD END,
           MODIFIED_DT = SYSTIMESTAMP,
           MODIFIED_BY = p_user_id
     WHERE NCR_ID = p_ncr_id;

    p_status_cd := C_OK;
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK TO ams_cancel_ncr;
      RAISE;
  END cancel_ncr_date;

  -- ------------------------------------------------------------------
  -- cancel_site_type_ncr
  -- ------------------------------------------------------------------
  PROCEDURE cancel_site_type_ncr(p_ncr_id         IN  NUMBER,
                                 p_asset_id       IN  NUMBER,
                                 p_reason         IN  VARCHAR2,
                                 p_user_id        IN  VARCHAR2,
                                 p_status_cd      OUT VARCHAR2,
                                 p_released_count OUT NUMBER) IS
    l_status   AMS_NETWORK_CHANGE_REQUESTS.NCR_STATUS_CD%TYPE;
    l_sched    AMS_NETWORK_CHANGE_REQUESTS.SCHEDULED_DT%TYPE;
    l_moves    NUMBER;
  BEGIN
    p_status_cd      := C_ERROR;
    p_released_count := 0;

    IF NVL(p_ncr_id, 0) = 0 THEN
      p_status_cd := C_INVALID_INPUT;
      RETURN;
    END IF;

    SAVEPOINT ams_cancel_site;

    BEGIN
      SELECT NCR_STATUS_CD, SCHEDULED_DT INTO l_status, l_sched
        FROM AMS_NETWORK_CHANGE_REQUESTS
       WHERE NCR_ID = p_ncr_id
         FOR UPDATE WAIT 5;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN p_status_cd := C_NOT_FOUND; RETURN;
      WHEN resource_busy OR lock_nowait THEN p_status_cd := C_BUSY; RETURN;
    END;

    IF l_status IN ('COMPLETED', 'CANCELLED') THEN
      p_status_cd := C_NOT_OPEN;
      RETURN;
    END IF;
    IF l_status = 'INPROG' THEN
      p_status_cd := C_IN_PROGRESS;
      RETURN;
    END IF;

    l_moves := release_move_timeslots(p_ncr_id, p_reason, p_user_id);

    -- Release every circuit window, ascending by timeslot to keep the lock order stable.
    FOR w IN (SELECT CIRCUIT_WINDOW_ID, TIMESLOT_ID
                FROM AMS_CIRCUIT_WINDOWS
               WHERE NCR_ID = p_ncr_id
                 AND WINDOW_STATUS_CD = 'RESERVED'
               ORDER BY TIMESLOT_ID) LOOP

      UPDATE AMS_TIMESLOTS
         SET RESERVED_COUNT = GREATEST(NVL(RESERVED_COUNT, 0) - 1, 0)
       WHERE TIMESLOT_ID = w.TIMESLOT_ID;

      UPDATE AMS_CIRCUIT_WINDOWS
         SET WINDOW_STATUS_CD = 'RELEASED',
             RELEASE_REASON = SUBSTR(p_reason, 1, 400),
             RELEASED_DT = SYSTIMESTAMP,
             MODIFIED_DT = SYSTIMESTAMP,
             MODIFIED_BY = p_user_id
       WHERE CIRCUIT_WINDOW_ID = w.CIRCUIT_WINDOW_ID;

      p_released_count := p_released_count + 1;
    END LOOP;

    IF l_sched IS NULL AND l_moves = 0 AND p_released_count = 0 THEN
      p_status_cd := C_NOT_SCHEDULED;
      RETURN;
    END IF;

    UPDATE AMS_NETWORK_CHANGE_REQUESTS
       SET SCHEDULED_DT = NULL,
           NCR_STATUS_CD = CASE WHEN NCR_STATUS_CD = 'SCHEDULED' THEN 'SUBMITTED'
                                ELSE NCR_STATUS_CD END,
           MODIFIED_DT = SYSTIMESTAMP,
           MODIFIED_BY = p_user_id
     WHERE NCR_ID = p_ncr_id;

    p_status_cd := C_OK;
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK TO ams_cancel_site;
      p_released_count := 0;
      RAISE;
  END cancel_site_type_ncr;

END AMS_NCR_SCHEDULING_PG;
/
