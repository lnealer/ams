package org.example.am.shared.logging;

/**
 * Logger names and diagnostic context keys.
 *
 * <p>{@link #ALERT_LOGGER} is watched by the infrastructure alerting pipeline: anything written to
 * it raises a ticket, so it is reserved for failures that need a human.</p>
 */
public final class LoggingConstants {

    public static final String ALERT_LOGGER = "ALERT_LOGGER";
    public static final String APP_VERSION_LOGGER = "APP_VERSION_LOGGER";
    public static final String APPLICATION_LOGGER = "org.example.am";

    /** Thread context keys stamped by the web tier's logging filter. */
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_ACTIVITY_TYPE = "activityType";
    public static final String MDC_REQUEST_ID = "requestId";

    /** Activity types stamped onto the diagnostic context. */
    public static final String ACTIVITY_HTTP = "HTTP";
    public static final String ACTIVITY_SERVICE = "SERVICE";
    public static final String ACTIVITY_DAO = "DAO";
    public static final String ACTIVITY_REST = "REST";

    private LoggingConstants() {
        super();
    }
}
