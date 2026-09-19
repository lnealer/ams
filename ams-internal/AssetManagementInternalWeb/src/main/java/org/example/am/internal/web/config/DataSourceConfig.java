package org.example.am.internal.web.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The data source and the transaction manager.
 *
 * <p>The pool is the container's, looked up over JNDI, so connection sizing, validation and
 * failover are configured in the Liberty {@code server.xml} rather than in the application. There
 * is no ORM, so the transaction manager is the plain JDBC one: a {@code @Transactional} service
 * method and the {@code NamedParameterJdbcTemplate} calls underneath it share one connection and
 * one transaction.</p>
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    /** Matches the {@code jndiName} on the Liberty {@code dataSource} element. */
    public static final String DATA_SOURCE_JNDI_NAME = "jdbc/amsInternalDS";

    @Bean
    public DataSource dataSource() {
        final JndiDataSourceLookup lookup = new JndiDataSourceLookup();
        // Prefixes the name with java:comp/env, which is how the resource-ref in web.xml exposes it.
        lookup.setResourceRef(true);
        return lookup.getDataSource(DATA_SOURCE_JNDI_NAME);
    }

    @Bean
    public PlatformTransactionManager transactionManager(final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
