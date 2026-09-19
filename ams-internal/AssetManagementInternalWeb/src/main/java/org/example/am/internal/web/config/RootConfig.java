package org.example.am.internal.web.config;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;

/**
 * The root application context: everything that is not a Spring MVC handler.
 *
 * <p>Loaded by {@code ContextLoaderListener}, so it is the parent of the {@code DispatcherServlet}
 * context and is also what the Struts Spring plugin resolves action beans from. Controllers are
 * excluded here and picked up by {@link ServletConfig} instead, so that a controller is not created
 * twice in two contexts.</p>
 */
@Configuration
@ComponentScan(basePackages = "org.example.am",
        excludeFilters = {
                @Filter(type = FilterType.ANNOTATION, classes = Controller.class),
                @Filter(type = FilterType.ANNOTATION, classes = Configuration.class) })
@Import({ DataSourceConfig.class, GlobalSecurityConfig.class, WebSecurityConfig.class,
        RestConfig.class })
public class RootConfig {

    /** Bean name patterns wrapped with the alert logging interceptor. */
    private static final String[] ALERT_LOGGED_BEAN_NAMES =
            { "*Service", "*DAO", "*Impl", "*Initializer" };

    /**
     * Wraps the business layer so that an exception escaping it is written once to the alert
     * logger.
     *
     * <p>Matched by bean name rather than by annotation deliberately: a newly added
     * {@code *ServiceImpl} is covered without its author having to remember to opt in, which is the
     * whole point of having an infrastructure alert channel. The interceptor itself is the
     * {@code alertLoggingInterceptor} component in the shared library.</p>
     */
    @Bean
    public BeanNameAutoProxyCreator alertLoggingAutoProxyCreator() {
        final BeanNameAutoProxyCreator proxyCreator = new BeanNameAutoProxyCreator();
        proxyCreator.setBeanNames(ALERT_LOGGED_BEAN_NAMES);
        proxyCreator.setInterceptorNames("alertLoggingInterceptor");
        // Proxy the class rather than its interfaces: several DAOs are injected by concrete type.
        proxyCreator.setProxyTargetClass(true);
        return proxyCreator;
    }

    /**
     * @return the message source backing both the JSP tags and {@code BaseAction.getText}
     */
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        final ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages", "classpath:appVersion");
        messageSource.setDefaultEncoding("UTF-8");
        // A missing key should show as the key, not blow up the page it appears on.
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }
}
