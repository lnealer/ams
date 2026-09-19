package org.example.am.shared.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.am.shared.logging.LoggingConstants;
import org.example.am.shared.logging.LoggingUtils;

/**
 * Records outbound REST traffic. Bodies are logged at debug only, and the logger deliberately never
 * echoes request headers so that credentials on an outbound call cannot reach the log file.
 */
public class RestLogger {

    private static final Logger LOGGER = LogManager.getLogger(RestLogger.class);
    private static final Logger ALERT_LOGGER = LogManager.getLogger(LoggingConstants.ALERT_LOGGER);

    private static final int MAX_LOGGED_BODY_LENGTH = 4000;

    public void logRequest(final String method, final String url, final String body) {
        LoggingUtils.setActivityType(LoggingConstants.ACTIVITY_REST);
        LOGGER.info("--> {} {}", method, url);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("--> body {}", truncate(body));
        }
    }

    public void logResponse(final String method, final String url, final int statusCode,
            final long elapsedMillis, final String body) {
        LOGGER.info("<-- {} {} {} ({} ms)", method, url, statusCode, elapsedMillis);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<-- body {}", truncate(body));
        }
    }

    public void logFailure(final String method, final String url, final long elapsedMillis,
            final Throwable failure) {
        ALERT_LOGGER.error("Outbound call {} {} failed after {} ms", method, url, elapsedMillis,
                failure);
    }

    private static String truncate(final String body) {
        if (body == null) {
            return null;
        }
        if (body.length() <= MAX_LOGGED_BODY_LENGTH) {
            return body;
        }
        return body.substring(0, MAX_LOGGED_BODY_LENGTH) + "...[truncated]";
    }
}
