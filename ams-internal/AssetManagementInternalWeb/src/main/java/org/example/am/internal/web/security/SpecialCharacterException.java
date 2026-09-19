package org.example.am.internal.web.security;

/**
 * Raised when a request parameter contains a character outside the accepted whitelist.
 *
 * <p>Unchecked, because it is not a condition any caller can recover from: the request is rejected
 * and the global error page is rendered. The offending value is deliberately not carried on the
 * exception, so it cannot be echoed back into the error page.</p>
 */
public class SpecialCharacterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String parameterName;

    public SpecialCharacterException(final String parameterName) {
        super("Parameter '" + parameterName + "' contains characters that are not accepted");
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }
}
