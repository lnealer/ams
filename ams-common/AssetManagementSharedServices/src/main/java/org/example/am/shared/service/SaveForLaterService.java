package org.example.am.shared.service;

import java.util.List;
import org.example.am.shared.domain.SaveForLaterType;

/**
 * Parks and restores partially completed forms.
 */
public interface SaveForLaterService {

    String load(String userId, long customerId, SaveForLaterType type);

    List<Long> getSavedCustomerIds(String userId, SaveForLaterType type);

    void save(String userId, long customerId, SaveForLaterType type, String payload);

    void discard(String userId, long customerId, SaveForLaterType type);
}
