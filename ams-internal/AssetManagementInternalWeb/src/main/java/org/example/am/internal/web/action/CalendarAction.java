package org.example.am.internal.web.action;

import java.util.Collections;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.CalendarService;
import org.example.am.internal.web.model.CalendarModel;
import org.example.am.shared.domain.FacilitationCallType;
import org.example.am.shared.domain.Timeslot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Serves the timeslot pickers.
 *
 * <p>Every method answers JSON: the pickers are Dojo widgets that fetch their slots as the user
 * moves through the calendar rather than having them rendered into the page.</p>
 */
@Component("CalendarAction")
@Scope("prototype")
public class CalendarAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private final CalendarModel model = new CalendarModel();

    @Autowired
    private transient CalendarService internalCalendarService;

    @Override
    public CalendarModel getModel() {
        return model;
    }

    /** The techline call picker. */
    public String techLineTimeSlots() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SCHEDULE_TECHLINE);
        if (denied != null) {
            return denied;
        }
        final long customerId = resolveCustomerId();
        model.setCallType(FacilitationCallType.TECHLINE.getCode());
        model.setEarliestDate(internalCalendarService.getEarliestTechlineDate(customerId));
        model.setTimeslots(internalCalendarService.getTechlineTimeslots(customerId,
                model.getFromDate(), model.getToDate()));
        return Action.SUCCESS;
    }

    /** The installation visit picker. */
    public String installationTimeSlots() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SCHEDULE_INSTALLATION);
        if (denied != null) {
            return denied;
        }
        final long customerId = resolveCustomerId();
        model.setCallType(FacilitationCallType.INSTALLATION.getCode());
        model.setEarliestDate(internalCalendarService.getEarliestInstallationDate(customerId));
        model.setTimeslots(internalCalendarService.getInstallationTimeslots(customerId,
                model.getFromDate(), model.getToDate()));
        return Action.SUCCESS;
    }

    /**
     * The dashboard's combined view.
     *
     * <p>Shows only installation slots that still have room for a techline call on the same day,
     * because booking one without the other leaves the visit unsupported.</p>
     */
    public String installationTimeSlotsModifiedForDashboardTechline() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SCHEDULE_INSTALLATION);
        if (denied != null) {
            return denied;
        }
        final long customerId = resolveCustomerId();
        final java.util.List<Timeslot> installationSlots = internalCalendarService
                .getInstallationTimeslots(customerId, model.getFromDate(), model.getToDate());
        final java.util.List<Timeslot> techlineSlots = internalCalendarService
                .getTechlineTimeslots(customerId, model.getFromDate(), model.getToDate());

        final java.util.Set<Long> techlineDays = new java.util.HashSet<Long>();
        for (final Timeslot slot : techlineSlots) {
            if (slot.getStartTime() != null) {
                techlineDays.add(Long.valueOf(org.example.am.shared.utils.ConversionUtils
                        .truncateToDay(slot.getStartTime()).getTime()));
            }
        }

        final java.util.List<Timeslot> pairable = new java.util.ArrayList<Timeslot>();
        for (final Timeslot slot : installationSlots) {
            if (slot.getStartTime() == null) {
                continue;
            }
            final Long day = Long.valueOf(org.example.am.shared.utils.ConversionUtils
                    .truncateToDay(slot.getStartTime()).getTime());
            if (techlineDays.contains(day)) {
                pairable.add(slot);
            }
        }
        model.setCallType(FacilitationCallType.INSTALLATION.getCode());
        model.setTimeslots(pairable);
        return Action.SUCCESS;
    }

    /** The general service call picker. */
    public String serviceTimeSlots() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_CALENDAR);
        if (denied != null) {
            return denied;
        }
        model.setCallType(FacilitationCallType.SERVICE.getCode());
        model.setTimeslots(Collections.<Timeslot>emptyList());
        return Action.SUCCESS;
    }

    private long resolveCustomerId() {
        if (model.getCustomerId() != null) {
            return model.getCustomerId().longValue();
        }
        final Long sessionCustomerId = getCurrentCustomerId();
        return sessionCustomerId == null ? 0L : sessionCustomerId.longValue();
    }
}
