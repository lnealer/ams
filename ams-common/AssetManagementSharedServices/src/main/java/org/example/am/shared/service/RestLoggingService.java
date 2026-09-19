package org.example.am.shared.service;

/**
 * Records outbound REST traffic for the support team's request tracing.
 */
public interface RestLoggingService {

    void logRequest(String method, String url, String correlationId, String body);

    void logResponse(String method, String url, String correlationId, int statusCode,
            long elapsedMillis, String body);

    void logFailure(String method, String url, String correlationId, long elapsedMillis,
            Throwable failure);
}
