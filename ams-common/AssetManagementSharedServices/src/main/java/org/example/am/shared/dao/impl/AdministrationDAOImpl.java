package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.example.am.shared.dao.AdministrationDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.ETLStatus;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Backs the administration utilities screen: the queue counters operations staff watch and the
 * ETL run status they check after the nightly load.
 */
@Repository("administrationSharedDAO")
public class AdministrationDAOImpl extends BaseDAO implements AdministrationDAO {

    private static final String SELECT_QUEUE_DEPTH =
            "SELECT COUNT(*) FROM AMS_MODIFY_QUEUE Q "
          + " WHERE Q.QUEUE_NAME = :queueName AND Q.QUEUE_STATUS_CD IN ('NEW', 'RETRY') ";

    private static final String SELECT_LAST_ETL_STATUS =
            "SELECT * FROM ( SELECT E.ETL_STATUS_CD FROM AMS_ETL_RUNS E "
          + "                 WHERE E.JOB_NAME = :jobName ORDER BY E.RUN_DT DESC ) WHERE ROWNUM <= 1 ";

    private static final String SELECT_LAST_ETL_DATE =
            "SELECT * FROM ( SELECT E.RUN_DT FROM AMS_ETL_RUNS E "
          + "                 WHERE E.JOB_NAME = :jobName ORDER BY E.RUN_DT DESC ) WHERE ROWNUM <= 1 ";

    /**
     * Entries still unprocessed after the grace period; each is reported by its business key.
     *
     * <p>The cutoff is computed in Java and bound, rather than derived in SQL from the database
     * clock, so that the statement carries no vendor specific interval arithmetic.</p>
     */
    private static final String SELECT_STUCK_ENTRIES =
            "SELECT Q.ENTITY_REFERENCE FROM AMS_MODIFY_QUEUE Q "
          + " WHERE Q.QUEUE_NAME = :queueName "
          + "   AND Q.QUEUE_STATUS_CD IN ('NEW', 'RETRY') "
          + "   AND Q.CREATED_DT < :cutoff "
          + " ORDER BY Q.CREATED_DT ";

    private static final RowMapper<String> STATUS_CODE_MAPPER = new RowMapper<String>() {

        @Override
        public String mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            return ConversionUtils.getString(rs, "ETL_STATUS_CD");
        }
    };

    private static final RowMapper<String> REFERENCE_MAPPER = new RowMapper<String>() {

        @Override
        public String mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            return ConversionUtils.getString(rs, "ENTITY_REFERENCE");
        }
    };

    @Override
    public int getQueueDepth(final String queueName) {
        final Integer depth = getNamedParameterJdbcTemplate().queryForObject(SELECT_QUEUE_DEPTH,
                ParameterRepository.of("queueName", queueName).build(), Integer.class);
        return depth == null ? 0 : depth.intValue();
    }

    @Override
    public ETLStatus getLastEtlStatus(final String jobName) {
        final List<String> rows = getNamedParameterJdbcTemplate().query(SELECT_LAST_ETL_STATUS,
                ParameterRepository.of("jobName", jobName).build(), STATUS_CODE_MAPPER);
        return rows.isEmpty() ? null : ETLStatus.lookup(rows.get(0));
    }

    @Override
    public Date getLastEtlRunDate(final String jobName) {
        final List<Date> rows = getNamedParameterJdbcTemplate().query(SELECT_LAST_ETL_DATE,
                ParameterRepository.of("jobName", jobName).build(), SharedRowMappers.DATE);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<String> getStuckQueueEntries(final String queueName, final int olderThanMinutes) {
        final Date cutoff = new Date(System.currentTimeMillis() - olderThanMinutes * 60L * 1000L);
        return getNamedParameterJdbcTemplate().query(SELECT_STUCK_ENTRIES,
                ParameterRepository.create()
                        .with("queueName", queueName)
                        .withDate("cutoff", cutoff)
                        .build(), REFERENCE_MAPPER);
    }
}
