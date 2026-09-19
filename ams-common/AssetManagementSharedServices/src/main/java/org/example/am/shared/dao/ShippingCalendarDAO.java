package org.example.am.shared.dao;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.Timeslot;

/**
 * Despatch capacity: which warehouse regions can ship on which day.
 *
 * <p>Reads the same {@code AMS_TIMESLOTS} table as the installation calendar, filtered to
 * {@code CALL_TYPE_CD = 'SHIP'}. Carrying shipping on the shared calendar rather than a table of
 * its own is what lets a window be booked through the existing reservation procedure.</p>
 */
public interface ShippingCalendarDAO {

    List<Timeslot> getShippingWindows(String regionCode, Date from, Date to);

    Timeslot getShippingWindow(long timeslotId);
}
