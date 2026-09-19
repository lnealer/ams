package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.List;

import org.example.am.internal.security.AmsUser;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.security.WebSealPrincipal;
import org.example.am.internal.service.AmsUserDetailsService;
import org.example.am.internal.web.model.UserModel;
import org.example.am.shared.domain.PropertyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.Action;

/**
 * Lets a tester swap their own directory groups and move the application's clock.
 *
 * <p><strong>This is a test facility and must never be reachable in production.</strong> It lets
 * the caller rebuild their own authentication token with a different set of groups, which is
 * exactly the escalation the rest of the security model exists to prevent. Three independent things
 * have to be true before it will do anything:</p>
 *
 * <ol>
 *   <li>the environment is not production, per {@code ConfigService.isNonProductionEnvironment()};</li>
 *   <li>the {@code ENABLEIMPERS} property is switched on in that environment's database;</li>
 *   <li>the caller holds {@link SecurityRoleType#INT_CHANGE_USER}.</li>
 * </ol>
 *
 * <p>The gate is re-evaluated on every method, not cached, so revoking any one of the three takes
 * effect immediately. If the facility is not wanted at all, deleting this class and its
 * {@code struts-user.xml} entry removes it completely - nothing else depends on it.</p>
 */
@Component("UserAction")
@Scope("prototype")
public class UserAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private final UserModel model = new UserModel();

    @Autowired
    private transient AmsUserDetailsService amsUserDetailsService;

    @Override
    public UserModel getModel() {
        return model;
    }

    /** Renders the impersonation form. */
    public String changeUser() throws Exception {
        final String denied = requireImpersonation();
        if (denied != null) {
            return denied;
        }
        final AmsUser current = getAmsUser();
        model.setUsername(current == null ? getUserId() : current.getUsername());
        model.setLdapGroups(current == null
                ? new ArrayList<String>() : new ArrayList<String>(current.getLdapGroups()));
        model.setAvailableGroups(getKnownGroups());
        model.setCurrentTimeOverride(getCurrentTimeOverride());
        model.setImpersonationEnabled(true);
        return Action.SUCCESS;
    }

    /**
     * Rebuilds the caller's authentication token around the chosen groups, and optionally moves the
     * session's clock.
     */
    public String submitChangeUser() throws Exception {
        final String denied = requireImpersonation();
        if (denied != null) {
            return denied;
        }

        final String username = model.getUsername() == null || model.getUsername().trim().length() == 0
                ? getUserId() : model.getUsername().trim();

        final WebSealPrincipal principal = new WebSealPrincipal(username);
        principal.setLdapGroups(model.getLdapGroups());

        final PreAuthenticatedAuthenticationToken request =
                new PreAuthenticatedAuthenticationToken(principal, "N/A");
        final UserDetails impersonated = amsUserDetailsService.loadUserDetails(request);

        final PreAuthenticatedAuthenticationToken authenticated =
                new PreAuthenticatedAuthenticationToken(impersonated, "N/A",
                        impersonated.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticated);

        if (model.getCurrentTimeOverride() != null) {
            setCurrentTimeOverride(model.getCurrentTimeOverride());
        }

        // Logged loudly and unconditionally: an impersonation must always be traceable to the
        // person who performed it, whatever else happens afterwards.
        logger.warn("NON-PRODUCTION IMPERSONATION: {} is now acting as '{}' with groups {}{}",
                getUserId(), username, model.getLdapGroups(),
                model.getCurrentTimeOverride() == null
                        ? "" : " and the clock set to " + model.getCurrentTimeOverride());

        addActionMessage("You are now acting as '" + username + "'.");
        return Action.SUCCESS;
    }

    /** Drops the clock override, leaving the groups alone. */
    public String clearTimeOverride() throws Exception {
        final String denied = requireImpersonation();
        if (denied != null) {
            return denied;
        }
        setCurrentTimeOverride(null);
        addActionMessage("The clock override has been cleared.");
        return Action.SUCCESS;
    }

    /**
     * All three conditions, checked together.
     *
     * @return {@code null} when impersonation may proceed, otherwise the result to return
     */
    private String requireImpersonation() {
        if (!isImpersonationEnabled()) {
            logger.warn("User {} reached the impersonation screen, which is not enabled here",
                    getUserId());
            addActionError("This facility is not available in this environment.");
            return UNAUTHORIZED;
        }
        return requireRole(SecurityRoleType.INT_CHANGE_USER);
    }

    /**
     * @return {@code true} only when the environment is non production and the switch is on
     */
    public boolean isImpersonationEnabled() {
        if (!getConfigService().isNonProductionEnvironment()) {
            return false;
        }
        return getConfigService().getBoolean(PropertyType.ENABLE_USER_IMPERSONATION, false);
    }

    /**
     * @return the groups a tester can pick from. Derived from the roles this release defines, so
     *         the list cannot drift away from what the application actually understands.
     */
    private List<String> getKnownGroups() {
        final List<String> groups = new ArrayList<String>();
        groups.add("AMS_INTERNAL_READONLY");
        groups.add("AMS_INTERNAL_OPERATIONS");
        groups.add("AMS_INTERNAL_SCHEDULING");
        groups.add("AMS_INTERNAL_ADMIN");
        return groups;
    }
}
