package org.example.am.internal.web.action;

import java.util.Date;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.CalendarService;
import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.service.NetworkChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Moves a booked network change to a different date.
 */
@Component("RescheduleNcrAction")
@Scope("prototype")
public class RescheduleNcrAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long networkChangeRequestId;
    private Date newDate;
    private NetworkChangeRequest request;

    @Autowired
    private transient NetworkChangeRequestService networkChangeRequestService;

    @Autowired
    private transient CalendarService internalCalendarService;

    public String initReschedule() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_RESCHEDULE_NCR);
        if (denied != null) {
            return denied;
        }
        if (networkChangeRequestId == null) {
            return Action.ERROR;
        }
        request = networkChangeRequestService.getNetworkChangeRequest(
                networkChangeRequestId.longValue());
        if (request == null || !request.isOpen()) {
            addActionError("That request can no longer be rescheduled.");
            return Action.ERROR;
        }
        return Action.SUCCESS;
    }

    public String submitReschedule() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_RESCHEDULE_NCR);
        if (denied != null) {
            return denied;
        }
        if (networkChangeRequestId == null || newDate == null) {
            addActionError("Choose a new date.");
            return Action.INPUT;
        }
        request = networkChangeRequestService.getNetworkChangeRequest(
                networkChangeRequestId.longValue());
        if (request == null || !request.isOpen()) {
            addActionError("That request can no longer be rescheduled.");
            return Action.ERROR;
        }

        // Release the old booking before taking the new one, so the two cannot both be held.
        internalCalendarService.cancelNetworkChangeDate(request, "Rescheduled", getUserId());
        final boolean booked =
                internalCalendarService.reserveNetworkChangeDate(request, newDate, getUserId());
        if (!booked) {
            addActionError("That date is not available. The request is now unscheduled;"
                    + " please choose another date.");
            return Action.INPUT;
        }
        addActionMessage("The request has been rescheduled.");
        return Action.SUCCESS;
    }

    public Long getNetworkChangeRequestId() {
        return networkChangeRequestId;
    }

    public void setNetworkChangeRequestId(final Long networkChangeRequestId) {
        this.networkChangeRequestId = networkChangeRequestId;
    }

    public Date getNewDate() {
        return newDate;
    }

    public void setNewDate(final Date newDate) {
        this.newDate = newDate;
    }

    public NetworkChangeRequest getRequest() {
        return request;
    }

    public void setRequest(final NetworkChangeRequest request) {
        this.request = request;
    }
}
