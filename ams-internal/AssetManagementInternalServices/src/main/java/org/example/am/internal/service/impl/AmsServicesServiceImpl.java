package org.example.am.internal.service.impl;

import java.util.List;

import org.example.am.internal.service.AmsServicesService;
import org.example.am.internal.service.dao.AmsServicesDAO;
import org.example.am.shared.domain.AmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("amsServicesService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AmsServicesServiceImpl implements AmsServicesService {

    @Autowired
    private AmsServicesDAO amsServicesDAO;

    @Override
    public List<AmsService> getServices(final long customerId) {
        return amsServicesDAO.getServices(customerId);
    }

    @Override
    public int getAssetCount(final long serviceId) {
        return amsServicesDAO.getAssetCountForService(serviceId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public boolean updateStatus(final long serviceId, final String statusCode, final String userId) {
        return amsServicesDAO.updateServiceStatus(serviceId, statusCode, userId) > 0;
    }
}
