package org.example.am.shared.service;

import java.util.List;

import org.example.am.shared.domain.NetworkChangeRequest;

/** Network change request reads, submission and cancellation. */
public interface NetworkChangeRequestService {

    NetworkChangeRequest getNetworkChangeRequest(long networkChangeRequestId);

    List<NetworkChangeRequest> getHistoryForAsset(long assetId);

    List<NetworkChangeRequest> getOpenRequests(long customerId);

    /**
     * @throws IllegalStateException when the request is not complete enough to submit
     * @return the generated request id
     */
    long submit(NetworkChangeRequest request, String userId);

    /**
     * Cancels the request and releases whatever calendar capacity it held, routing to the simple or
     * the complex procedure according to the change types.
     *
     * @return {@code true} when the request was cancelled
     */
    boolean cancel(long networkChangeRequestId, String reason, String userId);
}
