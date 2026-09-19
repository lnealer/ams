package org.example.am.shared.service.impl;

import org.example.am.shared.domain.FacilitationCallType;
import org.springframework.stereotype.Service;

/**
 * Books the change window for a network change request.
 */
@Service("networkChangeRequestScheduleService")
public class NetworkChangeRequestScheduleServiceImpl extends BaseScheduleServiceImpl {

    private static final int NCR_LEAD_TIME_BUSINESS_DAYS = 5;

    @Override
    protected FacilitationCallType getCallType() {
        return FacilitationCallType.NCR;
    }

    @Override
    protected int getMinimumLeadTimeBusinessDays() {
        return NCR_LEAD_TIME_BUSINESS_DAYS;
    }

    @Override
    protected String getEntityTypeCode() {
        return "NCR";
    }
}
