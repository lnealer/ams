package org.example.am.shared.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.example.am.shared.dao.ShippingCalendarDAO;
import org.example.am.shared.domain.Timeslot;
import org.example.am.shared.helper.AbstractBaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ShippingCalendarDAOImplTest extends AbstractBaseTest {

    @Autowired
    @Qualifier("shippingCalendarSharedDAO")
    private ShippingCalendarDAO shippingCalendarDAO;

    private static Date on(final int year, final int month, final int day) {
        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private static Date from() {
        return on(2025, 9, 1);
    }

    private static Date to() {
        return on(2025, 9, 30);
    }

    @Test
    public void bookableWindowsAreReturnedForTheRegion() {
        final List<Timeslot> windows =
                shippingCalendarDAO.getShippingWindows("CENTRAL", from(), to());
        // 9705 has room. 9706 is full and 9707 is closed, so neither may be offered.
        assertEquals(1, windows.size());
        assertEquals(Long.valueOf(9705L), windows.get(0).getTimeslotId());
    }

    /** A full window is not bookable even though operations left it open. */
    @Test
    public void aFullWindowIsNotOffered() {
        for (final Timeslot window : shippingCalendarDAO.getShippingWindows("CENTRAL", from(), to())) {
            assertTrue(window.getReserved() < window.getCapacity());
        }
    }

    /** Windows belong to a region; another region's capacity is not this region's. */
    @Test
    public void windowsAreScopedToTheRegion() {
        final List<Timeslot> northeast =
                shippingCalendarDAO.getShippingWindows("NORTHEAST", from(), to());
        assertEquals(1, northeast.size());
        assertEquals(Long.valueOf(9708L), northeast.get(0).getTimeslotId());
    }

    @Test
    public void anUnknownRegionYieldsNothingRatherThanEverything() {
        assertTrue(shippingCalendarDAO.getShippingWindows("ATLANTIS", from(), to()).isEmpty());
    }

    @Test
    public void aNullRegionIsNotSentToTheDatabase() {
        assertTrue(shippingCalendarDAO.getShippingWindows(null, from(), to()).isEmpty());
    }

    /**
     * Reading a chosen window back must not apply the availability filter: by then it is likely to
     * be full, because it holds this order's own reservation.
     */
    @Test
    public void aChosenWindowIsReadableEvenWhenFull() {
        final Timeslot full = shippingCalendarDAO.getShippingWindow(9706L);
        assertNotNull(full);
        assertEquals(full.getCapacity(), full.getReserved());

        final Timeslot closed = shippingCalendarDAO.getShippingWindow(9707L);
        assertNotNull(closed);
    }

    /** An installation slot is not a despatch window, even though they share the table. */
    @Test
    public void anInstallationSlotIsNotAShippingWindow() {
        assertNull(shippingCalendarDAO.getShippingWindow(9703L));
    }
}
