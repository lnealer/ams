package org.example.am.shared.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.example.am.shared.dao.AdministrationDAO;
import org.example.am.shared.dao.ConfigDAO;
import org.example.am.shared.domain.ETLStatus;
import org.example.am.shared.service.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Backs the administration utilities screen.
 */
@Service("administrationService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AdministrationServiceImpl implements AdministrationService {

    @Autowired
    private AdministrationDAO administrationDAO;

    @Autowired
    private ConfigDAO configDAO;

    @Override
    public Map<String, Integer> getQueueDepths(final List<String> queueNames) {
        final Map<String, Integer> depths = new LinkedHashMap<String, Integer>();
        for (final String queueName : queueNames) {
            depths.put(queueName, Integer.valueOf(administrationDAO.getQueueDepth(queueName)));
        }
        return depths;
    }

    @Override
    public ETLStatus getLastEtlStatus(final String jobName) {
        return administrationDAO.getLastEtlStatus(jobName);
    }

    @Override
    public List<String> getStuckEntries(final String queueName, final int olderThanMinutes) {
        return administrationDAO.getStuckQueueEntries(queueName, olderThanMinutes);
    }

    @Override
    public Map<String, String> getAllProperties() {
        return configDAO.getAllProperties();
    }
}
