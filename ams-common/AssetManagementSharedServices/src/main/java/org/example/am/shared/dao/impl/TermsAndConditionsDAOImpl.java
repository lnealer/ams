package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.TermsAndConditionsDAO;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Tracks which version of the terms and conditions each user has accepted.
 */
@Repository("termsAndConditionsSharedDAO")
public class TermsAndConditionsDAOImpl extends BaseDAO implements TermsAndConditionsDAO {

    private static final String SELECT_CURRENT_VERSION =
            "SELECT * FROM ( SELECT T.VERSION FROM AMS_TERMS_AND_CONDITIONS T "
          + "                 WHERE T.EFFECTIVE_DT <= SYSTIMESTAMP "
          + "                 ORDER BY T.EFFECTIVE_DT DESC ) WHERE ROWNUM <= 1 ";

    private static final String SELECT_ACCEPTED =
            "SELECT COUNT(*) FROM AMS_TERMS_ACCEPTANCES A "
          + " WHERE A.USER_ID = :userId AND A.VERSION = :version ";

    private static final String INSERT_ACCEPTANCE =
            "INSERT INTO AMS_TERMS_ACCEPTANCES ( USER_ID, VERSION, ACCEPTED_DT ) "
          + "VALUES ( :userId, :version, SYSTIMESTAMP ) ";

    private static final RowMapper<String> VERSION_MAPPER = new RowMapper<String>() {

        @Override
        public String mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            return ConversionUtils.getString(rs, "VERSION");
        }
    };

    @Override
    public String getCurrentVersion() {
        final List<String> rows = getNamedParameterJdbcTemplate().query(SELECT_CURRENT_VERSION,
                ParameterRepository.create().build(), VERSION_MAPPER);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public boolean hasAccepted(final String userId, final String version) {
        final Integer count = getNamedParameterJdbcTemplate().queryForObject(SELECT_ACCEPTED,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with("version", version)
                        .build(), Integer.class);
        return count != null && count.intValue() > 0;
    }

    @Override
    public int recordAcceptance(final String userId, final String version) {
        return getNamedParameterJdbcTemplate().update(INSERT_ACCEPTANCE,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with("version", version)
                        .build());
    }
}
