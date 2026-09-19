package org.example.am.internal.web.interceptors;

import org.example.am.internal.utils.InternalConstants;

/**
 * The token check for endpoints whose caller expects JSON.
 *
 * <p>Identical logic to {@link AjaxTokenInterceptor}; only the failure result differs, so that a
 * grid receives a JSON body it can act on rather than an HTML error page it would render as
 * gibberish inside a table cell.</p>
 */
public class AjaxJsonTokenInterceptor extends AjaxTokenInterceptor {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getInvalidTokenResult() {
        return InternalConstants.RESULT_INVALID_JSON_TOKEN;
    }
}
