package org.example.am.internal.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.example.am.shared.domain.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The authenticated internal user.
 *
 * <p>The password is always blank and the account flags are always true: authentication happened at
 * the reverse proxy, so there is no credential for this application to hold, expire or lock. The
 * {@link UserDetails} contract still has to be satisfied, and returning a blank password rather
 * than {@code null} keeps Spring Security's own null checks happy.</p>
 */
public class AmsUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    private static final String NO_PASSWORD = "";

    private final String username;
    private final Collection<GrantedAuthority> authorities;
    private String emailAddress;
    private String displayName;
    private List<String> ldapGroups = new ArrayList<String>();
    private Customer customer;

    public AmsUser(final String username, final Collection<GrantedAuthority> authorities) {
        this.username = username;
        this.authorities = authorities == null
                ? Collections.<GrantedAuthority>emptyList()
                : Collections.unmodifiableCollection(new ArrayList<GrantedAuthority>(authorities));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return NO_PASSWORD;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getDisplayName() {
        return displayName == null ? username : displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public List<String> getLdapGroups() {
        return Collections.unmodifiableList(ldapGroups);
    }

    public void setLdapGroups(final List<String> ldapGroups) {
        this.ldapGroups = ldapGroups == null ? new ArrayList<String>() : new ArrayList<String>(ldapGroups);
    }

    /**
     * @return the customer the operator is currently acting on behalf of, or {@code null} before
     *         one has been selected. Internal users are not tied to a customer, so this is session
     *         state rather than an attribute of the account.
     */
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    /**
     * @param role the role to test
     * @return {@code true} when this user holds it
     */
    public boolean hasRole(final SecurityRoleType role) {
        if (role == null) {
            return false;
        }
        for (final GrantedAuthority authority : authorities) {
            if (role.getAuthority().equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return username;
    }
}
