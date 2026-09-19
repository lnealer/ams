package org.example.am.shared.dao;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.domain.NetworkChangeRequestStatusType;

/** Reads and writes {@code AMS_NETWORK_CHANGE_REQUESTS} and its type child table. */
public interface NetworkChangeRequestDAO {

    NetworkChangeRequest getNetworkChangeRequest(long networkChangeRequestId);

    List<NetworkChangeRequest> getRequestsForAsset(long assetId);

    List<NetworkChangeRequest> getOpenRequestsForCustomer(long customerId);

    long insertNetworkChangeRequest(NetworkChangeRequest request, String userId);

    int updateStatus(long networkChangeRequestId, NetworkChangeRequestStatusType status, String userId);

    int cancelRequest(long networkChangeRequestId, String reason, Date cancelledDate, String userId);
}
