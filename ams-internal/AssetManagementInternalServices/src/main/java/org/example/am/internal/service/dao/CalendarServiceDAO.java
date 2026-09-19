package org.example.am.internal.service.dao;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.FacilitationCallType;
import org.example.am.shared.domain.Timeslot;

/**
 * The internal application's own calendar reads.
 *
 * <p>Separate from the shared {@code CalendarDAO} because the internal screens need the operator
 * view: every slot including the full ones, so an agent can see that a day is booked out rather
 * than simply not being offered it.</p>
 */
public interface CalendarServiceDAO {

    /** @return every slot in the window, full ones included, so the grid can grey them out */
    List<Timeslot> getAllTimeslots(FacilitationCallType callType, Date from, Date to);

    List<Timeslot> getAvailableTimeslots(FacilitationCallType callType, Date from, Date to);

    List<Date> getHolidays(Date from, Date to);

    /** @return the customer's blackout dates from {@code AMS_CUSTOMER_SCHEDULES} */
    List<Date> getBlackoutDates(long customerId, Date from, Date to);

    /** @return the date a decommission is currently booked for, or {@code null} */
    Date getScheduledDecommissionDate(long assetId);
}
