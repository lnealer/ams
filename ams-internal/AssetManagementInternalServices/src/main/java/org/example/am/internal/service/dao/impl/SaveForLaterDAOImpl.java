package org.example.am.internal.service.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.example.am.internal.service.dao.SaveForLaterDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.SaveForLaterType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("internalSaveForLaterDAO")
public class SaveForLaterDAOImpl extends BaseDAO implements SaveForLaterDAO {

    private static final String SELECT_CUSTOMER_IDS =
            "SELECT S.CUSTOMER_ID FROM AMS_SAVE_FOR_LATER S "
          + " WHERE S.USER_ID = :userId AND S.SAVE_TYPE_CD = :typeCode "
          + " ORDER BY S.MODIFIED_DT DESC ";

    private static final String SELECT_SAVED_DATE =
            "SELECT S.MODIFIED_DT FROM AMS_SAVE_FOR_LATER S "
          + " WHERE S.USER_ID = :userId AND S.CUSTOMER_ID = :customerId AND S.SAVE_TYPE_CD = :typeCode ";

    /**
     * The cutoff is bound rather than derived from the database clock, so the statement stays free
     * of vendor specific interval arithmetic.
     */
    private static final String DELETE_EXPIRED =
            "DELETE FROM AMS_SAVE_FOR_LATER WHERE MODIFIED_DT < :cutoff ";

    private static final RowMapper<Long> CUSTOMER_ID_MAPPER = new RowMapper<Long>() {

        @Override
        public Long mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            return ConversionUtils.getLong(rs, "CUSTOMER_ID");
        }
    };

    private static final RowMapper<Date> DATE_MAPPER = new RowMapper<Date>() {

        @Override
        public Date mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            return ConversionUtils.getDate(rs, "MODIFIED_DT");
        }
    };

    @Override
    public List<Long> getSavedCustomerIds(final String userId, final SaveForLaterType type) {
        return getNamedParameterJdbcTemplate().query(SELECT_CUSTOMER_IDS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_TYPE_CODE, type == null ? null : type.getCode())
                        .build(), CUSTOMER_ID_MAPPER);
    }

    @Override
    public Date getSavedDate(final String userId, final long customerId, final SaveForLaterType type) {
        final List<Date> rows = getNamedParameterJdbcTemplate().query(SELECT_SAVED_DATE,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .with(CommonConstants.PARAM_TYPE_CODE, type == null ? null : type.getCode())
                        .build(), DATE_MAPPER);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public int deleteExpired(final int olderThanDays) {
        final Date cutoff = ConversionUtils.addDays(new Date(), -olderThanDays);
        return getNamedParameterJdbcTemplate().update(DELETE_EXPIRED,
                ParameterRepository.create().withDate("cutoff", cutoff).build());
    }
}
