package org.example.am.internal.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What the reverse proxy asserted about the caller.
 *
 * <p>There is no login form: the proxy authenticates the user and forwards the request with the
 * identity in headers. This object is the parsed form of those headers and becomes the principal of
 * the {@code PreAuthenticatedAuthenticationToken}.</p>
 *
 * <p>It carries no authorities of its own. The groups here are directory groups, which mean nothing
 * to this application until {@code AmsUserDetailsService} maps them to roles.</p>
 */
public class WebSealPrincipal implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private String emailAddress;
    private String accountNumber;
    private String firstName;
    private String lastName;
    private List<String> ldapGroups = new ArrayList<String>();

    public WebSealPrincipal(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /** @return the account number the proxy asserts for the caller's company, if any */
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public List<String> getLdapGroups() {
        return Collections.unmodifiableList(ldapGroups);
    }

    public void setLdapGroups(final List<String> ldapGroups) {
        this.ldapGroups = ldapGroups == null ? new ArrayList<String>() : new ArrayList<String>(ldapGroups);
    }

    public String getDisplayName() {
        final StringBuilder builder = new StringBuilder();
        if (firstName != null) {
            builder.append(firstName);
        }
        if (lastName != null) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(lastName);
        }
        return builder.length() == 0 ? username : builder.toString();
    }

    /** The principal is what Spring Security prints in its logs, so keep it to the identity. */
    @Override
    public String toString() {
        return username;
    }
}
