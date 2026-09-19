package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.SaveForLaterDAO;
import org.example.am.shared.domain.SaveForLaterType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("saveForLaterSharedDAO")
public class SaveForLaterDAOImpl extends BaseDAO implements SaveForLaterDAO {

    private static final String SELECT_SAVED_FORM =
            "SELECT S.PAYLOAD "
          + "  FROM AMS_SAVE_FOR_LATER S "
          + " WHERE S.USER_ID = :userId "
          + "   AND S.CUSTOMER_ID = :customerId "
          + "   AND S.SAVE_TYPE_CD = :typeCode ";

    private static final String SELECT_SAVED_KEYS =
            "SELECT S.CUSTOMER_ID "
          + "  FROM AMS_SAVE_FOR_LATER S "
          + " WHERE S.USER_ID = :userId "
          + "   AND S.SAVE_TYPE_CD = :typeCode "
          + " ORDER BY S.MODIFIED_DT DESC ";

    private static final String DELETE_SAVED_FORM =
            "DELETE FROM AMS_SAVE_FOR_LATER "
          + " WHERE USER_ID = :userId AND CUSTOMER_ID = :customerId AND SAVE_TYPE_CD = :typeCode ";

    private static final String INSERT_SAVED_FORM =
            "INSERT INTO AMS_SAVE_FOR_LATER "
          + "       ( SAVE_FOR_LATER_ID, USER_ID, CUSTOMER_ID, SAVE_TYPE_CD, PAYLOAD,"
          + "         CREATED_DT, MODIFIED_DT ) "
          + "VALUES ( " + CommonConstants.SEQ_SAVE_FOR_LATER + ".NEXTVAL, :userId, :customerId,"
          + "         :typeCode, :payload, SYSTIMESTAMP, SYSTIMESTAMP ) ";

    private static final RowMapper<String> PAYLOAD_MAPPER = new RowMapper<String>() {

        @Override
        public String mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            return ConversionUtils.getString(rs, "PAYLOAD");
        }
    };

    private static final RowMapper<Long> CUSTOMER_ID_MAPPER = new RowMapper<Long>() {

        @Override
        public Long mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            return ConversionUtils.getLong(rs, "CUSTOMER_ID");
        }
    };

    @Override
    public String getSavedForm(final String userId, final long customerId, final SaveForLaterType type) {
        final List<String> rows = getNamedParameterJdbcTemplate().query(SELECT_SAVED_FORM,
                key(userId, customerId, type).build(), PAYLOAD_MAPPER);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<Long> getSavedFormKeys(final String userId, final SaveForLaterType type) {
        return getNamedParameterJdbcTemplate().query(SELECT_SAVED_KEYS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_TYPE_CODE, type == null ? null : type.getCode())
                        .build(), CUSTOMER_ID_MAPPER);
    }

    /**
     * Delete-then-insert rather than a merge: there is at most one saved form per key, and this
     * keeps the statement portable across the environments the integration tests run in.
     */
    @Override
    public int saveForm(final String userId, final long customerId, final SaveForLaterType type,
            final String payload) {
        deleteSavedForm(userId, customerId, type);
        return getNamedParameterJdbcTemplate().update(INSERT_SAVED_FORM,
                key(userId, customerId, type).with("payload", payload).build());
    }

    @Override
    public int deleteSavedForm(final String userId, final long customerId, final SaveForLaterType type) {
        return getNamedParameterJdbcTemplate().update(DELETE_SAVED_FORM,
                key(userId, customerId, type).build());
    }

    private static ParameterRepository key(final String userId, final long customerId,
            final SaveForLaterType type) {
        return ParameterRepository.create()
                .with(CommonConstants.PARAM_USER_ID, userId)
                .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                .with(CommonConstants.PARAM_TYPE_CODE, type == null ? null : type.getCode());
    }
}
