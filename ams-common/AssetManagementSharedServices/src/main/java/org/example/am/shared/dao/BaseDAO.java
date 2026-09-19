package org.example.am.shared.dao;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Holds the one piece of state every AMS DAO needs.
 *
 * <p>There is no ORM anywhere in this application: each DAO owns its SQL as string constants and
 * maps rows with a hand written {@link org.springframework.jdbc.core.RowMapper}. The template is
 * built from the container managed {@link DataSource} looked up over JNDI, so transactions are
 * driven by {@code DataSourceTransactionManager} and participate in whatever the calling
 * {@code @Transactional} service started.</p>
 */
public abstract class BaseDAO {

    protected final Logger logger = LogManager.getLogger(getClass());

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private DataSource dataSource;

    /**
     * Setter injection rather than constructor injection, because the legacy DAOs are also
     * instantiated directly in a handful of unit tests.
     */
    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    protected DataSource getDataSource() {
        return dataSource;
    }
}
