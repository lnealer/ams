package org.example.am.shared.service.impl;

import org.example.am.shared.logging.LoggingConstants;
import org.example.am.shared.logging.LoggingUtils;
import org.example.am.shared.service.RestLoggingService;
import org.example.am.shared.utils.RestLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Stamps the correlation id onto the diagnostic context, then delegates the actual writing to
 * {@link RestLogger} so that the log format lives in one place.
 */
@Service("restLoggingService")
public class RestLoggingServiceImpl implements RestLoggingService {

    @Autowired
    private RestLogger restLogger;

    @Override
    public void logRequest(final String method, final String url, final String correlationId,
            final String body) {
        LoggingUtils.setRequestId(correlationId);
        LoggingUtils.setActivityType(LoggingConstants.ACTIVITY_REST);
        restLogger.logRequest(method, url, body);
    }

    @Override
    public void logResponse(final String method, final String url, final String correlationId,
            final int statusCode, final long elapsedMillis, final String body) {
        LoggingUtils.setRequestId(correlationId);
        restLogger.logResponse(method, url, statusCode, elapsedMillis, body);
    }

    @Override
    public void logFailure(final String method, final String url, final String correlationId,
            final long elapsedMillis, final Throwable failure) {
        LoggingUtils.setRequestId(correlationId);
        restLogger.logFailure(method, url, elapsedMillis, failure);
    }

    public void setRestLogger(final RestLogger restLogger) {
        this.restLogger = restLogger;
    }
}
