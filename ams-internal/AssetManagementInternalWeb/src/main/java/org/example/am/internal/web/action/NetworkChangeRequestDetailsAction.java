package org.example.am.internal.web.action;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.service.NetworkChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * One network change request in full.
 */
@Component("NetworkChangeRequestDetailsAction")
@Scope("prototype")
public class NetworkChangeRequestDetailsAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long networkChangeRequestId;
    private NetworkChangeRequest request;
    private String cancellationReason;

    @Autowired
    private transient NetworkChangeRequestService networkChangeRequestService;

    public String viewRequest() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_NCR);
        if (denied != null) {
            return denied;
        }
        if (networkChangeRequestId == null) {
            return Action.ERROR;
        }
        request = networkChangeRequestService.getNetworkChangeRequest(
                networkChangeRequestId.longValue());
        if (request == null) {
            addActionError("That request could not be found.");
            return Action.ERROR;
        }
        request.setCurrentTime(getCurrentTime());
        return Action.SUCCESS;
    }

    public String cancelRequest() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CANCEL_NCR);
        if (denied != null) {
            return denied;
        }
        if (networkChangeRequestId == null) {
            return Action.ERROR;
        }
        if (cancellationReason == null || cancellationReason.trim().length() == 0) {
            addFieldErrorAndLog("cancellationReason", "Give a reason for the cancellation.");
            return Action.INPUT;
        }
        final boolean cancelled = networkChangeRequestService.cancel(
                networkChangeRequestId.longValue(), cancellationReason.trim(), getUserId());
        if (!cancelled) {
            addActionError("That request can no longer be cancelled.");
            return Action.INPUT;
        }
        addActionMessage("The network change request has been cancelled.");
        return Action.SUCCESS;
    }

    public Long getNetworkChangeRequestId() {
        return networkChangeRequestId;
    }

    public void setNetworkChangeRequestId(final Long networkChangeRequestId) {
        this.networkChangeRequestId = networkChangeRequestId;
    }

    public NetworkChangeRequest getRequest() {
        return request;
    }

    public void setRequest(final NetworkChangeRequest request) {
        this.request = request;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(final String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
