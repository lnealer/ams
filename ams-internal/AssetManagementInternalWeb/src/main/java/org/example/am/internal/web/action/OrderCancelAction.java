package org.example.am.internal.web.action;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Abandons an order that has not been submitted.
 *
 * <p>Distinct from cancelling a submitted order: nothing has been persisted, so there is nothing to
 * reverse and no cancellation charge to consider.</p>
 */
@Component("OrderCancelAction")
@Scope("prototype")
public class OrderCancelAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;



    public String abandonOrder() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        clearModel();
        addActionMessage("The order has been discarded.");
        return Action.SUCCESS;
    }
}
