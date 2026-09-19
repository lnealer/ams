package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.ShippingCalendarDAO;
import org.example.am.shared.domain.Timeslot;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Despatch capacity: which warehouse regions can ship on which day.
 *
 * <p>A near twin of {@code InstallationCalendarDAOImpl} - same table, same exclusions, different
 * call type. Kept as its own DAO rather than parameterising that one on the call type: the two
 * have different lifetimes (a despatch window is booked at order time, an installation slot much
 * later) and a shared method would invite a caller to pass 'INSTALL' where it means 'SHIP'.</p>
 */
@Repository("shippingCalendarSharedDAO")
public class ShippingCalendarDAOImpl extends BaseDAO implements ShippingCalendarDAO {

    private static final String SHIPPING_COLUMNS =
            "  T.TIMESLOT_ID, T.START_TM, T.END_TM, T.CAPACITY, T.RESERVED_COUNT,"
          + "  T.AVAILABLE_FL, T.DISPLAY_LABEL, T.TIME_ZONE ";

    /**
     * Both exclusions matter and they mean different things: {@code AVAILABLE_FL = 'N'} is a
     * window operations never opened, and {@code RESERVED_COUNT >= CAPACITY} is one that filled up.
     * Neither is bookable, but only the second one changes during the day.
     */
    private static final String SELECT_SHIPPING_WINDOWS =
            "SELECT " + SHIPPING_COLUMNS
          + "  FROM AMS_TIMESLOTS T "
          + " WHERE T.CALL_TYPE_CD = 'SHIP' "
          + "   AND T.REGION_CD = :regionCode "
          + "   AND T.START_TM BETWEEN :startDate AND :endDate "
          + "   AND T.AVAILABLE_FL = 'Y' "
          + "   AND T.RESERVED_COUNT < T.CAPACITY "
          + " ORDER BY T.START_TM ";

    /**
     * Deliberately unfiltered: this reads back a window that has already been chosen, and by then
     * it may well be full - it holds this order's own reservation. Applying the availability
     * predicate here would make a placed order's own window vanish from its confirmation.
     */
    private static final String SELECT_SHIPPING_WINDOW =
            "SELECT " + SHIPPING_COLUMNS
          + "  FROM AMS_TIMESLOTS T "
          + " WHERE T.TIMESLOT_ID = :timeslotId AND T.CALL_TYPE_CD = 'SHIP' ";

    private static final RowMapper<Timeslot> SHIPPING_WINDOW_MAPPER = new RowMapper<Timeslot>() {

        @Override
        public Timeslot mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Timeslot timeslot = new Timeslot();
            timeslot.setTimeslotId(ConversionUtils.getLong(rs, "TIMESLOT_ID"));
            timeslot.setStartTime(ConversionUtils.getDate(rs, "START_TM"));
            timeslot.setEndTime(ConversionUtils.getDate(rs, "END_TM"));
            final Integer capacity = ConversionUtils.getInteger(rs, "CAPACITY");
            timeslot.setCapacity(capacity == null ? 0 : capacity.intValue());
            final Integer reserved = ConversionUtils.getInteger(rs, "RESERVED_COUNT");
            timeslot.setReserved(reserved == null ? 0 : reserved.intValue());
            timeslot.setAvailable(ConversionUtils.getBoolean(rs, "AVAILABLE_FL"));
            timeslot.setDisplayLabel(ConversionUtils.getString(rs, "DISPLAY_LABEL"));
            timeslot.setTimeZone(ConversionUtils.getString(rs, "TIME_ZONE"));
            return timeslot;
        }
    };

    @Override
    public List<Timeslot> getShippingWindows(final String regionCode, final Date from, final Date to) {
        if (regionCode == null) {
            return java.util.Collections.emptyList();
        }
        return getNamedParameterJdbcTemplate().query(SELECT_SHIPPING_WINDOWS,
                ParameterRepository.create()
                        .with("regionCode", regionCode)
                        .withDate(CommonConstants.PARAM_START_DATE, from)
                        .withDate(CommonConstants.PARAM_END_DATE, to)
                        .build(), SHIPPING_WINDOW_MAPPER);
    }

    @Override
    public Timeslot getShippingWindow(final long timeslotId) {
        final List<Timeslot> rows = getNamedParameterJdbcTemplate().query(SELECT_SHIPPING_WINDOW,
                ParameterRepository.of("timeslotId", Long.valueOf(timeslotId)).build(),
                SHIPPING_WINDOW_MAPPER);
        return rows.isEmpty() ? null : rows.get(0);
    }
}
