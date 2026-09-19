package org.example.am.internal.security;

import java.io.Serializable;

import org.springframework.security.core.GrantedAuthority;

/**
 * One granted permission, wrapping a {@link SecurityRoleType}.
 *
 * <p>{@link #getAuthority()} returns the prefixed form Spring Security compares against, while
 * {@link #getSecurityRole()} keeps the typed constant available to the action layer, which checks
 * roles by their bare code through {@code HttpServletRequest.isUserInRole}.</p>
 */
public class AmsRole implements GrantedAuthority, Serializable {

    private static final long serialVersionUID = 1L;

    private final String authority;
    private final SecurityRoleType securityRole;

    /**
     * @param code a role code, with or without the {@code ROLE_} prefix
     */
    public AmsRole(final String code) {
        this.securityRole = SecurityRoleType.lookup(code);
        this.authority = SecurityRoleType.ROLE_PREFIX + SecurityRoleType.stripPrefix(code);
    }

    public AmsRole(final SecurityRoleType securityRole) {
        this.securityRole = securityRole;
        this.authority = securityRole == null
                ? SecurityRoleType.ROLE_USER : securityRole.getAuthority();
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public SecurityRoleType getSecurityRole() {
        return securityRole;
    }

    /** @return the role code without the Spring Security prefix */
    public String getCode() {
        return SecurityRoleType.stripPrefix(authority);
    }

    /** @return {@code true} when the authority names a role this release actually defines */
    public boolean isKnown() {
        return securityRole != null;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AmsRole)) {
            return false;
        }
        return authority.equals(((AmsRole) other).authority);
    }

    @Override
    public int hashCode() {
        return authority.hashCode();
    }

    @Override
    public String toString() {
        return authority;
    }
}
