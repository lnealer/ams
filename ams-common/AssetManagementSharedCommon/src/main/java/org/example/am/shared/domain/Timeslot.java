package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * A bookable window offered by the techline or installation calendar.
 *
 * <p>Capacity is decremented by the {@code ReserveTimeslotProcedure} inside the same transaction as
 * the owning order update, so an in-memory {@code Timeslot} is only ever a snapshot.</p>
 */
public class Timeslot implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long timeslotId;
    private Date startTime;
    private Date endTime;
    private int capacity;
    private int reserved;
    private boolean available;
    private String displayLabel;
    private String timeZone;

    public Long getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(final Long timeslotId) {
        this.timeslotId = timeslotId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(final Date endTime) {
        this.endTime = endTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(final int capacity) {
        this.capacity = capacity;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(final int reserved) {
        this.reserved = reserved;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(final boolean available) {
        this.available = available;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public void setDisplayLabel(final String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isFull() {
        return reserved >= capacity;
    }

    public int getRemainingCapacity() {
        final int remaining = capacity - reserved;
        return remaining < 0 ? 0 : remaining;
    }

    /**
     * A timeslot is only offerable when the calendar says it is open and it still has room.
     */
    public boolean isSelectable() {
        return available && !isFull();
    }

}
