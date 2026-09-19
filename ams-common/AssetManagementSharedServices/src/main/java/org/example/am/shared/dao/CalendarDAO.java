package org.example.am.shared.dao;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.FacilitationCallType;
import org.example.am.shared.domain.Timeslot;

/**
 * Reads the calendar tables: the holiday list that business-day arithmetic has to skip, and the
 * bookable timeslots offered by each calendar.
 */
public interface CalendarDAO {

    /** @return the holiday dates falling inside the window, truncated to whole days */
    List<Date> getHolidays(Date from, Date to);

    boolean isHoliday(Date date);

    List<Timeslot> getAvailableTimeslots(FacilitationCallType callType, Date from, Date to);

    Timeslot getTimeslot(long timeslotId);

    /** @return the customer's own scheduling blackout dates from {@code AMS_CUSTOMER_SCHEDULES} */
    List<Date> getCustomerBlackoutDates(long customerId, Date from, Date to);
}
