package org.example.am.internal.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.am.internal.service.CalendarService;
import org.example.am.internal.service.dao.CalendarServiceDAO;
import org.example.am.internal.service.dao.StoredProcedureDAO;
import org.example.am.shared.domain.FacilitationCallType;
import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.domain.PropertyType;
import org.example.am.shared.domain.Timeslot;
import org.example.am.shared.service.ConfigService;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("internalCalendarService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class CalendarServiceImpl implements CalendarService {

    private static final Logger LOGGER = LogManager.getLogger(CalendarServiceImpl.class);

    /** Status the scheduling procedures return when they succeeded. */
    private static final String STATUS_OK = "OK";

    /** Defaults used when the property table has no value. */
    private static final int DEFAULT_TECHLINE_LEAD_TIME_DAYS = 3;
    private static final int DEFAULT_INSTALL_LEAD_TIME_DAYS = 10;

    /**
     * A decommission may not be booked more than this far ahead. Engineer availability that far out
     * is guesswork, and a booking made now would almost certainly have to be moved.
     */
    private static final int DEFAULT_MAX_DECOMMISSION_SCHEDULING_DAYS = 42;

    /**
     * How far past the target the holiday window is stretched when counting business days forward.
     * Covers weekends plus a run of consecutive holidays without a second query.
     */
    private static final int HOLIDAY_WINDOW_SLACK_DAYS = 21;

    @Autowired
    private CalendarServiceDAO calendarServiceDAO;

    @Autowired
    private StoredProcedureDAO storedProcedureDAO;

    @Autowired
    private ConfigService configService;

    // ------------------------------------------------------------------
    // Techline
    // ------------------------------------------------------------------

    @Override
    public Date getEarliestTechlineDate(final long customerId) {
        return addBusinessDays(new Date(), configService.getInt(
                PropertyType.MIN_TECHLINE_LEAD_TIME_DAYS, DEFAULT_TECHLINE_LEAD_TIME_DAYS));
    }

    @Override
    public List<Timeslot> getTechlineTimeslots(final long customerId, final Date from, final Date to) {
        return offerableSlots(FacilitationCallType.TECHLINE, customerId,
                getEarliestTechlineDate(customerId), from, to);
    }

    // ------------------------------------------------------------------
    // Installation
    // ------------------------------------------------------------------

    @Override
    public Date getEarliestInstallationDate(final long customerId) {
        return addBusinessDays(new Date(), configService.getInt(
                PropertyType.MIN_INSTALL_LEAD_TIME_DAYS, DEFAULT_INSTALL_LEAD_TIME_DAYS));
    }

    @Override
    public List<Timeslot> getInstallationTimeslots(final long customerId, final Date from,
            final Date to) {
        return offerableSlots(FacilitationCallType.INSTALLATION, customerId,
                getEarliestInstallationDate(customerId), from, to);
    }

    // ------------------------------------------------------------------
    // Network change requests
    // ------------------------------------------------------------------

    /**
     * A site type change reshapes the circuit as well as the device, so it has to go through the
     * procedure that books both windows atomically. Everything else takes the simple path.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public boolean reserveNetworkChangeDate(final NetworkChangeRequest request,
            final Date scheduledDate, final String userId) {
        if (request == null || request.getNetworkChangeRequestId() == null) {
            throw new IllegalArgumentException("A persisted network change request is required");
        }
        final String status = storedProcedureDAO.reserveNetworkChangeDate(
                request.getNetworkChangeRequestId().longValue(),
                request.getAssetId() == null ? 0L : request.getAssetId().longValue(),
                getSiteTypeCode(request), scheduledDate, request.isComplexScheduling(), userId);
        if (!STATUS_OK.equals(status)) {
            LOGGER.info("Could not reserve {} change window for request {}: {}",
                    request.isComplexScheduling() ? "complex" : "simple",
                    request.getNetworkChangeRequestId(), status);
            return false;
        }
        return true;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public boolean cancelNetworkChangeDate(final NetworkChangeRequest request, final String reason,
            final String userId) {
        if (request == null || request.getNetworkChangeRequestId() == null) {
            throw new IllegalArgumentException("A persisted network change request is required");
        }
        final String status = storedProcedureDAO.cancelNetworkChangeDate(
                request.getNetworkChangeRequestId().longValue(),
                request.getAssetId() == null ? 0L : request.getAssetId().longValue(), reason,
                request.isComplexScheduling(), userId);
        return STATUS_OK.equals(status);
    }

    /**
     * @return the target site type for a site type change, or {@code null} for the simple path,
     *         where the procedure does not take one
     */
    private static String getSiteTypeCode(final NetworkChangeRequest request) {
        if (!request.isComplexScheduling()) {
            return null;
        }
        return request.getProposedConfiguration() == null
                || request.getProposedConfiguration().getAssetConfigurationType() == null
                ? null : request.getProposedConfiguration().getAssetConfigurationType().getCode();
    }

    // ------------------------------------------------------------------
    // Decommission
    // ------------------------------------------------------------------

    @Override
    public Date getLatestDecommissionDate() {
        final int windowDays = configService.getInt(PropertyType.MAX_DECOMMISSION_SCHEDULING_DAYS,
                DEFAULT_MAX_DECOMMISSION_SCHEDULING_DAYS);
        return ConversionUtils.addDays(ConversionUtils.truncateToDay(new Date()), windowDays);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public boolean reserveDecommissionDate(final long decommissionId, final long assetId,
            final Date scheduledDate, final boolean hardwareReturnRequired, final String userId) {
        if (scheduledDate == null) {
            throw new IllegalArgumentException("A decommission date is required");
        }
        final Date latest = getLatestDecommissionDate();
        if (ConversionUtils.truncateToDay(scheduledDate).after(latest)) {
            throw new IllegalArgumentException("A decommission cannot be scheduled after " + latest);
        }
        if (ConversionUtils.truncateToDay(scheduledDate)
                .before(ConversionUtils.truncateToDay(new Date()))) {
            throw new IllegalArgumentException("A decommission cannot be scheduled in the past");
        }
        final String status = storedProcedureDAO.reserveDecommissionDate(decommissionId, assetId,
                scheduledDate, hardwareReturnRequired, userId);
        return STATUS_OK.equals(status);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public boolean cancelDecommissionDate(final long decommissionId, final String reason,
            final String userId) {
        return STATUS_OK.equals(
                storedProcedureDAO.cancelDecommissionDate(decommissionId, reason, userId));
    }

    // ------------------------------------------------------------------
    // Shared date arithmetic
    // ------------------------------------------------------------------

    @Override
    public boolean isSchedulableDate(final long customerId, final Date date) {
        if (date == null) {
            return false;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (isWeekend(calendar)) {
            return false;
        }
        final Date day = ConversionUtils.truncateToDay(date);
        if (toDayKeys(calendarServiceDAO.getHolidays(day, day)).contains(dayKey(day))) {
            return false;
        }
        return !toDayKeys(calendarServiceDAO.getBlackoutDates(customerId, day, day))
                .contains(dayKey(day));
    }

    /**
     * Counts forward over weekdays, skipping the holiday list. The holidays for the whole window
     * are read once: doing it a day at a time would cost a query per iteration.
     */
    private Date addBusinessDays(final Date from, final int businessDays) {
        final Date start = ConversionUtils.truncateToDay(from);
        if (businessDays <= 0) {
            return start;
        }
        final Date windowEnd =
                ConversionUtils.addDays(start, businessDays * 2 + HOLIDAY_WINDOW_SLACK_DAYS);
        final Set<Long> holidays = toDayKeys(calendarServiceDAO.getHolidays(start, windowEnd));

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        int remaining = businessDays;
        while (remaining > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if (!isWeekend(calendar) && !holidays.contains(Long.valueOf(calendar.getTimeInMillis()))) {
                remaining--;
            }
        }
        return calendar.getTime();
    }

    /**
     * Applies the three filters every slot picker needs: nothing before the lead time, nothing on a
     * date the customer has blacked out, and nothing already full.
     */
    private List<Timeslot> offerableSlots(final FacilitationCallType callType, final long customerId,
            final Date earliest, final Date from, final Date to) {
        final Date windowStart = from == null || from.before(earliest) ? earliest : from;
        final Date windowEnd = to == null ? ConversionUtils.addDays(windowStart, 60) : to;
        if (windowEnd.before(windowStart)) {
            return new ArrayList<Timeslot>();
        }

        final Set<Long> blackouts =
                toDayKeys(calendarServiceDAO.getBlackoutDates(customerId, windowStart, windowEnd));

        final List<Timeslot> offerable = new ArrayList<Timeslot>();
        for (final Timeslot slot : calendarServiceDAO.getAvailableTimeslots(callType, windowStart,
                windowEnd)) {
            if (slot.getStartTime() == null || !slot.isSelectable()) {
                continue;
            }
            if (blackouts.contains(dayKey(slot.getStartTime()))) {
                continue;
            }
            offerable.add(slot);
        }
        return offerable;
    }

    private static boolean isWeekend(final Calendar calendar) {
        final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    private static Set<Long> toDayKeys(final List<Date> dates) {
        final Set<Long> keys = new HashSet<Long>();
        for (final Date date : dates) {
            if (date != null) {
                keys.add(dayKey(date));
            }
        }
        return keys;
    }

    private static Long dayKey(final Date date) {
        return Long.valueOf(ConversionUtils.truncateToDay(date).getTime());
    }
}
