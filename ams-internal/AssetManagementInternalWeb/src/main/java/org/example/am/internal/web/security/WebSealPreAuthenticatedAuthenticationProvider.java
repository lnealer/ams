package org.example.am.internal.web.security;

import org.example.am.internal.service.AmsUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * Turns the token the pre-authentication filter created into an authenticated one.
 *
 * <p>There is nothing to verify - the proxy already authenticated the caller - so the provider's
 * only job is authorisation: hand the asserted identity to {@link AmsUserDetailsService}, which
 * resolves the directory groups into AMS roles, and rebuild the token around the result.</p>
 */
public class WebSealPreAuthenticatedAuthenticationProvider implements AuthenticationProvider {

    private final AmsUserDetailsService amsUserDetailsService;

    public WebSealPreAuthenticatedAuthenticationProvider(
            final AmsUserDetailsService amsUserDetailsService) {
        this.amsUserDetailsService = amsUserDetailsService;
    }

    @Override
    public Authentication authenticate(final Authentication authentication)
            throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        final UserDetails user = amsUserDetailsService.loadUserDetails(authentication);
        final PreAuthenticatedAuthenticationToken authenticated = new PreAuthenticatedAuthenticationToken(
                user, authentication.getCredentials(), user.getAuthorities());
        authenticated.setDetails(authentication.getDetails());
        return authenticated;
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
