package org.example.am.shared.service;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.FacilitationCallType;
import org.example.am.shared.domain.Timeslot;

/**
 * Business-day arithmetic and timeslot lookup.
 *
 * <p>A business day here is a weekday that is not in {@code AMS_HOLIDAYS}. The holiday list is read
 * once for the whole window rather than one day at a time, because adding thirty business days
 * would otherwise cost thirty round trips.</p>
 */
public interface CalendarService {

    /** @return the date {@code businessDays} business days after {@code from} */
    Date addBusinessDays(Date from, int businessDays);

    /** @return the number of business days between the two dates, exclusive of {@code from} */
    int countBusinessDays(Date from, Date to);

    boolean isBusinessDay(Date date);

    List<Timeslot> getAvailableTimeslots(FacilitationCallType callType, Date from, Date to);

    Timeslot getTimeslot(long timeslotId);

    /** @return the dates in the window that this customer has asked not to be scheduled on */
    List<Date> getCustomerBlackoutDates(long customerId, Date from, Date to);
}
