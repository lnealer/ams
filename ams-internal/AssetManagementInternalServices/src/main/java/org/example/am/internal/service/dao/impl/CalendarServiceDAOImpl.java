package org.example.am.internal.service.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.example.am.internal.service.dao.CalendarServiceDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.FacilitationCallType;
import org.example.am.shared.domain.Timeslot;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("calendarServiceDAO")
public class CalendarServiceDAOImpl extends BaseDAO implements CalendarServiceDAO {

    private static final String TIMESLOT_COLUMNS =
            "  T.TIMESLOT_ID, T.START_TM, T.END_TM, T.CAPACITY, T.RESERVED_COUNT,"
          + "  T.AVAILABLE_FL, T.DISPLAY_LABEL, T.TIME_ZONE ";

    private static final String SELECT_ALL_TIMESLOTS =
            "SELECT " + TIMESLOT_COLUMNS
          + "  FROM AMS_TIMESLOTS T "
          + " WHERE T.CALL_TYPE_CD = :typeCode "
          + "   AND T.START_TM >= :startDate "
          + "   AND T.START_TM <= :endDate "
          + " ORDER BY T.START_TM ";

    private static final String SELECT_AVAILABLE_TIMESLOTS =
            "SELECT " + TIMESLOT_COLUMNS
          + "  FROM AMS_TIMESLOTS T "
          + " WHERE T.CALL_TYPE_CD = :typeCode "
          + "   AND T.START_TM >= :startDate "
          + "   AND T.START_TM <= :endDate "
          + "   AND T.AVAILABLE_FL = 'Y' "
          + "   AND T.RESERVED_COUNT < T.CAPACITY "
          + " ORDER BY T.START_TM ";

    private static final String SELECT_HOLIDAYS =
            "SELECT H.HOLIDAY_DT FROM AMS_HOLIDAYS H "
          + " WHERE H.HOLIDAY_DT >= :startDate AND H.HOLIDAY_DT <= :endDate "
          + " ORDER BY H.HOLIDAY_DT ";

    private static final String SELECT_BLACKOUTS =
            "SELECT S.BLACKOUT_DT FROM AMS_CUSTOMER_SCHEDULES S "
          + " WHERE S.CUSTOMER_ID = :customerId "
          + "   AND S.BLACKOUT_DT >= :startDate AND S.BLACKOUT_DT <= :endDate "
          + " ORDER BY S.BLACKOUT_DT ";

    private static final String SELECT_DECOMMISSION_DATE =
            "SELECT * FROM ( SELECT D.SCHEDULED_DT FROM AMS_DECOMMISSIONS D "
          + "                 WHERE D.ASSET_ID = :assetId "
          + "                   AND D.DECOM_STATUS_CD = 'SCHEDULED' "
          + "                 ORDER BY D.SCHEDULED_DT DESC ) WHERE ROWNUM <= 1 ";

    private static final RowMapper<Timeslot> TIMESLOT_MAPPER = new TimeslotMapper();
    private static final RowMapper<Date> DATE_MAPPER = new DateMapper();

    @Override
    public List<Timeslot> getAllTimeslots(final FacilitationCallType callType, final Date from,
            final Date to) {
        return getNamedParameterJdbcTemplate().query(SELECT_ALL_TIMESLOTS,
                window(callType, from, to).build(), TIMESLOT_MAPPER);
    }

    @Override
    public List<Timeslot> getAvailableTimeslots(final FacilitationCallType callType, final Date from,
            final Date to) {
        return getNamedParameterJdbcTemplate().query(SELECT_AVAILABLE_TIMESLOTS,
                window(callType, from, to).build(), TIMESLOT_MAPPER);
    }

    @Override
    public List<Date> getHolidays(final Date from, final Date to) {
        if (from == null || to == null) {
            return new ArrayList<Date>();
        }
        return getNamedParameterJdbcTemplate().query(SELECT_HOLIDAYS,
                ParameterRepository.create()
                        .withDate(CommonConstants.PARAM_START_DATE, ConversionUtils.truncateToDay(from))
                        .withDate(CommonConstants.PARAM_END_DATE, ConversionUtils.truncateToDay(to))
                        .build(), DATE_MAPPER);
    }

    @Override
    public List<Date> getBlackoutDates(final long customerId, final Date from, final Date to) {
        return getNamedParameterJdbcTemplate().query(SELECT_BLACKOUTS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .withDate(CommonConstants.PARAM_START_DATE, from)
                        .withDate(CommonConstants.PARAM_END_DATE, to)
                        .build(), DATE_MAPPER);
    }

    @Override
    public Date getScheduledDecommissionDate(final long assetId) {
        final List<Date> rows = getNamedParameterJdbcTemplate().query(SELECT_DECOMMISSION_DATE,
                ParameterRepository.of(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId)).build(),
                DATE_MAPPER);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static ParameterRepository window(final FacilitationCallType callType, final Date from,
            final Date to) {
        return ParameterRepository.create()
                .with(CommonConstants.PARAM_TYPE_CODE, callType == null ? null : callType.getCode())
                .withDate(CommonConstants.PARAM_START_DATE, from)
                .withDate(CommonConstants.PARAM_END_DATE, to);
    }

    private static class TimeslotMapper implements RowMapper<Timeslot> {

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
    }

    private static class DateMapper implements RowMapper<Date> {

        @Override
        public Date mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final java.sql.Timestamp value = rs.getTimestamp(1);
            return value == null ? null : new Date(value.getTime());
        }
    }
}
