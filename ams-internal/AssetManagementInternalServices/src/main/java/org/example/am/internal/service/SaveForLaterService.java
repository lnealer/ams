package org.example.am.internal.service;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.SaveForLaterType;

/** Internal view of an operator's parked forms. */
public interface SaveForLaterService {

    List<Long> getSavedCustomerIds(String userId, SaveForLaterType type);

    Date getSavedDate(String userId, long customerId, SaveForLaterType type);

    /**
     * Removes parked forms nobody has come back to.
     *
     * @return the number of rows removed
     */
    int purgeExpired(int olderThanDays);
}
