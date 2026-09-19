package org.example.am.internal.service.dao;

import java.util.List;

import org.example.am.internal.security.LdapGroupMapping;
import org.example.am.internal.security.SecurityRoleType;

/**
 * Resolves the roles a pre-authenticated user holds.
 *
 * <p>The reverse proxy tells us who the user is and which LDAP groups they are in; the mapping from
 * those groups to AMS roles is data, so a directory change does not need an application release.</p>
 */
public interface AmsUserDetailsDAO {

    /**
     * @param ldapGroups the groups the proxy asserted, in the order it supplied them
     * @return the distinct roles those groups grant; empty when none of them map to anything
     */
    List<SecurityRoleType> getRolesForLdapGroups(List<String> ldapGroups);

    List<LdapGroupMapping> getAllMappings();

    String getEmailAddressForUser(String userId);
}
