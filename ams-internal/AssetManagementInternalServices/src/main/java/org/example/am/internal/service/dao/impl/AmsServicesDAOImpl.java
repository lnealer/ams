package org.example.am.internal.service.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.example.am.internal.service.dao.AmsServicesDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.AmsService;
import org.example.am.shared.domain.ServiceStatusType;
import org.example.am.shared.domain.ServiceType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("amsServicesDAO")
public class AmsServicesDAOImpl extends BaseDAO implements AmsServicesDAO {

    private static final String SELECT_SERVICES =
            "SELECT S.SERVICE_ID, S.CUSTOMER_ID, S.SERVICE_TYPE_CD, S.SERVICE_STATUS_CD,"
          + "       S.START_DT, S.END_DT, S.DESCRIPTION, S.EXTERNAL_SERVICE_REF "
          + "  FROM AMS_SERVICES S "
          + " WHERE S.CUSTOMER_ID = :customerId "
          + " ORDER BY S.SERVICE_TYPE_CD ";

    /**
     * Legacy Oracle outer join syntax, kept verbatim from the report this projection was lifted
     * from. Oracle only - the integration tests exercise the ANSI statements around it.
     */
    private static final String SELECT_ASSET_COUNT_FOR_SERVICE =
            "SELECT COUNT(A.ASSET_ID) "
          + "  FROM AMS_SERVICES S, AMS_ASSETS A "
          + " WHERE S.SERVICE_ID = :serviceId "
          + "   AND A.CUSTOMER_ID (+) = S.CUSTOMER_ID "
          + "   AND A.ASSET_STATUS_CD IN ('INSTALLED', 'ACTIVE') ";

    private static final String UPDATE_SERVICE_STATUS =
            "UPDATE AMS_SERVICES SET SERVICE_STATUS_CD = :statusCode,"
          + "       MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId WHERE SERVICE_ID = :serviceId ";

    private static final RowMapper<AmsService> SERVICE_MAPPER = new ServiceMapper();

    @Override
    public List<AmsService> getServices(final long customerId) {
        return getNamedParameterJdbcTemplate().query(SELECT_SERVICES,
                ParameterRepository.of(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .build(), SERVICE_MAPPER);
    }

    @Override
    public int getAssetCountForService(final long serviceId) {
        final Integer count = getNamedParameterJdbcTemplate().queryForObject(
                SELECT_ASSET_COUNT_FOR_SERVICE,
                ParameterRepository.of("serviceId", Long.valueOf(serviceId)).build(), Integer.class);
        return count == null ? 0 : count.intValue();
    }

    @Override
    public int updateServiceStatus(final long serviceId, final String statusCode, final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_SERVICE_STATUS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_STATUS_CODE, statusCode)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with("serviceId", Long.valueOf(serviceId))
                        .build());
    }

    private static class ServiceMapper implements RowMapper<AmsService> {

        @Override
        public AmsService mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final AmsService service = new AmsService();
            service.setServiceId(ConversionUtils.getLong(rs, "SERVICE_ID"));
            service.setCustomerId(ConversionUtils.getLong(rs, "CUSTOMER_ID"));
            service.setServiceType(ServiceType.lookup(ConversionUtils.getString(rs, "SERVICE_TYPE_CD")));
            service.setServiceStatusType(
                    ServiceStatusType.lookup(ConversionUtils.getString(rs, "SERVICE_STATUS_CD")));
            service.setStartDate(ConversionUtils.getDate(rs, "START_DT"));
            service.setEndDate(ConversionUtils.getDate(rs, "END_DT"));
            service.setDescription(ConversionUtils.getString(rs, "DESCRIPTION"));
            service.setExternalServiceReference(ConversionUtils.getString(rs, "EXTERNAL_SERVICE_REF"));
            return service;
        }
    }
}
