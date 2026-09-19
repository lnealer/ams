package org.example.am.shared.service.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.example.am.shared.dao.InstallationCalendarDAO;
import org.example.am.shared.dao.ShippingCalendarDAO;
import org.example.am.shared.domain.PropertyType;
import org.example.am.shared.domain.Timeslot;
import org.example.am.shared.service.CalendarService;
import org.example.am.shared.service.ConfigService;
import org.example.am.shared.service.ShippingCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The despatch windows an order can be given, resolved from the destination postcode.
 */
@Service("shippingCalendarService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ShippingCalendarServiceImpl implements ShippingCalendarService {

    /** Used when the property table has no value. */
    private static final int DEFAULT_LEAD_TIME_DAYS = 2;
    private static final int DEFAULT_HORIZON_DAYS = 21;

    @Autowired
    private ShippingCalendarDAO shippingCalendarDAO;

    /**
     * The postcode to region map lives on the installation DAO and is shared, not duplicated: a
     * region is a geography, and warehouses and engineers are organised on the same one. Two maps
     * would drift and an order would be despatched from a region no engineer covers.
     */
    @Autowired
    private InstallationCalendarDAO installationCalendarDAO;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private ConfigService configService;

    @Override
    public List<Timeslot> getShippingWindowsForZipCode(final String zipCode) {
        // Business days, not calendar days: the warehouse cannot pick over a weekend, so counting
        // plainly would offer a Monday window on a Saturday order that nobody can fill.
        final Date now = new Date();
        final Date from = calendarService.addBusinessDays(now, getLeadTimeDays());
        return getShippingWindowsForZipCode(zipCode, from, addDays(from, getHorizonDays()));
    }

    @Override
    public List<Timeslot> getShippingWindowsForZipCode(final String zipCode, final Date from,
            final Date to) {
        final String regionCode = getRegion(zipCode);
        if (regionCode == null) {
            return Collections.<Timeslot>emptyList();
        }
        return shippingCalendarDAO.getShippingWindows(regionCode, from, to);
    }

    @Override
    public Timeslot getShippingWindow(final long timeslotId) {
        return shippingCalendarDAO.getShippingWindow(timeslotId);
    }

    @Override
    public String getRegion(final String zipCode) {
        if (zipCode == null || zipCode.trim().length() == 0) {
            return null;
        }
        return installationCalendarDAO.getRegionForZipCode(zipCode.trim());
    }

    private int getLeadTimeDays() {
        return configService.getInt(PropertyType.MIN_SHIPPING_LEAD_TIME_DAYS,
                DEFAULT_LEAD_TIME_DAYS);
    }

    private int getHorizonDays() {
        return configService.getInt(PropertyType.SHIPPING_WINDOW_HORIZON_DAYS,
                DEFAULT_HORIZON_DAYS);
    }

    private static Date addDays(final Date from, final int days) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(from);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    public void setShippingCalendarDAO(final ShippingCalendarDAO shippingCalendarDAO) {
        this.shippingCalendarDAO = shippingCalendarDAO;
    }

    public void setInstallationCalendarDAO(final InstallationCalendarDAO installationCalendarDAO) {
        this.installationCalendarDAO = installationCalendarDAO;
    }

    public void setCalendarService(final CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public void setConfigService(final ConfigService configService) {
        this.configService = configService;
    }
}
