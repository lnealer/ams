package org.example.am.shared.dao.impl;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.NetworkChangeRequestDAO;
import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.domain.NetworkChangeRequestStatusType;
import org.example.am.shared.domain.NetworkChangeRequestType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.springframework.stereotype.Repository;

@Repository("networkChangeRequestSharedDAO")
public class NetworkChangeRequestDAOImpl extends BaseDAO implements NetworkChangeRequestDAO {

    private static final String NCR_COLUMNS =
            "  N.NCR_ID, N.REQUEST_NUMBER, N.ASSET_ID, N.CUSTOMER_ID, N.NCR_STATUS_CD,"
          + "  N.REQUESTED_DT, N.SUBMITTED_DT, N.SCHEDULED_DT, N.COMPLETED_DT, N.CANCELLED_DT,"
          + "  N.CANCELLATION_REASON, N.COMMENTS, N.CREATED_DT, N.MODIFIED_DT ";

    private static final String SELECT_NCR =
            "SELECT " + NCR_COLUMNS
          + "  FROM AMS_NETWORK_CHANGE_REQUESTS N WHERE N.NCR_ID = :networkChangeRequestId ";

    private static final String SELECT_NCR_TYPES =
            "SELECT T.NCR_TYPE_CD "
          + "  FROM AMS_NCR_TYPES T "
          + " WHERE T.NCR_ID = :networkChangeRequestId "
          + " ORDER BY T.NCR_TYPE_CD ";

    private static final String SELECT_FOR_ASSET =
            "SELECT " + NCR_COLUMNS
          + "  FROM AMS_NETWORK_CHANGE_REQUESTS N "
          + " WHERE N.ASSET_ID = :assetId "
          + " ORDER BY N.REQUESTED_DT DESC, N.NCR_ID DESC ";

    private static final String SELECT_OPEN_FOR_CUSTOMER =
            "SELECT " + NCR_COLUMNS
          + "  FROM AMS_NETWORK_CHANGE_REQUESTS N "
          + " WHERE N.CUSTOMER_ID = :customerId "
          + "   AND N.NCR_STATUS_CD NOT IN ('COMPLETED', 'CANCELLED') "
          + " ORDER BY N.REQUESTED_DT DESC, N.NCR_ID DESC ";

    private static final String NEXT_NCR_ID = "SELECT AMS_NCR_SQ.NEXTVAL FROM DUAL ";

    private static final String INSERT_NCR =
            "INSERT INTO AMS_NETWORK_CHANGE_REQUESTS "
          + "       ( NCR_ID, REQUEST_NUMBER, ASSET_ID, CUSTOMER_ID, NCR_STATUS_CD, REQUESTED_DT,"
          + "         SUBMITTED_DT, COMMENTS, CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :networkChangeRequestId, :requestNumber, :assetId, :customerId, :statusCode,"
          + "         :requestedDate, :submittedDate, :comments,"
          + "         SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final String INSERT_NCR_TYPE =
            "INSERT INTO AMS_NCR_TYPES ( NCR_ID, NCR_TYPE_CD ) "
          + "VALUES ( :networkChangeRequestId, :typeCode ) ";

    private static final String UPDATE_STATUS =
            "UPDATE AMS_NETWORK_CHANGE_REQUESTS "
          + "   SET NCR_STATUS_CD = :statusCode, MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE NCR_ID = :networkChangeRequestId ";

    private static final String CANCEL_NCR =
            "UPDATE AMS_NETWORK_CHANGE_REQUESTS "
          + "   SET NCR_STATUS_CD = 'CANCELLED', CANCELLED_DT = :cancelledDate,"
          + "       CANCELLATION_REASON = :reason, MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE NCR_ID = :networkChangeRequestId "
          + "   AND NCR_STATUS_CD NOT IN ('COMPLETED', 'CANCELLED', 'INPROG') ";

