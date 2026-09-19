-- Despatch windows for the ordering flow's last step.
--
-- Generated relative to SYSDATE rather than written out as literal dates. The installation and
-- techline slots above are fixed dates, which is fine for the DAO tests that query a fixed range,
-- but a shipping window has to be in the *future* to be offered to a user - a fixed date makes the
-- screen empty the day after it is seeded.
--
-- Carried on AMS_TIMESLOTS with CALL_TYPE_CD = 'SHIP' rather than in a table of its own: a window
-- is warehouse capacity per region per day, which is exactly what this table already models, and
-- reusing it means the booking goes through the same reserve/cancel procedure and the same
-- reservation ledger as everything else.
--
-- Re-runnable, and it needs to be. The window is generated relative to SYSDATE, but 00_init.sh only
-- runs the seed when AMS_CUSTOMERS is empty - so on a database that has been up for a few weeks the
-- generated dates quietly drift into the past and the last step of the ordering flow goes empty
-- with nothing to say why. Re-running this file rolls the window forward.
--
-- Only future slots that nobody holds are cleared: a slot with a reservation against it is left
-- alone, so refreshing the calendar never strands a despatch an order is relying on.
DELETE FROM AMS_TIMESLOTS
 WHERE CALL_TYPE_CD = 'SHIP'
   AND START_TM > SYSTIMESTAMP
   AND TIMESLOT_ID NOT IN (SELECT TIMESLOT_ID FROM AMS_TIMESLOT_RESERVATIONS);

DECLARE
  l_day   DATE;
  l_start TIMESTAMP;
BEGIN
  FOR r IN (SELECT DISTINCT REGION_CD FROM AMS_INSTALL_REGIONS ORDER BY REGION_CD) LOOP
    FOR offset_days IN 2 .. 21 LOOP
      l_day := TRUNC(SYSDATE) + offset_days;
      -- Nothing despatches at the weekend, so offering those windows would book a van that
      -- does not run.
      CONTINUE WHEN TO_CHAR(l_day, 'DY', 'NLS_DATE_LANGUAGE=AMERICAN') IN ('SAT', 'SUN');

      l_start := CAST(l_day AS TIMESTAMP) + INTERVAL '8' HOUR;
      INSERT INTO AMS_TIMESLOTS
             (TIMESLOT_ID, CALL_TYPE_CD, REGION_CD, START_TM, END_TM,
              CAPACITY, RESERVED_COUNT, AVAILABLE_FL, DISPLAY_LABEL, TIME_ZONE)
      VALUES (AMS_TIMESLOTS_SQ.NEXTVAL, 'SHIP', r.REGION_CD,
              l_start, l_start + INTERVAL '4' HOUR,
              6, 0, 'Y', 'Morning despatch (8:00 AM - 12:00 PM)', 'America/New_York');

      l_start := CAST(l_day AS TIMESTAMP) + INTERVAL '13' HOUR;
      INSERT INTO AMS_TIMESLOTS
             (TIMESLOT_ID, CALL_TYPE_CD, REGION_CD, START_TM, END_TM,
              CAPACITY, RESERVED_COUNT, AVAILABLE_FL, DISPLAY_LABEL, TIME_ZONE)
      VALUES (AMS_TIMESLOTS_SQ.NEXTVAL, 'SHIP', r.REGION_CD,
              l_start, l_start + INTERVAL '4' HOUR,
              4, 0, 'Y', 'Afternoon despatch (1:00 PM - 5:00 PM)', 'America/New_York');
    END LOOP;
  END LOOP;
END;
/
