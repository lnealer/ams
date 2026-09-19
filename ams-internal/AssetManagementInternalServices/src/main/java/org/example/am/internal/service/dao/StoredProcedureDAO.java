package org.example.am.internal.service.dao;

import java.util.Date;

/**
 * The internal application's single entry point to the PL/SQL scheduling layer.
 *
 * <p>Every reservation and cancellation goes through a procedure rather than through SQL here,
 * because each one has to update several tables and take a lock in a single atomic step.</p>
 */
public interface StoredProcedureDAO {

    String reserveTimeslot(long timeslotId, long entityId, String entityTypeCode, Date scheduledDate,
            String userId);

    String cancelTimeslot(long timeslotId, long entityId, String entityTypeCode, String userId);

    /**
     * @param complex {@code true} for a site type change, which also books the circuit window
     * @param siteTypeCode the new site type, only meaningful when {@code complex}
     */
    String reserveNetworkChangeDate(long networkChangeRequestId, long assetId, String siteTypeCode,
            Date scheduledDate, boolean complex, String userId);

    String cancelNetworkChangeDate(long networkChangeRequestId, long assetId, String reason,
            boolean complex, String userId);

    String reserveDecommissionDate(long decommissionId, long assetId, Date scheduledDate,
            boolean hardwareReturnRequired, String userId);

    String cancelDecommissionDate(long decommissionId, String reason, String userId);

    String addEntityEmail(String entityTypeCode, long entityId, String templateCode, String userId);
}
