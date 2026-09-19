package org.example.am.shared.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import org.example.am.shared.dao.PreLoadServiceDAO;
import org.example.am.shared.service.PreLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads the reference data cached for the life of the application context.
 */
@Service("preLoadService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class PreLoadServiceImpl implements PreLoadService {

    @Autowired
    private PreLoadServiceDAO preLoadServiceDAO;

    @Override
    public Map<String, Long> getLoadableTypeIds(final String typeName) {
        return preLoadServiceDAO.getLoadableTypeIds(typeName);
    }

    @Override
    public boolean isReferenceDataPresent() {
        return preLoadServiceDAO.getReferenceDataRowCount() > 0;
    }
}
