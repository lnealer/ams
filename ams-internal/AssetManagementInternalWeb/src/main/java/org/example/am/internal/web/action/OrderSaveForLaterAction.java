package org.example.am.internal.web.action;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.shared.domain.SaveForLaterType;
import org.example.am.shared.service.SaveForLaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Parks a half-finished order and picks it up again.
 */
@Component("OrderSaveForLaterAction")
@Scope("prototype")
public class OrderSaveForLaterAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;

    /**
     * @return a short human readable description of the parked order, which is what the dashboard
     *         lists. The form state itself is rebuilt from the session, so nothing sensitive needs
     *         to be written to the saved row.
     */
    private static String describe(final OrderModel model) {
        final StringBuilder builder = new StringBuilder();
        builder.append(model.getOrderType() == null
                ? "Order" : model.getOrderType().getDescription());
        if (model.getSelectedAsset() != null) {
            builder.append(" for ").append(model.getSelectedAsset().getDisplayTag());
        }
        return builder.toString();
    }

    private boolean saved;

    @Autowired
    private transient SaveForLaterService saveForLaterService;

    public String saveForLater() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SAVE_ORDER_FOR_LATER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (model.getCustomerId() == null) {
            addActionError("Choose a customer before saving.");
            return Action.INPUT;
        }
        saveForLaterService.save(getUserId(), model.getCustomerId().longValue(),
                SaveForLaterType.ORDER, describe(model));
        saved = true;
        addActionMessage("The order has been saved. You can pick it up again from the dashboard.");
        return Action.SUCCESS;
    }

    public String continueOrder() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SAVE_ORDER_FOR_LATER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (model.getCustomerId() == null) {
            return Action.INPUT;
        }
        final String payload = saveForLaterService.load(getUserId(),
                model.getCustomerId().longValue(), SaveForLaterType.ORDER);
        if (payload == null) {
            addActionError("There is no saved order for this customer.");
            return Action.INPUT;
        }
        storeModel(model);
        return Action.SUCCESS;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(final boolean saved) {
        this.saved = saved;
    }
}
