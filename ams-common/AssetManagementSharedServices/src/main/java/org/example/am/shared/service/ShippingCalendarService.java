package org.example.am.shared.service;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.Timeslot;

/**
 * The despatch windows an order can be given, resolved from the destination postcode.
 */
public interface ShippingCalendarService {

    /**
     * @param zipCode destination postcode
     * @return the bookable despatch windows serving this postcode over the default horizon; empty
     *         when the postcode is not mapped to a region
     */
    List<Timeslot> getShippingWindowsForZipCode(String zipCode);

    List<Timeslot> getShippingWindowsForZipCode(String zipCode, Date from, Date to);

    Timeslot getShippingWindow(long timeslotId);

    /** @return the warehouse region serving this postcode, or {@code null} when it is unmapped */
    String getRegion(String zipCode);
}
