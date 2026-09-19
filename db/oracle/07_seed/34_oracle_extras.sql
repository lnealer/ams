-- Seed rows that exist only in Oracle, because they support the PL/SQL rather than the Java.
-- The H2 test fixtures have no equivalent: nothing in the test suite reaches the procedures.

-- De-duplication window for AMS_EMAIL_PG.add_entity_email. Absent, it falls back to 60 minutes.
INSERT INTO AMS_PROPERTIES (PROPERTY_KEY, PROPERTY_VALUE, DESCRIPTION)
VALUES ('EMAILDEDUPMIN', '60', 'Minutes within which a repeated notification is suppressed');

/*
 * The carrier's circuit change calendar.
 *
 * AMS_SCHEDULING_PG and the site-type NCR path both take capacity from AMS_TIMESLOTS; these rows
 * are the CIRCUIT calendar, distinguished by CALL_TYPE_CD. They cannot appear in any user-facing
 * picker because FacilitationCallType has no CIRCUIT constant, so no Java query asks for them.
 *
 * One window per region per night for the next 90 days. Capacity 2 reflects how few circuit
 * changes a carrier will accept in one region on one night - it is deliberately small, so the
 * NO_CAPACITY path is reachable in practice rather than only in theory.
 *
 * Region 'ALL' is the fallback AMS_NCR_SCHEDULING_PG.resolve_region returns when an asset has no
 * installation address, which is the case for most of the seeded assets.
 */
DECLARE
  TYPE region_list IS TABLE OF VARCHAR2(20 CHAR);
  l_regions region_list := region_list('ALL', 'CENTRAL', 'NORTHEAST');
  l_id      NUMBER;
BEGIN
  FOR r IN 1 .. l_regions.COUNT LOOP
    FOR d IN 0 .. 89 LOOP
      l_id := AMS_TIMESLOTS_SQ.NEXTVAL;
      INSERT INTO AMS_TIMESLOTS
             (TIMESLOT_ID, CALL_TYPE_CD, REGION_CD, START_TM, END_TM,
              CAPACITY, RESERVED_COUNT, AVAILABLE_FL, DISPLAY_LABEL, TIME_ZONE)
      VALUES (l_id, 'CIRCUIT', l_regions(r),
              CAST(TRUNC(SYSDATE) + d + 22/24 AS TIMESTAMP),
              CAST(TRUNC(SYSDATE) + d + 26/24 AS TIMESTAMP),
              2, 0, 'Y', '10:00 PM - 2:00 AM', 'America/New_York');
    END LOOP;
  END LOOP;
END;
/
