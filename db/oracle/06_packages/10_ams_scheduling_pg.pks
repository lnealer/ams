CREATE OR REPLACE PACKAGE AMS_SCHEDULING_PG AS
  /*
   * Calendar reservations and decommission dates.
   *
   * CONTRACT WITH THE CALLING JAVA
   * ------------------------------
   * Spring's StoredProcedure builds {call name(?,?,...)} with one placeholder per declared
   * parameter, bound POSITIONALLY. RdbmsOperation.compile() never reads database metadata, so a
   * parameter added, removed or reordered here is invisible until the first user click, where it
   * surfaces as ORA-06550. The formal parameter lists below must stay exactly in step with the
   * StoredProcedure subclasses in:
   *   ams-common/.../shared/dao/procs/
   *   ams-internal/.../internal/service/dao/procs/
   * 08_validate.sql smoke-calls every entry point precisely because nothing else checks this.
   *
   * TRANSACTIONS
   * ------------
   * No procedure in this package commits, rolls back, or runs autonomously. The caller is inside a
   * Spring @Transactional on DataSourceTransactionManager and shares this physical connection, so
   * a COMMIT here would silently commit the caller's work and destroy its ability to roll back.
   * RescheduleNcrAction depends on cancel-then-reserve being one abandonable unit.
   *
   * Each procedure takes a SAVEPOINT and rolls back to it on any business failure, so a status
   * other than OK reliably means "nothing was changed".
   *
   * A consequence worth knowing: because these do not commit, row locks taken here are held until
   * the caller's transaction ends, not until the procedure returns. Reservation calls should be
   * the last database work in their transaction. InstallationAction and TechLineAction already are.
   *
   * STATUS CODES
   * ------------
   * 'OK' and nothing else means success. Everything else is an ordinary business outcome the UI
   * reports to the user - not an error - so these procedures do not raise for expected failures.
   */

  -- Success.
  C_OK              CONSTANT VARCHAR2(30) := 'OK';
  -- The slot filled up before this caller got to it. The common, expected race.
  C_NO_CAPACITY     CONSTANT VARCHAR2(30) := 'NO_CAPACITY';
  -- Another session held the row longer than the lock wait allows.
  C_BUSY            CONSTANT VARCHAR2(30) := 'BUSY';
  C_NOT_FOUND       CONSTANT VARCHAR2(30) := 'NOT_FOUND';
  C_INVALID_INPUT   CONSTANT VARCHAR2(30) := 'INVALID_INPUT';
  -- Ops has closed the slot; distinct from it being full.
  C_SLOT_CLOSED     CONSTANT VARCHAR2(30) := 'SLOT_CLOSED';
  -- Cancelling a reservation the caller does not hold. Makes a double-cancel harmless.
  C_NOT_RESERVED    CONSTANT VARCHAR2(30) := 'NOT_RESERVED';
  C_NOT_OPEN        CONSTANT VARCHAR2(30) := 'NOT_OPEN';
  C_IN_PROGRESS     CONSTANT VARCHAR2(30) := 'IN_PROGRESS';
  C_NOT_SCHEDULED   CONSTANT VARCHAR2(30) := 'NOT_SCHEDULED';
  C_ASSET_MISMATCH  CONSTANT VARCHAR2(30) := 'ASSET_MISMATCH';
  C_DATE_IN_PAST    CONSTANT VARCHAR2(30) := 'DATE_IN_PAST';
  C_OUTSIDE_WINDOW  CONSTANT VARCHAR2(30) := 'OUTSIDE_WINDOW';
  C_NO_INSTALLATION CONSTANT VARCHAR2(30) := 'NO_INSTALLATION';
  C_ERROR           CONSTANT VARCHAR2(30) := 'ERROR';

  /*
   * Takes one place on a calendar slot.
   *
   * The capacity test and the decrement happen under a row lock, so two operators booking the last
   * place cannot both succeed - the loser gets NO_CAPACITY rather than an exception.
   *
   * Idempotent: reserving a slot this entity already holds returns OK without taking a second
   * place, so a double-clicked form neither errors nor double-books.
   *
   * p_entity_type_cd is INSTALL, TECHLINE or NCR. For INSTALL, p_entity_id is the ORDER id -
   * InstallationAction passes orderId.
   */
  PROCEDURE reserve_timeslot(p_timeslot_id    IN  NUMBER,
                             p_entity_id      IN  NUMBER,
                             p_entity_type_cd IN  VARCHAR2,
                             p_scheduled_dt   IN  TIMESTAMP,
                             p_user_id        IN  VARCHAR2,
                             p_status_cd      OUT VARCHAR2,
                             p_message        OUT VARCHAR2);

  /* Gives a place back. Cancelling one that is not held returns NOT_RESERVED and changes nothing. */
  PROCEDURE cancel_timeslot(p_timeslot_id    IN  NUMBER,
                            p_entity_id      IN  NUMBER,
                            p_entity_type_cd IN  VARCHAR2,
                            p_user_id        IN  VARCHAR2,
                            p_status_cd      OUT VARCHAR2);

  /*
   * Books the date an asset is taken out of service.
   *
   * p_decommission_id may be 0: CalendarServiceImpl passes 0 where the Java value was null, in
   * which case the open decommission for the asset is used, or one is created.
   *
   * The scheduling window (AMS_PROPERTIES MAXDECOMDAYS, default 42 days) is re-checked here even
   * though CalendarServiceImpl already enforces it, so a batch caller cannot bypass it.
   */
  PROCEDURE schedule_decommission(p_decommission_id    IN  NUMBER,
                                  p_asset_id           IN  NUMBER,
                                  p_scheduled_dt       IN  TIMESTAMP,
                                  p_hardware_return_fl IN  VARCHAR2,
                                  p_user_id            IN  VARCHAR2,
                                  p_status_cd          OUT VARCHAR2);

  /*
   * Releases a booked decommission date.
   *
   * Releases the DATE, not the request: the decommission goes back to REQUESTED, never to
   * CANCELLED. Cancelling the request itself is a separate concern owned by the Java.
   */
  PROCEDURE cancel_decommission(p_decommission_id IN  NUMBER,
                                p_reason          IN  VARCHAR2,
                                p_user_id         IN  VARCHAR2,
                                p_status_cd       OUT VARCHAR2);

END AMS_SCHEDULING_PG;
/
