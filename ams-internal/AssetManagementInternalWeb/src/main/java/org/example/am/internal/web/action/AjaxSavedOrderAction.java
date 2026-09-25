package org.example.am.internal.web.action;

import java.util.List;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.domain.SaveForLaterType;
import org.example.am.shared.service.SaveForLaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Lists and discards the operator's parked orders.
 */
@Component("AjaxSavedOrderAction")
@Scope("prototype")
public class AjaxSavedOrderAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private List<Long> savedCustomerIds;
    private Long customerId;
    private boolean discarded;

    @Autowired
    private transient SaveForLaterService saveForLaterService;

    public String listSavedOrders() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SAVE_ORDER_FOR_LATER);
        if (denied != null) {
            return denied;
        }
        savedCustomerIds = saveForLaterService.getSavedCustomerIds(getUserId(),
                SaveForLaterType.ORDER);
        return Action.SUCCESS;
    }

    public String discardSavedOrder() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SAVE_ORDER_FOR_LATER);
        if (denied != null) {
            return denied;
        }
        if (customerId == null) {
            return Action.ERROR;
        }
        saveForLaterService.discard(getUserId(), customerId.longValue(), SaveForLaterType.ORDER);
        discarded = true;
        return Action.SUCCESS;
    }

    public List<Long> getSavedCustomerIds() {
        return savedCustomerIds;
    }

    public void setSavedCustomerIds(final List<Long> savedCustomerIds) {
        this.savedCustomerIds = savedCustomerIds;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public boolean isDiscarded() {
        return discarded;
    }

    public void setDiscarded(final boolean discarded) {
        this.discarded = discarded;
    }
}
