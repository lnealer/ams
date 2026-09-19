package org.example.am.internal.web.action;

import java.util.Date;
import java.util.List;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.CalendarService;
import org.example.am.shared.domain.Timeslot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Serves the techline slot picker.
 */
@Component("AjaxTechlineAction")
@Scope("prototype")
public class AjaxTechlineAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private List<Timeslot> timeslots;
    private Long customerId;
    private Date fromDate;
    private Date toDate;
    private Date earliestDate;

    @Autowired
    private transient CalendarService internalCalendarService;

    public String getTechlineSlots() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SCHEDULE_TECHLINE);
        if (denied != null) {
            return denied;
        }
        final long customer = customerId == null ? currentCustomerId() : customerId.longValue();
        earliestDate = internalCalendarService.getEarliestTechlineDate(customer);
        timeslots = internalCalendarService.getTechlineTimeslots(customer, fromDate, toDate);
        return Action.SUCCESS;
    }

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(final List<Timeslot> timeslots) {
        this.timeslots = timeslots;
    }

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

    public Date getEarliestDate() {
        return earliestDate;
    }

    public void setEarliestDate(final Date earliestDate) {
        this.earliestDate = earliestDate;
    }

    private long currentCustomerId() {
        final Long sessionCustomerId = getCurrentCustomerId();
        return sessionCustomerId == null ? 0L : sessionCustomerId.longValue();
    }
}
