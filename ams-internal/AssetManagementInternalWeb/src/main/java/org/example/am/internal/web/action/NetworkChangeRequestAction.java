package org.example.am.internal.web.action;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.CalendarService;
import org.example.am.internal.utils.InternalConstants;
import org.example.am.internal.web.model.NetworkChangeRequestModel;
import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.service.NetworkChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Collection;
import org.example.am.shared.domain.NetworkChangeRequestType;

/**
 * Raises a network change request.
 */
@Component("NetworkChangeRequestAction")
@Scope("prototype")
public class NetworkChangeRequestAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    /**
     * The change types the checkbox list on {@code networkChangeRequest.jsp} offers.
     *
     * <p>A request may carry more than one, which is why this is a checkbox list rather than a
     * radio group - and why selecting SITE_TYPE_CHANGE alongside others still routes the whole
     * request down the complex scheduling path.</p>
     */
    public Collection<NetworkChangeRequestType> getChangeTypeOptions() {
        return NetworkChangeRequestType.values();
    }

    /** The in-progress request lives in the session, like the ordering flow's model. */
    protected NetworkChangeRequestModel getRequestModel() {
        final javax.servlet.http.HttpSession session = getOrCreateSession();
        NetworkChangeRequestModel model = (NetworkChangeRequestModel)
                session.getAttribute(InternalConstants.SESSION_NCR_MODEL);
        if (model == null) {
            model = new NetworkChangeRequestModel();
            session.setAttribute(InternalConstants.SESSION_NCR_MODEL, model);
        }
        return model;
    }

    protected void storeRequestModel(final NetworkChangeRequestModel model) {
        getOrCreateSession().setAttribute(InternalConstants.SESSION_NCR_MODEL, model);
    }

    protected void clearRequestModel() {
        final javax.servlet.http.HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(InternalConstants.SESSION_NCR_MODEL);
        }
    }

    private Long submittedRequestId;

    @Autowired
    private transient NetworkChangeRequestService networkChangeRequestService;

    @Autowired
    private transient CalendarService internalCalendarService;

    public String initRequest() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_NCR);
        if (denied != null) {
            return denied;
        }
        final NetworkChangeRequestModel model = getRequestModel();
        if (model.getCustomerId() == null) {
            model.setCustomerId(getCurrentCustomerId());
        }
        storeRequestModel(model);
        return Action.SUCCESS;
    }

    public String submitRequest() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SUBMIT_NCR);
        if (denied != null) {
            return denied;
        }
        final NetworkChangeRequestModel model = getRequestModel();
        final NetworkChangeRequest request = model.toNetworkChangeRequest();
        request.setCurrentTime(getCurrentTime());

        if (!request.isSubmittable()) {
            addActionError("The request is not complete enough to submit.");
            return Action.INPUT;
        }
        submittedRequestId =
                Long.valueOf(networkChangeRequestService.submit(request, getUserId()));

        if (model.getRequestedDate() != null) {
            request.setNetworkChangeRequestId(submittedRequestId);
            final boolean booked = internalCalendarService.reserveNetworkChangeDate(request,
                    model.getRequestedDate(), getUserId());
            if (!booked) {
                // The request stands; only the date could not be held. Saying so is more useful
                // than failing the whole submit and losing the request.
                addActionMessage("The request was submitted, but the date you asked for is no"
                        + " longer available. Please reschedule it.");
            }
        }
        clearRequestModel();
        return Action.SUCCESS;
    }

    public Long getSubmittedRequestId() {
        return submittedRequestId;
    }

    public void setSubmittedRequestId(final Long submittedRequestId) {
        this.submittedRequestId = submittedRequestId;
    }
}
