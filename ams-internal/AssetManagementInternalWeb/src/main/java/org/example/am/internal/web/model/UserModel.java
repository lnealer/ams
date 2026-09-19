package org.example.am.internal.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Backs the non-production impersonation screen.
 *
 * <p>Populated only when that screen is enabled; see {@code UserAction} for the gating.</p>
 */
public class UserModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private List<String> ldapGroups;
    private List<String> availableGroups;
    private Date currentTimeOverride;
    private boolean impersonationEnabled;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public List<String> getLdapGroups() {
        return ldapGroups;
    }

    public void setLdapGroups(final List<String> ldapGroups) {
        this.ldapGroups = ldapGroups;
    }

    public List<String> getAvailableGroups() {
        return availableGroups;
    }

    public void setAvailableGroups(final List<String> availableGroups) {
        this.availableGroups = availableGroups;
    }

    public Date getCurrentTimeOverride() {
        return currentTimeOverride;
    }

    public void setCurrentTimeOverride(final Date currentTimeOverride) {
        this.currentTimeOverride = currentTimeOverride;
    }

    public boolean isImpersonationEnabled() {
        return impersonationEnabled;
    }

    public void setImpersonationEnabled(final boolean impersonationEnabled) {
        this.impersonationEnabled = impersonationEnabled;
    }

    public boolean isOverridingTime() {
        return currentTimeOverride != null;
    }

}
