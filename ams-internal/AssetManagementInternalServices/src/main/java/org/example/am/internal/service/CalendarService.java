package org.example.am.internal.service;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.domain.Timeslot;

/**
 * The internal scheduling rules.
 *
 * <p>This is where the "when may this happen" questions are answered for every flow the operator
 * screens drive: techline calls, installation visits, network change work and decommissions. The
 * rules differ per flow in three ways - the minimum lead time, whether the customer's blackout
 * dates apply, and whether there is a hard ceiling on how far ahead work may be booked.</p>
 */
public interface CalendarService {

    /** @return the earliest date a techline call may be booked for this customer */
    Date getEarliestTechlineDate(long customerId);

    /** @return the bookable techline slots, blackouts and holidays already removed */
    List<Timeslot> getTechlineTimeslots(long customerId, Date from, Date to);

    /** @return the earliest date an installation visit may be booked for this customer */
    Date getEarliestInstallationDate(long customerId);

    List<Timeslot> getInstallationTimeslots(long customerId, Date from, Date to);

    /**
     * Books the change window for a request, routing to the simple or complex procedure according
     * to whether the request includes a site type change.
     *
     * @return {@code true} when the window was reserved
     */
    boolean reserveNetworkChangeDate(NetworkChangeRequest request, Date scheduledDate, String userId);

    boolean cancelNetworkChangeDate(NetworkChangeRequest request, String reason, String userId);

    /**
     * @return the last date a decommission may be booked for; work further out than this has to be
     *         re-raised nearer the time
     */
    Date getLatestDecommissionDate();

    /**
     * @throws IllegalArgumentException when the date is outside the schedulable window
     * @return {@code true} when the date was reserved
     */
    boolean reserveDecommissionDate(long decommissionId, long assetId, Date scheduledDate,
            boolean hardwareReturnRequired, String userId);

    boolean cancelDecommissionDate(long decommissionId, String reason, String userId);

    /** @return {@code true} when the date is a weekday, not a holiday and not a customer blackout */
    boolean isSchedulableDate(long customerId, Date date);
}
