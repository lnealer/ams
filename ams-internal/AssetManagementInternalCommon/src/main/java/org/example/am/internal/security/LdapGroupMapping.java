package org.example.am.internal.security;

import java.io.Serializable;

/**
 * One row of the table that maps an LDAP group to an AMS role.
 *
 * <p>Kept in the database rather than in configuration so that directory administrators can grant
 * an existing role to a new group without an application release.</p>
 */
public class LdapGroupMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ldapGroupName;
    private SecurityRoleType securityRole;
    private boolean active;

    public String getLdapGroupName() {
        return ldapGroupName;
    }

    public void setLdapGroupName(final String ldapGroupName) {
        this.ldapGroupName = ldapGroupName;
    }

    public SecurityRoleType getSecurityRole() {
        return securityRole;
    }

    public void setSecurityRole(final SecurityRoleType securityRole) {
        this.securityRole = securityRole;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return ldapGroupName + " -> " + securityRole;
    }
}
