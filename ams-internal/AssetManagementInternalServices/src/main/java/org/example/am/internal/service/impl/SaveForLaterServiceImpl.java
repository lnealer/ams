package org.example.am.internal.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.am.internal.service.SaveForLaterService;
import org.example.am.internal.service.dao.SaveForLaterDAO;
import org.example.am.shared.domain.SaveForLaterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("internalSaveForLaterService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class SaveForLaterServiceImpl implements SaveForLaterService {

    private static final Logger LOGGER = LogManager.getLogger(SaveForLaterServiceImpl.class);

    /** Guard against a mistyped retention period wiping every parked form. */
    private static final int MINIMUM_RETENTION_DAYS = 7;

    @Autowired
    private SaveForLaterDAO internalSaveForLaterDAO;

    @Override
    public List<Long> getSavedCustomerIds(final String userId, final SaveForLaterType type) {
        return internalSaveForLaterDAO.getSavedCustomerIds(userId, type);
    }

    @Override
    public Date getSavedDate(final String userId, final long customerId, final SaveForLaterType type) {
        return internalSaveForLaterDAO.getSavedDate(userId, customerId, type);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int purgeExpired(final int olderThanDays) {
        if (olderThanDays < MINIMUM_RETENTION_DAYS) {
            throw new IllegalArgumentException(
                    "Refusing to purge saved forms younger than " + MINIMUM_RETENTION_DAYS + " days");
        }
        final int removed = internalSaveForLaterDAO.deleteExpired(olderThanDays);
        LOGGER.info("Purged {} saved forms older than {} days", Integer.valueOf(removed),
                Integer.valueOf(olderThanDays));
        return removed;
    }
}
