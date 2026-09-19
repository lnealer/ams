package org.example.am.shared.dao;

import java.util.Date;

/**
 * The single entry point to the PL/SQL layer from the shared module.
 *
 * <p>The {@link org.springframework.jdbc.object.StoredProcedure} objects are compiled once and
 * reused; they are constructed directly in {@code init} rather than declared as beans, because they
 * are not injected anywhere and giving each one a bean name would put four near-identical
 * definitions into the context.</p>
 */
public interface StoredProcedureDAO {

    String reserveTimeslot(long timeslotId, long entityId, String entityType, Date scheduledDate,
            String userId);

    /**
     * Routes to the simple or the complex cancellation procedure.
     *
     * @param complex {@code true} for a site type change, which also holds a circuit reservation
     */
    String cancelNetworkChangeRequestDate(long networkChangeRequestId, long assetId, String reason,
            boolean complex, String userId);

    Long addEntityEmail(String entityTypeCode, long entityId, String templateCode, String userId);
}
