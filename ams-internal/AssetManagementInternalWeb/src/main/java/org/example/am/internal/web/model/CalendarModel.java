package org.example.am.internal.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.example.am.shared.domain.Timeslot;

/**
 * Backs the timeslot pickers.
 *
 * <p>The same model serves the techline, installation and change windows; the action decides which
 * calendar to fill it from.</p>
 */
public class CalendarModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private Date fromDate;
    private Date toDate;
    private List<Timeslot> timeslots;
    private Date earliestDate;
    private Date latestDate;
    private Long selectedTimeslotId;
    private String callType;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(final Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(final Date toDate) {
        this.toDate = toDate;
    }

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(final List<Timeslot> timeslots) {
        this.timeslots = timeslots;
    }

    public Date getEarliestDate() {
        return earliestDate;
    }

    public void setEarliestDate(final Date earliestDate) {
        this.earliestDate = earliestDate;
    }

    public Date getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(final Date latestDate) {
        this.latestDate = latestDate;
    }

    public Long getSelectedTimeslotId() {
        return selectedTimeslotId;
    }

    public void setSelectedTimeslotId(final Long selectedTimeslotId) {
        this.selectedTimeslotId = selectedTimeslotId;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(final String callType) {
        this.callType = callType;
    }

    public boolean isEmpty() {
        return timeslots == null || timeslots.isEmpty();
    }

    public int getSlotCount() {
        return timeslots == null ? 0 : timeslots.size();
    }

}
