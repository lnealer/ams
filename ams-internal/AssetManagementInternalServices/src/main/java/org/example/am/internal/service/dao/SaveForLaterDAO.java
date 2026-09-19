package org.example.am.internal.service.dao;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.SaveForLaterType;

/**
 * Internal-only view of the saved forms: an operator needs to see what they parked and when, not
 * just be able to reload it.
 */
public interface SaveForLaterDAO {

    /** @return the customer ids and save times for this operator's parked forms of the given type */
    List<Long> getSavedCustomerIds(String userId, SaveForLaterType type);

    Date getSavedDate(String userId, long customerId, SaveForLaterType type);

    int deleteExpired(int olderThanDays);
}
