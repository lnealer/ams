package org.example.am.shared.dao.impl;

import java.util.Date;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.am.shared.dao.StoredProcedureDAO;
import org.example.am.shared.dao.procs.AddEntityEmailProcedure;
import org.example.am.shared.dao.procs.CancelNCRDateProcedureComplex;
import org.example.am.shared.dao.procs.CancelNCRDateProcedureSimple;
import org.example.am.shared.dao.procs.ReserveTimeslotProcedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("storedProcedureSharedDAO")
public class StoredProcedureDAOImpl implements StoredProcedureDAO {

    private static final Logger LOGGER = LogManager.getLogger(StoredProcedureDAOImpl.class);

    private ReserveTimeslotProcedure reserveTimeslotProcedure;
    private CancelNCRDateProcedureSimple cancelNcrDateSimple;
    private CancelNCRDateProcedureComplex cancelNcrDateComplex;
    private AddEntityEmailProcedure addEntityEmailProcedure;

    /**
     * Compiles every procedure once, on injection of the data source.
     *
     * <p>{@code StoredProcedure.compile()} reads the parameter metadata, so doing this per call
     * would cost a round trip each time.</p>
     */
    @Autowired
    public void init(final DataSource dataSource) {
        reserveTimeslotProcedure = new ReserveTimeslotProcedure(dataSource);
        cancelNcrDateSimple = new CancelNCRDateProcedureSimple(dataSource);
        cancelNcrDateComplex = new CancelNCRDateProcedureComplex(dataSource);
        addEntityEmailProcedure = new AddEntityEmailProcedure(dataSource);
        LOGGER.info("Compiled {} shared stored procedures", Integer.valueOf(4));
    }

    @Override
    public String reserveTimeslot(final long timeslotId, final long entityId, final String entityType,
            final Date scheduledDate, final String userId) {
        return reserveTimeslotProcedure.reserve(timeslotId, entityId, entityType, scheduledDate, userId);
    }

    @Override
    public String cancelNetworkChangeRequestDate(final long networkChangeRequestId, final long assetId,
            final String reason, final boolean complex, final String userId) {
        if (complex) {
            return cancelNcrDateComplex.cancel(networkChangeRequestId, assetId, reason, userId);
        }
        return cancelNcrDateSimple.cancel(networkChangeRequestId, reason, userId);
    }

    @Override
    public Long addEntityEmail(final String entityTypeCode, final long entityId,
            final String templateCode, final String userId) {
        return addEntityEmailProcedure.addEmail(entityTypeCode, entityId, templateCode, userId);
    }
}
