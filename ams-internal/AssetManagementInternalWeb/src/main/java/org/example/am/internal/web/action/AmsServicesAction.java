package org.example.am.internal.web.action;

import java.util.List;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.AmsServicesService;
import org.example.am.shared.domain.AmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The customer's service subscriptions.
 */
@Component("AmsServicesAction")
@Scope("prototype")
public class AmsServicesAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private List<AmsService> services;

    @Autowired
    private transient AmsServicesService amsServicesService;

    public String listServices() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_SERVICES);
        if (denied != null) {
            return denied;
        }
        final Long resolved = customerId == null ? getCurrentCustomerId() : customerId;
        if (resolved == null) {
            addActionError("Choose a customer first.");
            return Action.INPUT;
        }
        services = amsServicesService.getServices(resolved.longValue());
        return Action.SUCCESS;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public List<AmsService> getServices() {
        return services;
    }

    public void setServices(final List<AmsService> services) {
        this.services = services;
    }
}
