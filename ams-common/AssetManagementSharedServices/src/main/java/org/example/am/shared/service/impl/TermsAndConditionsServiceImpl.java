package org.example.am.shared.service.impl;

import org.example.am.shared.dao.TermsAndConditionsDAO;
import org.example.am.shared.service.TermsAndConditionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gates the application on acceptance of the current terms and conditions.
 */
@Service("termsAndConditionsService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TermsAndConditionsServiceImpl implements TermsAndConditionsService {

    @Autowired
    private TermsAndConditionsDAO termsAndConditionsDAO;

    @Override
    public String getCurrentVersion() {
        return termsAndConditionsDAO.getCurrentVersion();
    }

    @Override
    public boolean isAcceptanceRequired(final String userId) {
        final String version = termsAndConditionsDAO.getCurrentVersion();
        if (version == null) {
            return false;
        }
        return !termsAndConditionsDAO.hasAccepted(userId, version);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void accept(final String userId) {
        final String version = termsAndConditionsDAO.getCurrentVersion();
        if (version != null && !termsAndConditionsDAO.hasAccepted(userId, version)) {
            termsAndConditionsDAO.recordAcceptance(userId, version);
        }
    }
}
