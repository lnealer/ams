package org.example.am.internal.web.action;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.service.TermsAndConditionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shows the terms and records acceptance.
 */
@Component("TermsAndConditionsAction")
@Scope("prototype")
public class TermsAndConditionsAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private String version;
    private boolean acceptanceRequired;

    @Autowired
    private transient TermsAndConditionsService termsAndConditionsService;

    public String viewTerms() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_TERMS);
        if (denied != null) {
            return denied;
        }
        version = termsAndConditionsService.getCurrentVersion();
        acceptanceRequired = termsAndConditionsService.isAcceptanceRequired(getUserId());
        return Action.SUCCESS;
    }

    public String acceptTerms() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_TERMS);
        if (denied != null) {
            return denied;
        }
        termsAndConditionsService.accept(getUserId());
        acceptanceRequired = false;
        addActionMessage("Thank you. Your acceptance has been recorded.");
        return Action.SUCCESS;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public boolean isAcceptanceRequired() {
        return acceptanceRequired;
    }

    public void setAcceptanceRequired(final boolean acceptanceRequired) {
        this.acceptanceRequired = acceptanceRequired;
    }
}
