package org.example.am.internal.web.model;

import java.io.Serializable;
import java.util.List;
import org.example.am.shared.domain.AmsService;
import org.example.am.shared.domain.Customer;

/**
 * The customer administration screen: the one place an operator changes
 * customer state rather than reading it.
 */
public class CustomerAdminModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private Customer customer;
    private boolean canSubmitOrders;
    private int installedAssetCount;
    private List<AmsService> services;
    private String confirmationMessage;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    public boolean isCanSubmitOrders() {
        return canSubmitOrders;
    }

    public void setCanSubmitOrders(final boolean canSubmitOrders) {
        this.canSubmitOrders = canSubmitOrders;
    }

    public int getInstalledAssetCount() {
        return installedAssetCount;
    }

    public void setInstalledAssetCount(final int installedAssetCount) {
        this.installedAssetCount = installedAssetCount;
    }

    public List<AmsService> getServices() {
        return services;
    }

    public void setServices(final List<AmsService> services) {
        this.services = services;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(final String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

}
