package org.example.am.internal.service;

import org.example.am.internal.security.AmsUser;
import org.example.am.internal.security.WebSealPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Turns the identity the reverse proxy asserted into an authorised {@link AmsUser}.
 *
 * <p>Implements Spring Security's {@link AuthenticationUserDetailsService} rather than the plain
 * {@code UserDetailsService}, because the whole point of pre-authentication is that the token
 * carries more than a username: the directory groups come with it, and those are what the roles are
 * derived from.</p>
 */
public interface AmsUserDetailsService extends AuthenticationUserDetailsService<Authentication> {

    /**
     * @param authentication the pre-authenticated token whose principal is a {@link WebSealPrincipal}
     * @return the authorised user
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException when the
     *         token carries no usable identity
     */
    @Override
    UserDetails loadUserDetails(Authentication authentication);
}
