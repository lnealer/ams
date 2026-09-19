-- Installation and tech-line capacity, generated relative to SYSDATE.
--
-- 14_AMS_TIMESLOTS.sql seeds four slots on fixed September 2025 dates. Those are deliberate: the
-- DAO tests query a fixed range and would be flaky against moving dates. But a fixed date is no
-- use to anyone driving the portal, because both calendar screens only offer slots in the future -
-- the day after seeding, "Schedule installation" is an empty list and the flow dead-ends.
--
-- So this generates rolling capacity the same way the despatch windows do, for every region that
-- has a ZIP mapped to it. Without it an order can be placed but never scheduled, which makes the
-- second half of the lifecycle unverifiable.
--
-- Re-runnable: it clears only future slots that nobody holds. A slot with a reservation against it
-- is left alone, so re-running never strands a booked installation.
DELETE FROM AMS_TIMESLOTS
 WHERE CALL_TYPE_CD IN ('INSTALL', 'TECHLINE')
   AND START_TM > SYSTIMESTAMP
   AND TIMESLOT_ID NOT IN (SELECT TIMESLOT_ID FROM AMS_TIMESLOT_RESERVATIONS);

DECLARE
  l_day   DATE;
  l_start TIMESTAMP;

  PROCEDURE add_slot(p_type     IN VARCHAR2,
                     p_region   IN VARCHAR2,
                     p_start    IN TIMESTAMP,
                     p_hours    IN NUMBER,
                     p_capacity IN NUMBER,
                     p_label    IN VARCHAR2) IS
  BEGIN
    INSERT INTO AMS_TIMESLOTS
           (TIMESLOT_ID, CALL_TYPE_CD, REGION_CD, START_TM, END_TM,
            CAPACITY, RESERVED_COUNT, AVAILABLE_FL, DISPLAY_LABEL, TIME_ZONE)
    VALUES (AMS_TIMESLOTS_SQ.NEXTVAL, p_type, p_region,
            p_start, p_start + NUMTODSINTERVAL(p_hours, 'HOUR'),
            p_capacity, 0, 'Y', p_label, 'America/New_York');
  END add_slot;
BEGIN
  FOR r IN (SELECT DISTINCT REGION_CD FROM AMS_INSTALL_REGIONS ORDER BY REGION_CD) LOOP
    FOR offset_days IN 3 .. 24 LOOP
      l_day := TRUNC(SYSDATE) + offset_days;
      -- No engineer visits and no tech line at the weekend.
      CONTINUE WHEN TO_CHAR(l_day, 'DY', 'NLS_DATE_LANGUAGE=AMERICAN') IN ('SAT', 'SUN');

      -- Installations run in half-day visits; the afternoon slot is smaller because the second
      -- visit of the day is the one that overruns.
      l_start := CAST(l_day AS TIMESTAMP) + INTERVAL '8' HOUR;
      add_slot('INSTALL', r.REGION_CD, l_start, 4, 3, 'Morning visit (8:00 AM - 12:00 PM)');

      l_start := CAST(l_day AS TIMESTAMP) + INTERVAL '13' HOUR;
      add_slot('INSTALL', r.REGION_CD, l_start, 4, 2, 'Afternoon visit (1:00 PM - 5:00 PM)');

      -- Tech line is a phone appointment, so it takes two hours and more of them fit.
      l_start := CAST(l_day AS TIMESTAMP) + INTERVAL '9' HOUR;
      add_slot('TECHLINE', r.REGION_CD, l_start, 2, 4, '9:00 AM - 11:00 AM');

      l_start := CAST(l_day AS TIMESTAMP) + INTERVAL '14' HOUR;
      add_slot('TECHLINE', r.REGION_CD, l_start, 2, 4, '2:00 PM - 4:00 PM');
    END LOOP;
  END LOOP;
END;
/
