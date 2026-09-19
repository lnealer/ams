package org.example.am.shared.service.impl;

import org.example.am.shared.domain.FacilitationCallType;
import org.example.am.shared.domain.PropertyType;
import org.springframework.stereotype.Service;

/**
 * Books the on-site installation visit. The lead time has to cover shipping the hardware as well as
 * finding an engineer, which is why it is the longest of the four flows.
 */
@Service("scheduleInstallationService")
public class ScheduleInstallationServiceImpl extends BaseScheduleServiceImpl {

    private static final int DEFAULT_INSTALL_LEAD_TIME_DAYS = 10;

    @Override
    protected FacilitationCallType getCallType() {
        return FacilitationCallType.INSTALLATION;
    }

    @Override
    protected int getMinimumLeadTimeBusinessDays() {
        return getConfigService().getInt(PropertyType.MIN_INSTALL_LEAD_TIME_DAYS,
                DEFAULT_INSTALL_LEAD_TIME_DAYS);
    }

    @Override
    protected String getEntityTypeCode() {
        return "INSTALL";
    }
}
