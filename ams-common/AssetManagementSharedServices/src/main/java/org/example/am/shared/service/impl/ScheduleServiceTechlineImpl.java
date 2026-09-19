package org.example.am.shared.service.impl;

import org.example.am.shared.domain.FacilitationCallType;
import org.example.am.shared.domain.PropertyType;
import org.springframework.stereotype.Service;

/**
 * Books the technical support call that runs alongside an installation.
 *
 * <p>Techline needs less notice than an engineer visit, because nobody has to travel.</p>
 */
@Service("scheduleServiceTechline")
public class ScheduleServiceTechlineImpl extends BaseScheduleServiceImpl {

    private static final int DEFAULT_TECHLINE_LEAD_TIME_DAYS = 3;

    @Override
    protected FacilitationCallType getCallType() {
        return FacilitationCallType.TECHLINE;
    }

    @Override
    protected int getMinimumLeadTimeBusinessDays() {
        return getConfigService().getInt(PropertyType.MIN_TECHLINE_LEAD_TIME_DAYS,
                DEFAULT_TECHLINE_LEAD_TIME_DAYS);
    }

    @Override
    protected String getEntityTypeCode() {
        return "TECHLINE";
    }
}
