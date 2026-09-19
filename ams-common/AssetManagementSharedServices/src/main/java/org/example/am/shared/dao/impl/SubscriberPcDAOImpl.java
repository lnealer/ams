package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.SubscriberPcDAO;
import org.example.am.shared.domain.SubscriberPc;
import org.example.am.shared.domain.SubscriberPcType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Reads and writes {@code AMS_SUBSCRIBER_PCS}, the machines captured with an order. */
@Repository("subscriberPcSharedDAO")
public class SubscriberPcDAOImpl extends BaseDAO implements SubscriberPcDAO {

    private static final String SELECT_PCS =
            "SELECT P.SUBSCRIBER_PC_ID, P.ORDER_ID, P.HOST_NAME, P.PC_TYPE_CD,"
          + "       P.OPERATING_SYSTEM, P.MAC_ADDRESS, P.IP_ADDRESS, P.STATIC_FL,"
          + "       P.USER_COUNT, P.NOTES "
          + "  FROM AMS_SUBSCRIBER_PCS P "
          + " WHERE P.ORDER_ID = :orderId "
          // Ordered by id, not by host name: the engineer works down the list in the order it was
          // keyed, and re-sorting it makes the printed sheet disagree with the screen.
          + " ORDER BY P.SUBSCRIBER_PC_ID ";

    private static final String NEXT_PC_ID =
            "SELECT " + CommonConstants.SEQ_SUBSCRIBER_PCS + ".NEXTVAL FROM DUAL ";

    private static final String INSERT_PC =
            "INSERT INTO AMS_SUBSCRIBER_PCS "
          + "       ( SUBSCRIBER_PC_ID, ORDER_ID, HOST_NAME, PC_TYPE_CD, OPERATING_SYSTEM,"
          + "         MAC_ADDRESS, IP_ADDRESS, STATIC_FL, USER_COUNT, NOTES,"
          + "         CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :subscriberPcId, :orderId, :hostName, :pcTypeCode, :operatingSystem,"
          + "         :macAddress, :ipAddress, :staticFlag, :userCount, :notes,"
          + "         SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final String DELETE_PCS =
            "DELETE FROM AMS_SUBSCRIBER_PCS WHERE ORDER_ID = :orderId ";

    private static final RowMapper<SubscriberPc> PC_MAPPER = new RowMapper<SubscriberPc>() {

        @Override
        public SubscriberPc mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final SubscriberPc pc = new SubscriberPc();
            pc.setSubscriberPcId(ConversionUtils.getLong(rs, "SUBSCRIBER_PC_ID"));
            pc.setOrderId(ConversionUtils.getLong(rs, "ORDER_ID"));
            pc.setHostName(ConversionUtils.getString(rs, "HOST_NAME"));
            pc.setSubscriberPcType(
                    SubscriberPcType.lookup(ConversionUtils.getString(rs, "PC_TYPE_CD")));
            pc.setOperatingSystem(ConversionUtils.getString(rs, "OPERATING_SYSTEM"));
            pc.setMacAddress(ConversionUtils.getString(rs, "MAC_ADDRESS"));
            pc.setIpAddress(ConversionUtils.getString(rs, "IP_ADDRESS"));
            pc.setStaticAddress(ConversionUtils.getBoolean(rs, "STATIC_FL"));
            pc.setUserCount(ConversionUtils.getInteger(rs, "USER_COUNT"));
            pc.setNotes(ConversionUtils.getString(rs, "NOTES"));
            return pc;
        }
    };

    @Override
    public List<SubscriberPc> getSubscriberPcs(final long orderId) {
        return getNamedParameterJdbcTemplate().query(SELECT_PCS,
                ParameterRepository.of(CommonConstants.PARAM_ORDER_ID, Long.valueOf(orderId)).build(),
                PC_MAPPER);
    }

    @Override
    public long insertSubscriberPc(final SubscriberPc subscriberPc, final long orderId,
            final String userId) {
        final Long id = getNamedParameterJdbcTemplate().queryForObject(NEXT_PC_ID,
                ParameterRepository.create().build(), Long.class);

        getNamedParameterJdbcTemplate().update(INSERT_PC, ParameterRepository.create()
                .with("subscriberPcId", id)
                .with(CommonConstants.PARAM_ORDER_ID, Long.valueOf(orderId))
                .with("hostName", trimToNull(subscriberPc.getHostName()))
                .with("pcTypeCode", subscriberPc.getSubscriberPcType() == null
                        ? null : subscriberPc.getSubscriberPcType().getCode())
                .with("operatingSystem", trimToNull(subscriberPc.getOperatingSystem()))
                .with("macAddress", trimToNull(subscriberPc.getMacAddress()))
                .with("ipAddress", trimToNull(subscriberPc.getIpAddress()))
                .withFlag("staticFlag", subscriberPc.isStaticAddress())
                .with("userCount", subscriberPc.getUserCount())
                .with("notes", trimToNull(subscriberPc.getNotes()))
                .with(CommonConstants.PARAM_USER_ID, userId)
                .build());

        subscriberPc.setSubscriberPcId(id);
        subscriberPc.setOrderId(Long.valueOf(orderId));
        return id.longValue();
    }

    @Override
    public int replaceSubscriberPcs(final List<SubscriberPc> subscriberPcs, final long orderId,
            final String userId) {
        deleteSubscriberPcs(orderId);
        if (subscriberPcs == null) {
            return 0;
        }
        int written = 0;
        for (final SubscriberPc pc : subscriberPcs) {
            // A blank row is the form's padding, not a machine. Writing it would put an empty
            // line on the engineer's sheet for every row the user did not fill in.
            if (pc == null || pc.isBlank()) {
                continue;
            }
            insertSubscriberPc(pc, orderId, userId);
            written++;
        }
        return written;
    }

    @Override
    public int deleteSubscriberPcs(final long orderId) {
        return getNamedParameterJdbcTemplate().update(DELETE_PCS,
                ParameterRepository.of(CommonConstants.PARAM_ORDER_ID, Long.valueOf(orderId)).build());
    }

    /**
     * Keeps a field the user tabbed through but left blank out of the database as NULL rather than
     * as an empty string, so {@code SubscriberPc.isBlank} agrees with what was stored.
     */
    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
