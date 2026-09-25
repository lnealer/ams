package org.example.am.internal.web.action;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.AmsServicesService;
import org.example.am.internal.service.AssetService;
import org.example.am.internal.web.model.CustomerAdminModel;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The customer administration screen.
 *
 * <p>The only place the internal application changes customer state rather than reading it, which
 * is why the ordering toggle sits behind its own role rather than the general customer role.</p>
 */
@Component("CustomerAdminAction")
@Scope("prototype")
public class CustomerAdminAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private final CustomerAdminModel model = new CustomerAdminModel();

    @Autowired
    private transient CustomerService customerService;

    @Autowired
    private transient AmsServicesService amsServicesService;

    @Autowired
    private transient AssetService internalAssetService;

    @Override
    public CustomerAdminModel getModel() {
        return model;
    }

    public String initCustomerAdmin() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CUSTOMER_ADMIN);
        if (denied != null) {
            return denied;
        }
        final Long customerId = resolveCustomerId();
        if (customerId == null) {
            addActionError("Choose a customer first.");
            return Action.INPUT;
        }
        final Customer customer = customerService.getCustomer(customerId.longValue());
        if (customer == null) {
            addActionError("That customer could not be found.");
            return Action.ERROR;
        }
        model.setCustomerId(customerId);
        model.setCustomer(customer);
        model.setCanSubmitOrders(customer.isCanSubmitOrders());
        model.setServices(amsServicesService.getServices(customerId.longValue()));
        model.setInstalledAssetCount(
                internalAssetService.getInstalledAssetCount(customerId.longValue()));
        return Action.SUCCESS;
    }

    /**
     * Toggles whether the customer may raise orders. Answers JSON, because the switch is flipped
     * in place on the page rather than through a form submit.
     */
    public String updateCanSubmitOrders() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_ENABLE_ORDERING);
        if (denied != null) {
            return denied;
        }
        final Long customerId = resolveCustomerId();
        if (customerId == null) {
            model.setConfirmationMessage("No customer was identified.");
            return Action.ERROR;
        }
        final boolean changed = customerService.updateCanSubmitOrders(customerId.longValue(),
                model.isCanSubmitOrders(), getUserId());
        if (!changed) {
            model.setConfirmationMessage("That customer could not be found.");
            return Action.ERROR;
        }
        model.setConfirmationMessage(model.isCanSubmitOrders()
                ? "Ordering has been enabled for this customer."
                : "Ordering has been disabled for this customer.");
        logger.info("User {} {} ordering for customer {}", getUserId(),
                model.isCanSubmitOrders() ? "enabled" : "disabled", customerId);
        return Action.SUCCESS;
    }

    /**
     * Renders the enable-ordering panel on its own, for the dashboard.
     */
    public String showEnableOrdering() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_ENABLE_ORDERING);
        if (denied != null) {
            return denied;
        }
        final Long customerId = resolveCustomerId();
        if (customerId == null) {
            return Action.INPUT;
        }
        final Customer customer = customerService.getCustomer(customerId.longValue());
        if (customer == null) {
            return Action.ERROR;
        }
        model.setCustomerId(customerId);
        model.setCustomer(customer);
        model.setCanSubmitOrders(customer.isCanSubmitOrders());
        return Action.SUCCESS;
    }

    /**
     * @return the customer from the request, falling back to the one already selected in the
     *         session
     */
    private Long resolveCustomerId() {
        return model.getCustomerId() == null ? getCurrentCustomerId() : model.getCustomerId();
    }
}
