package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.MaintenanceWindowDAO;
import org.example.am.shared.domain.DayType;
import org.example.am.shared.domain.HourType;
import org.example.am.shared.domain.MaintenanceWindow;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Reads and writes the weekly maintenance window held against an asset.
 */
@Repository("maintenanceWindowSharedDAO")
public class MaintenanceWindowDAOImpl extends BaseDAO implements MaintenanceWindowDAO {

    private static final String ATTACH_TO_ASSET =
            "UPDATE AMS_MAINTENANCE_WINDOWS "
          + "   SET ASSET_ID = :assetId, ORDER_ID = NULL,"
          + "       MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE MAINT_WINDOW_ID = :maintenanceWindowId ";

    private static final String SELECT_WINDOW =
            "SELECT W.MAINT_WINDOW_ID, W.ASSET_ID, W.DAY_CD, W.START_HOUR_CD, W.END_HOUR_CD,"
          + "       W.TIME_ZONE, W.ENABLED_FL "
          + "  FROM AMS_MAINTENANCE_WINDOWS W WHERE W.ASSET_ID = :assetId ";

    private static final String UPDATE_WINDOW =
            "UPDATE AMS_MAINTENANCE_WINDOWS "
          + "   SET DAY_CD = :dayCode, START_HOUR_CD = :startHourCode, END_HOUR_CD = :endHourCode,"
          + "       TIME_ZONE = :timeZone, ENABLED_FL = :enabledFlag,"
          + "       MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE ASSET_ID = :assetId ";

    private static final String SELECT_WINDOW_FOR_ORDER =
            "SELECT W.MAINT_WINDOW_ID, W.ASSET_ID, W.DAY_CD, W.START_HOUR_CD, W.END_HOUR_CD,"
          + "       W.TIME_ZONE, W.ENABLED_FL "
          + "  FROM AMS_MAINTENANCE_WINDOWS W WHERE W.ORDER_ID = :orderId ";

    private static final String NEXT_WINDOW_ID =
            "SELECT " + CommonConstants.SEQ_MAINTENANCE_WINDOWS + ".NEXTVAL FROM DUAL ";

    /**
     * ASSET_ID is left null and ORDER_ID set, which is what MAINTWIN_OWNER_CK requires: a window
     * belongs to one or the other, never both. It is copied onto the asset at installation.
     */
    private static final String INSERT_WINDOW_FOR_ORDER =
            "INSERT INTO AMS_MAINTENANCE_WINDOWS "
          + "       ( MAINT_WINDOW_ID, ASSET_ID, ORDER_ID, DAY_CD, START_HOUR_CD, END_HOUR_CD,"
          + "         TIME_ZONE, ENABLED_FL, CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :maintenanceWindowId, NULL, :orderId, :dayCode, :startHourCode, :endHourCode,"
          + "         :timeZone, :enabledFlag, SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final RowMapper<MaintenanceWindow> WINDOW_MAPPER = new RowMapper<MaintenanceWindow>() {

        @Override
        public MaintenanceWindow mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final MaintenanceWindow window = new MaintenanceWindow();
            window.setMaintenanceWindowId(ConversionUtils.getLong(rs, "MAINT_WINDOW_ID"));
            window.setAssetId(ConversionUtils.getLong(rs, "ASSET_ID"));
            window.setDayType(DayType.lookup(ConversionUtils.getString(rs, "DAY_CD")));
            window.setStartHour(HourType.lookup(ConversionUtils.getString(rs, "START_HOUR_CD")));
            window.setEndHour(HourType.lookup(ConversionUtils.getString(rs, "END_HOUR_CD")));
            window.setTimeZone(ConversionUtils.getString(rs, "TIME_ZONE"));
            window.setEnabled(ConversionUtils.getBoolean(rs, "ENABLED_FL"));
            return window;
        }
    };

    @Override
    public MaintenanceWindow getMaintenanceWindow(final long assetId) {
        final List<MaintenanceWindow> rows = getNamedParameterJdbcTemplate().query(SELECT_WINDOW,
                ParameterRepository.of(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId)).build(),
                WINDOW_MAPPER);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public MaintenanceWindow getMaintenanceWindowForOrder(final long orderId) {
        final List<MaintenanceWindow> rows = getNamedParameterJdbcTemplate().query(
                SELECT_WINDOW_FOR_ORDER,
                ParameterRepository.of(CommonConstants.PARAM_ORDER_ID, Long.valueOf(orderId)).build(),
                WINDOW_MAPPER);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public long insertMaintenanceWindowForOrder(final MaintenanceWindow window, final long orderId,
            final String userId) {
        final Long windowId = getNamedParameterJdbcTemplate().queryForObject(NEXT_WINDOW_ID,
                ParameterRepository.create().build(), Long.class);
        getNamedParameterJdbcTemplate().update(INSERT_WINDOW_FOR_ORDER,
                ParameterRepository.create()
                        .with("maintenanceWindowId", windowId)
                        .with(CommonConstants.PARAM_ORDER_ID, Long.valueOf(orderId))
                        .with("dayCode", window.getDayType() == null
                                ? null : window.getDayType().getCode())
                        .with("startHourCode", window.getStartHour() == null
                                ? null : window.getStartHour().getCode())
                        .with("endHourCode", window.getEndHour() == null
                                ? null : window.getEndHour().getCode())
                        .with("timeZone", window.getTimeZone())
                        .withFlag("enabledFlag", window.isEnabled())
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .build());
        window.setMaintenanceWindowId(windowId);
        return windowId.longValue();
    }

    @Override
    public int updateMaintenanceWindow(final MaintenanceWindow window, final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_WINDOW,
                ParameterRepository.create()
                        .with("dayCode", window.getDayType() == null ? null : window.getDayType().getCode())
                        .with("startHourCode",
                                window.getStartHour() == null ? null : window.getStartHour().getCode())
                        .with("endHourCode",
                                window.getEndHour() == null ? null : window.getEndHour().getCode())
                        .with("timeZone", window.getTimeZone())
                        .withFlag("enabledFlag", window.isEnabled())
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_ASSET_ID, window.getAssetId())
                        .build());
    }

    @Override
    public int attachWindowToAsset(final long maintenanceWindowId, final long assetId,
            final String userId) {
        return getNamedParameterJdbcTemplate().update(ATTACH_TO_ASSET, ParameterRepository.create()
                .with("maintenanceWindowId", Long.valueOf(maintenanceWindowId))
                .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                .with(CommonConstants.PARAM_USER_ID, userId)
                .build());
    }
}
