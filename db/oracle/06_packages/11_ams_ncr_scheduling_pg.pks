CREATE OR REPLACE PACKAGE AMS_NCR_SCHEDULING_PG AS
  /*
   * Network change request scheduling.
   *
   * Two paths, and the split is the whole point of this package. A device-only change books one
   * date. A site type change reshapes the circuit as well, so it must additionally take capacity on
   * the carrier's change calendar - and both bookings have to succeed or neither may stand. That is
   * why schedule_site_type_ncr is a separate procedure rather than a flag on schedule_ncr, and it
   * is what NetworkChangeRequest.isComplexScheduling() selects between on the Java side.
   *
   * The same transaction and status rules as AMS_SCHEDULING_PG apply: no COMMIT, a SAVEPOINT per
   * procedure, OUT parameters assigned before anything can fail, and 'OK' as the only success.
   *
   * IMPORTANT - these procedures release the DATE, never the REQUEST.
   * NetworkChangeRequestServiceImpl.cancel() calls cancel_ncr_date and then separately calls
   * networkChangeRequestDAO.cancelRequest() to set CANCELLED. If the procedure also cancelled the
   * request, RescheduleNcrAction - which cancels then re-reserves in one transaction - would
   * destroy live requests every time somebody moved a date.
   */

  C_OK                   CONSTANT VARCHAR2(30) := 'OK';
  C_INVALID_INPUT        CONSTANT VARCHAR2(30) := 'INVALID_INPUT';
  C_NOT_FOUND            CONSTANT VARCHAR2(30) := 'NOT_FOUND';
  C_BUSY                 CONSTANT VARCHAR2(30) := 'BUSY';
  C_NOT_OPEN             CONSTANT VARCHAR2(30) := 'NOT_OPEN';
  C_IN_PROGRESS          CONSTANT VARCHAR2(30) := 'IN_PROGRESS';
  C_NOT_SCHEDULED        CONSTANT VARCHAR2(30) := 'NOT_SCHEDULED';
  C_DATE_IN_PAST         CONSTANT VARCHAR2(30) := 'DATE_IN_PAST';
  -- The customer asked not to be touched that day (AMS_CUSTOMER_SCHEDULES).
  C_CUSTOMER_BLACKOUT    CONSTANT VARCHAR2(30) := 'CUSTOMER_BLACKOUT';
  C_NO_ASSET             CONSTANT VARCHAR2(30) := 'NO_ASSET';
  C_INVALID_SITE_TYPE    CONSTANT VARCHAR2(30) := 'INVALID_SITE_TYPE';
  -- The carrier runs no circuit changes in that region that night.
  C_NO_CIRCUIT_WINDOW    CONSTANT VARCHAR2(30) := 'NO_CIRCUIT_WINDOW';
  C_CIRCUIT_CLOSED       CONSTANT VARCHAR2(30) := 'CIRCUIT_WINDOW_CLOSED';
  C_NO_CAPACITY          CONSTANT VARCHAR2(30) := 'NO_CAPACITY';
  C_ERROR                CONSTANT VARCHAR2(30) := 'ERROR';

  /*
   * Books the change window for a request that only touches the managed device.
   *
   * Weekends and public holidays are deliberately NOT rejected. Those calendars govern
   * facilitation calls, where an engineer has to travel - which is what
   * CalendarServiceImpl.isSchedulableDate filters. Carrier change work routinely lands overnight
   * or at a weekend, and that is the normal case rather than the exception.
   */
  PROCEDURE schedule_ncr(p_ncr_id       IN  NUMBER,
                         p_scheduled_dt IN  TIMESTAMP,
                         p_user_id      IN  VARCHAR2,
                         p_status_cd    OUT VARCHAR2);

  /*
   * Books the device window and the circuit window together, for a site type change.
   *
   * If circuit capacity cannot be taken, the change date written moments earlier is rolled back to
   * the savepoint, so a failed booking leaves the request exactly as it was rather than
   * half-scheduled.
   *
   * p_site_type_cd may be null: CalendarServiceImpl.getSiteTypeCode() returns null when the
   * request has no proposed configuration yet, and that is a valid booking, not an error.
   *
   * p_circuit_window_id returns the row created in AMS_CIRCUIT_WINDOWS, or null on any non-OK
   * status.
   */
  PROCEDURE schedule_site_type_ncr(p_ncr_id            IN  NUMBER,
                                   p_asset_id          IN  NUMBER,
                                   p_site_type_cd      IN  VARCHAR2,
                                   p_scheduled_dt      IN  TIMESTAMP,
                                   p_user_id           IN  VARCHAR2,
                                   p_status_cd         OUT VARCHAR2,
                                   p_circuit_window_id OUT NUMBER);

  /*
   * Releases the change date and any move-timeslot capacity the request held.
   *
   * Returns OK when there was nothing to release but the request is open - RescheduleNcrAction
   * cancels unconditionally before re-reserving, so "not currently scheduled" is the normal case
   * there, not a failure.
   */
  PROCEDURE cancel_ncr_date(p_ncr_id    IN  NUMBER,
                            p_reason    IN  VARCHAR2,
                            p_user_id   IN  VARCHAR2,
                            p_status_cd OUT VARCHAR2);

  /*
   * As cancel_ncr_date, and additionally releases every circuit window the request holds.
   *
   * p_released_count is how many circuit windows were given back - more than one when the asset
   * carries several circuits.
   */
  PROCEDURE cancel_site_type_ncr(p_ncr_id         IN  NUMBER,
                                 p_asset_id       IN  NUMBER,
                                 p_reason         IN  VARCHAR2,
                                 p_user_id        IN  VARCHAR2,
                                 p_status_cd      OUT VARCHAR2,
                                 p_released_count OUT NUMBER);

END AMS_NCR_SCHEDULING_PG;
/
