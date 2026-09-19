package org.example.am.shared.logging;

import org.apache.logging.log4j.ThreadContext;

/**
 * Thin wrapper over the Log4j2 thread context so that callers do not have to know the key names,
 * and so the keys can only be cleared through {@link #clear()}.
 */
public final class LoggingUtils {

    private LoggingUtils() {
        super();
    }

    public static void setUserId(final String userId) {
        put(LoggingConstants.MDC_USER_ID, userId);
    }

    public static String getUserId() {
        return ThreadContext.get(LoggingConstants.MDC_USER_ID);
    }

    public static void setActivityType(final String activityType) {
        put(LoggingConstants.MDC_ACTIVITY_TYPE, activityType);
    }

    public static String getActivityType() {
        return ThreadContext.get(LoggingConstants.MDC_ACTIVITY_TYPE);
    }

    public static void setRequestId(final String requestId) {
        put(LoggingConstants.MDC_REQUEST_ID, requestId);
    }

    private static void put(final String key, final String value) {
        if (value == null) {
            ThreadContext.remove(key);
        } else {
            ThreadContext.put(key, value);
        }
    }

    /**
     * Must be called from a {@code finally} block: the container pools request threads, so a key
     * left behind would be attributed to the next user served by that thread.
     */
    public static void clear() {
        ThreadContext.remove(LoggingConstants.MDC_USER_ID);
        ThreadContext.remove(LoggingConstants.MDC_ACTIVITY_TYPE);
        ThreadContext.remove(LoggingConstants.MDC_REQUEST_ID);
    }
}
