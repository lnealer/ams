package org.example.am.shared.dao;

import java.util.Date;

import org.example.am.shared.domain.Installation;

/**
 * Writes {@code AMS_INSTALLATIONS}.
 *
 * <p>The table was previously read-only as far as the application was concerned: the calendar
 * updated an existing row's date, and every row itself came from the seed. Provisioning creates
 * one per despatched order, which is the record the engineer's visit hangs off.</p>
 */
public interface InstallationDAO {

    /** @return the installation raised for this order, or {@code null} if it has not despatched */
    Installation getInstallationForOrder(long orderId);

    /**
     * Raises the visit record for a despatched order, unscheduled.
     *
     * @return the generated installation id
     */
    long insertInstallation(long orderId, long assetId, Long installAddressId, Long installContactId,
            String userId);

    /**
     * Records the visit as done.
     *
     * @return rows updated; zero when the installation was already completed or cancelled, which
     *         is what makes a double submit harmless
     */
    int completeInstallation(long installationId, String technicianName, String notes,
            Date completedDate, String userId);
}
