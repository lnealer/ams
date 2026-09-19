package org.example.am.shared.service;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.Timeslot;

/**
 * Common contract for the things AMS schedules: techline calls, installation visits, network
 * change work and decommissions.
 */
public interface ScheduleService {

    /** @return the slots that may be offered for this entity, honouring lead time and blackouts */
    List<Timeslot> getAvailableSlots(long customerId, Date from, Date to);

    /** @return the earliest date this kind of work may be scheduled for */
    Date getEarliestSchedulableDate(long customerId);

    /**
     * Reserves the slot.
     *
     * @return {@code true} when the reservation succeeded; {@code false} when the slot filled up
     *         first, which the caller shows as "please pick another time"
     */
    boolean reserve(long timeslotId, long entityId, Date scheduledDate, String userId);
}
