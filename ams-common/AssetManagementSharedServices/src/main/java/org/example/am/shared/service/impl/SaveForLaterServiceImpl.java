package org.example.am.shared.service.impl;

import java.util.List;
import org.example.am.shared.dao.SaveForLaterDAO;
import org.example.am.shared.domain.SaveForLaterType;
import org.example.am.shared.service.SaveForLaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Parks and restores partially completed forms.
 */
@Service("saveForLaterService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class SaveForLaterServiceImpl implements SaveForLaterService {

    @Autowired
    private SaveForLaterDAO saveForLaterDAO;

    @Override
    public String load(final String userId, final long customerId, final SaveForLaterType type) {
        return saveForLaterDAO.getSavedForm(userId, customerId, type);
    }

    @Override
    public List<Long> getSavedCustomerIds(final String userId, final SaveForLaterType type) {
        return saveForLaterDAO.getSavedFormKeys(userId, type);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(final String userId, final long customerId, final SaveForLaterType type,
            final String payload) {
        saveForLaterDAO.saveForm(userId, customerId, type, payload);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void discard(final String userId, final long customerId, final SaveForLaterType type) {
        saveForLaterDAO.deleteSavedForm(userId, customerId, type);
    }
}
