package org.example.am.shared.logging;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Wraps every service and DAO bean so that an exception escaping the business layer is written once
 * to the alert logger, with the bean and method that produced it.
 *
 * <p>Applied by the {@code BeanNameAutoProxyCreator} declared in the web tier's root configuration
 * rather than by an annotation, so that a newly added {@code *ServiceImpl} is covered without the
 * author having to remember to opt in.</p>
 */
@Component("alertLoggingInterceptor")
public class AlertLoggingInterceptor implements MethodInterceptor {

    private static final Logger LOGGER = LogManager.getLogger(AlertLoggingInterceptor.class);
    private static final Logger ALERT_LOGGER = LogManager.getLogger(LoggingConstants.ALERT_LOGGER);

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final String target = invocation.getMethod().getDeclaringClass().getSimpleName()
                + "." + invocation.getMethod().getName();
        final long startedAt = System.currentTimeMillis();
        try {
            final Object result = invocation.proceed();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("{} completed in {} ms", target, System.currentTimeMillis() - startedAt);
            }
            return result;
        } catch (final Throwable failure) {
            // Logged here and rethrown: the caller still decides how to present the failure.
            ALERT_LOGGER.error("Unhandled failure in {} for user {} after {} ms", target,
                    LoggingUtils.getUserId(), System.currentTimeMillis() - startedAt, failure);
            throw failure;
        }
    }
}