    @Override
    public NetworkChangeRequest getNetworkChangeRequest(final long networkChangeRequestId) {
        final List<NetworkChangeRequest> rows = getNamedParameterJdbcTemplate().query(SELECT_NCR,
                ParameterRepository.of(CommonConstants.PARAM_NCR_ID,
                        Long.valueOf(networkChangeRequestId)).build(),
                SharedRowMappers.NETWORK_CHANGE_REQUEST);
        if (rows.isEmpty()) {
            return null;
        }
        final NetworkChangeRequest request = rows.get(0);
        request.setNetworkChangeRequestTypes(new LinkedHashSet<NetworkChangeRequestType>(
                getNamedParameterJdbcTemplate().query(SELECT_NCR_TYPES,
                        ParameterRepository.of(CommonConstants.PARAM_NCR_ID,
                                Long.valueOf(networkChangeRequestId)).build(),
                        SharedRowMappers.NETWORK_CHANGE_REQUEST_TYPE)));
        return request;
    }

    @Override
    public List<NetworkChangeRequest> getRequestsForAsset(final long assetId) {
        return getNamedParameterJdbcTemplate().query(SELECT_FOR_ASSET,
                ParameterRepository.of(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId)).build(),
                SharedRowMappers.NETWORK_CHANGE_REQUEST);
    }

    @Override
    public List<NetworkChangeRequest> getOpenRequestsForCustomer(final long customerId) {
        return getNamedParameterJdbcTemplate().query(SELECT_OPEN_FOR_CUSTOMER,
                ParameterRepository.of(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .build(), SharedRowMappers.NETWORK_CHANGE_REQUEST);
    }

    @Override
    public long insertNetworkChangeRequest(final NetworkChangeRequest request, final String userId) {
        final Long ncrId = getNamedParameterJdbcTemplate().queryForObject(NEXT_NCR_ID,
                ParameterRepository.create().build(), Long.class);
        getNamedParameterJdbcTemplate().update(INSERT_NCR,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_NCR_ID, ncrId)
                        .with("requestNumber", request.getRequestNumber())
                        .with(CommonConstants.PARAM_ASSET_ID, request.getAssetId())
                        .with(CommonConstants.PARAM_CUSTOMER_ID, request.getCustomerId())
                        .with(CommonConstants.PARAM_STATUS_CODE,
                                request.getNetworkChangeRequestStatusType() == null ? null
                                        : request.getNetworkChangeRequestStatusType().getCode())
                        .withDate("requestedDate", request.getRequestedDate())
                        .withDate("submittedDate", request.getSubmittedDate())
                        .with("comments", request.getComments())
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .build());

        // The change types are a child table: one row per selected type, inserted in the same
        // transaction so a request can never be persisted without its types.
        for (final NetworkChangeRequestType type : request.getNetworkChangeRequestTypes()) {
            getNamedParameterJdbcTemplate().update(INSERT_NCR_TYPE,
                    ParameterRepository.create()
                            .with(CommonConstants.PARAM_NCR_ID, ncrId)
                            .with(CommonConstants.PARAM_TYPE_CODE, type.getCode())
                            .build());
        }
        request.setNetworkChangeRequestId(ncrId);
        return ncrId.longValue();
    }

    @Override
    public int updateStatus(final long networkChangeRequestId,
            final NetworkChangeRequestStatusType status, final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_STATUS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_STATUS_CODE, status == null ? null : status.getCode())
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_NCR_ID, Long.valueOf(networkChangeRequestId))
                        .build());
    }

    @Override
    public int cancelRequest(final long networkChangeRequestId, final String reason,
            final Date cancelledDate, final String userId) {
        return getNamedParameterJdbcTemplate().update(CANCEL_NCR,
                ParameterRepository.create()
                        .withDate("cancelledDate", cancelledDate == null ? new Date() : cancelledDate)
                        .with("reason", reason)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_NCR_ID, Long.valueOf(networkChangeRequestId))
                        .build());
    }
}
