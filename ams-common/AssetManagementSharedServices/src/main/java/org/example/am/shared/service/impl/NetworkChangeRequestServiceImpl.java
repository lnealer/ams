package org.example.am.shared.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.am.shared.dao.NetworkChangeRequestDAO;
import org.example.am.shared.dao.RequestDAO;
import org.example.am.shared.dao.StoredProcedureDAO;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.EmailTemplateType;
import org.example.am.shared.domain.EventType;
import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.domain.NetworkChangeRequestStatusType;
import org.example.am.shared.service.NetworkChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("networkChangeRequestService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class NetworkChangeRequestServiceImpl implements NetworkChangeRequestService {

    private static final Logger LOGGER =
            LogManager.getLogger(NetworkChangeRequestServiceImpl.class);

    @Autowired
    private NetworkChangeRequestDAO networkChangeRequestDAO;

    @Autowired
    private StoredProcedureDAO storedProcedureDAO;

    @Autowired
    private RequestDAO requestDAO;

    @Override
    public NetworkChangeRequest getNetworkChangeRequest(final long networkChangeRequestId) {
        return networkChangeRequestDAO.getNetworkChangeRequest(networkChangeRequestId);
    }

    @Override
    public List<NetworkChangeRequest> getHistoryForAsset(final long assetId) {
        return networkChangeRequestDAO.getRequestsForAsset(assetId);
    }

    @Override
    public List<NetworkChangeRequest> getOpenRequests(final long customerId) {
        return networkChangeRequestDAO.getOpenRequestsForCustomer(customerId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long submit(final NetworkChangeRequest request, final String userId) {
        if (!request.isSubmittable()) {
            throw new IllegalStateException("Network change request is not complete: "
                    + request.getChangeTypeDescription());
        }
        request.setNetworkChangeRequestStatusType(NetworkChangeRequestStatusType.SUBMITTED);
        if (request.getSubmittedDate() == null) {
            request.setSubmittedDate(request.getCurrentTime());
        }
        final long ncrId = networkChangeRequestDAO.insertNetworkChangeRequest(request, userId);

        requestDAO.recordEvent(EventType.NCR_SUBMITTED, EmailEntityType.NETWORK_CHANGE_REQUEST,
                ncrId, "Network change request submitted: " + request.getChangeTypeDescription(),
                userId);
        storedProcedureDAO.addEntityEmail(EmailEntityType.NETWORK_CHANGE_REQUEST.getCode(), ncrId,
                EmailTemplateType.NCR_CONFIRMATION.getCode(), userId);
        return ncrId;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public boolean cancel(final long networkChangeRequestId, final String reason, final String userId) {
        final NetworkChangeRequest request =
                networkChangeRequestDAO.getNetworkChangeRequest(networkChangeRequestId);
        if (request == null) {
            throw new IllegalArgumentException("No network change request " + networkChangeRequestId);
        }
        if (!request.isCancellable()) {
            LOGGER.info("Network change request {} is not cancellable in status {}",
                    Long.valueOf(networkChangeRequestId),
                    request.getNetworkChangeRequestStatusType());
            return false;
        }

        // A site type change holds a circuit reservation as well as a device slot, so the two
        // cancellations are different procedures rather than one with a flag.
        storedProcedureDAO.cancelNetworkChangeRequestDate(networkChangeRequestId,
                request.getAssetId() == null ? 0L : request.getAssetId().longValue(), reason,
                request.isComplexScheduling(), userId);

        final int updated = networkChangeRequestDAO.cancelRequest(networkChangeRequestId, reason,
                request.getCurrentTime(), userId);
        if (updated == 0) {
            return false;
        }
        requestDAO.recordEvent(EventType.NCR_CANCELLED, EmailEntityType.NETWORK_CHANGE_REQUEST,
                networkChangeRequestId, "Network change request cancelled: " + reason, userId);
        storedProcedureDAO.addEntityEmail(EmailEntityType.NETWORK_CHANGE_REQUEST.getCode(),
                networkChangeRequestId, EmailTemplateType.NCR_CANCELLATION.getCode(), userId);
        return true;
    }
}
