package org.example.am.internal.service.dao.impl;

import java.util.Date;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.am.internal.service.dao.StoredProcedureDAO;
import org.example.am.internal.service.dao.procs.AddEntityEmailProcedure;
import org.example.am.internal.service.dao.procs.CancelDecommissionDateProcedure;
import org.example.am.internal.service.dao.procs.CancelNCRDateProcedureComplex;
import org.example.am.internal.service.dao.procs.CancelNCRDateProcedureSimple;
import org.example.am.internal.service.dao.procs.CancelTimeslotProcedure;
import org.example.am.internal.service.dao.procs.ReserveDecommissionDateProcedure;
import org.example.am.internal.service.dao.procs.ReserveNCRDateProcedureComplex;
import org.example.am.internal.service.dao.procs.ReserveNCRDateProcedureSimple;
import org.example.am.internal.service.dao.procs.ReserveTimeslotProcedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Owns the compiled {@link org.springframework.jdbc.object.StoredProcedure} instances.
 *
 * <p>They are constructed here rather than declared as beans: nothing injects them individually,
 * and nine near-identical bean definitions would add noise to the context for no benefit. Compiling
 * once in {@link #init(DataSource)} matters because {@code compile()} reads parameter metadata,
 * which would otherwise cost a round trip on every call.</p>
 */
@Repository("internalStoredProcedureDAO")
public class StoredProcedureDAOImpl implements StoredProcedureDAO {

    private static final Logger LOGGER = LogManager.getLogger(StoredProcedureDAOImpl.class);

    private ReserveTimeslotProcedure reserveTimeslot;
    private CancelTimeslotProcedure cancelTimeslot;
    private ReserveNCRDateProcedureSimple reserveNcrSimple;
    private ReserveNCRDateProcedureComplex reserveNcrComplex;
    private CancelNCRDateProcedureSimple cancelNcrSimple;
    private CancelNCRDateProcedureComplex cancelNcrComplex;
    private ReserveDecommissionDateProcedure reserveDecommission;
    private CancelDecommissionDateProcedure cancelDecommission;
    private AddEntityEmailProcedure addEntityEmail;

    @Autowired
    public void init(final DataSource dataSource) {
        reserveTimeslot = new ReserveTimeslotProcedure(dataSource);
        cancelTimeslot = new CancelTimeslotProcedure(dataSource);
        reserveNcrSimple = new ReserveNCRDateProcedureSimple(dataSource);
        reserveNcrComplex = new ReserveNCRDateProcedureComplex(dataSource);
        cancelNcrSimple = new CancelNCRDateProcedureSimple(dataSource);
        cancelNcrComplex = new CancelNCRDateProcedureComplex(dataSource);
        reserveDecommission = new ReserveDecommissionDateProcedure(dataSource);
        cancelDecommission = new CancelDecommissionDateProcedure(dataSource);
        addEntityEmail = new AddEntityEmailProcedure(dataSource);
        LOGGER.info("Compiled {} internal stored procedures", Integer.valueOf(9));
    }

    @Override
    public String reserveTimeslot(final long timeslotId, final long entityId,
            final String entityTypeCode, final Date scheduledDate, final String userId) {
        return reserveTimeslot.reserve(timeslotId, entityId, entityTypeCode, scheduledDate, userId);
    }

    @Override
    public String cancelTimeslot(final long timeslotId, final long entityId,
            final String entityTypeCode, final String userId) {
        return cancelTimeslot.cancel(timeslotId, entityId, entityTypeCode, userId);
    }

    @Override
    public String reserveNetworkChangeDate(final long networkChangeRequestId, final long assetId,
            final String siteTypeCode, final Date scheduledDate, final boolean complex,
            final String userId) {
        if (complex) {
            return reserveNcrComplex.reserve(networkChangeRequestId, assetId, siteTypeCode,
                    scheduledDate, userId);
        }
        return reserveNcrSimple.reserve(networkChangeRequestId, scheduledDate, userId);
    }

    @Override
    public String cancelNetworkChangeDate(final long networkChangeRequestId, final long assetId,
            final String reason, final boolean complex, final String userId) {
        if (complex) {
            return cancelNcrComplex.cancel(networkChangeRequestId, assetId, reason, userId);
        }
        return cancelNcrSimple.cancel(networkChangeRequestId, reason, userId);
    }

    @Override
    public String reserveDecommissionDate(final long decommissionId, final long assetId,
            final Date scheduledDate, final boolean hardwareReturnRequired, final String userId) {
        return reserveDecommission.reserve(decommissionId, assetId, scheduledDate,
                hardwareReturnRequired, userId);
    }

    @Override
    public String cancelDecommissionDate(final long decommissionId, final String reason,
            final String userId) {
        return cancelDecommission.cancel(decommissionId, reason, userId);
    }

    @Override
    public String addEntityEmail(final String entityTypeCode, final long entityId,
            final String templateCode, final String userId) {
        return addEntityEmail.addEmail(entityTypeCode, entityId, templateCode, userId);
    }
}
