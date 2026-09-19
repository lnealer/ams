package org.example.am.shared.dao;

import java.util.Date;
import java.util.List;
import org.example.am.shared.domain.Timeslot;

/**
 * Installation-specific view of the calendar: which engineer regions have capacity on which day.
 */
public interface InstallationCalendarDAO {

    List<Timeslot> getInstallationSlots(String regionCode, java.util.Date from, java.util.Date to);

    String getRegionForZipCode(String zipCode);
}
