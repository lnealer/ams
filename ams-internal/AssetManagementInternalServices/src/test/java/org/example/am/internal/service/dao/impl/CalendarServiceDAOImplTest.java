package org.example.am.internal.service.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.example.am.internal.service.AbstractInternalTest;
import org.example.am.internal.service.dao.CalendarServiceDAO;
import org.example.am.shared.domain.FacilitationCallType;
import org.example.am.shared.domain.Timeslot;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CalendarServiceDAOImplTest extends AbstractInternalTest {

    private static final SimpleDateFormat DAY = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private CalendarServiceDAO calendarServiceDAO;

    private static Date day(final String text) {
        try {
            return DAY.parse(text);
        } catch (final java.text.ParseException impossible) {
            throw new IllegalArgumentException(text, impossible);
        }
    }

    /**
     * The operator view shows full slots too, greyed out, so an agent can see that a day is booked
     * out rather than simply not being offered it.
     */
    @Test
    public void theOperatorViewIncludesFullSlots() {
        final List<Timeslot> all = calendarServiceDAO.getAllTimeslots(FacilitationCallType.TECHLINE,
                day("2025-09-01"), day("2025-09-30"));
        assertEquals(2, all.size());

        final List<Timeslot> bookable = calendarServiceDAO.getAvailableTimeslots(
                FacilitationCallType.TECHLINE, day("2025-09-01"), day("2025-09-30"));
        assertEquals(1, bookable.size());
        assertEquals(Long.valueOf(9701L), bookable.get(0).getTimeslotId());
    }

    @Test
    public void slotsThatAreClosedAreNotBookable() {
        final List<Timeslot> bookable = calendarServiceDAO.getAvailableTimeslots(
                FacilitationCallType.INSTALLATION, day("2025-09-01"), day("2025-09-30"));
        // 9704 has capacity but is flagged unavailable.
        assertEquals(1, bookable.size());
        assertEquals(Long.valueOf(9703L), bookable.get(0).getTimeslotId());
    }

    @Test
    public void holidaysAreReadForTheWindowOnly() {
        assertEquals(1, calendarServiceDAO.getHolidays(day("2025-07-01"), day("2025-07-31")).size());
        assertEquals(3, calendarServiceDAO.getHolidays(day("2025-01-01"), day("2025-12-31")).size());
        assertTrue(calendarServiceDAO.getHolidays(null, day("2025-12-31")).isEmpty());
    }

    @Test
    public void customerBlackoutsAreScopedToTheCustomer() {
        assertEquals(1, calendarServiceDAO.getBlackoutDates(CUSTOMER_ID,
                day("2025-09-01"), day("2025-09-30")).size());
        assertTrue(calendarServiceDAO.getBlackoutDates(OTHER_CUSTOMER_ID,
                day("2025-09-01"), day("2025-09-30")).isEmpty());
    }

    @Test
    public void onlyAScheduledDecommissionHasADate() {
        // 5004's decommission is completed, not scheduled.
        assertNull(calendarServiceDAO.getScheduledDecommissionDate(5004L));
        assertNull(calendarServiceDAO.getScheduledDecommissionDate(HEALTHY_ASSET_ID));
    }
}
