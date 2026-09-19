package org.example.am.shared.service;

import java.util.Date;
import java.util.List;
import org.example.am.shared.domain.Timeslot;

/**
 * Installation scheduling view of the calendar, resolved by the destination postcode's engineer
 * region.
 */
public interface InstallationCalendarService {

    /**
     * @return the bookable installation slots serving this postcode; empty when the postcode is
     *         not mapped to a region
     */
    List<Timeslot> getSlotsForZipCode(String zipCode, java.util.Date from, java.util.Date to);

    String getRegion(String zipCode);
}
