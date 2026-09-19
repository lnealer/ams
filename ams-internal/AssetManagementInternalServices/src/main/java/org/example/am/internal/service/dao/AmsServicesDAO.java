package org.example.am.internal.service.dao;

import java.util.List;

import org.example.am.shared.domain.AmsService;

/** The internal view of customer service subscriptions, including the asset counts per service. */
public interface AmsServicesDAO {

    List<AmsService> getServices(long customerId);

    /** @return the number of installed assets carried by this service */
    int getAssetCountForService(long serviceId);

    int updateServiceStatus(long serviceId, String statusCode, String userId);
}
