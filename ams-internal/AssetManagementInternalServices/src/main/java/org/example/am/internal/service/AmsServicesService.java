package org.example.am.internal.service;

import java.util.List;

import org.example.am.shared.domain.AmsService;

/** The internal services screen: what a customer subscribes to and how much hardware it carries. */
public interface AmsServicesService {

    List<AmsService> getServices(long customerId);

    int getAssetCount(long serviceId);

    /** @return {@code true} when the status was changed */
    boolean updateStatus(long serviceId, String statusCode, String userId);
}
