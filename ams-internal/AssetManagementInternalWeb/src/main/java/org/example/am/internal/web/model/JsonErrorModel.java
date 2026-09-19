package org.example.am.internal.web.model;

import java.io.Serializable;

/**
 * The body returned to a JSON caller whose request could not be served.
 *
 * <p>The grids treat {@code invalidSession} as the signal to stop polling and send the user to the
 * login flow. Returning it as data rather than as a 401 matters: the vendored Dojo transport
 * swallows the status code, so a status-only answer would leave the grid spinning forever.</p>
 */
public class JsonErrorModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String INVALID_SESSION = "INVALID_SESSION";

    private boolean invalidSession;
    private String message;
    private String detail;

    public JsonErrorModel() {
        super();
    }

    public JsonErrorModel(final boolean invalidSession, final String message) {
        this.invalidSession = invalidSession;
        this.message = message;
    }

    /** @return the standard body for an expired or missing session */
    public static JsonErrorModel invalidSession() {
        return new JsonErrorModel(true, INVALID_SESSION);
    }

    public boolean isInvalidSession() {
        return invalidSession;
    }

    public void setInvalidSession(final boolean invalidSession) {
        this.invalidSession = invalidSession;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(final String detail) {
        this.detail = detail;
    }
}
