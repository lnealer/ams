package org.example.am.shared.dao;

import org.example.am.shared.domain.MaintenanceWindow;

/**
 * Reads and writes the weekly maintenance window held against an asset.
 */
public interface MaintenanceWindowDAO {

    MaintenanceWindow getMaintenanceWindow(long assetId);

    int updateMaintenanceWindow(MaintenanceWindow window, String userId);

    MaintenanceWindow getMaintenanceWindowForOrder(long orderId);

    /**
     * Writes the window chosen while ordering, before any asset exists to hang it on.
     *
     * <p>An insert rather than the update above because there is nothing to update: the row is
     * created here and only later copied onto the asset, at installation.</p>
     *
     * @return the generated id
     */
    long insertMaintenanceWindowForOrder(MaintenanceWindow window, long orderId, String userId);

    /**
     * Moves an order-time window onto the asset it now applies to, satisfying MAINTWIN_OWNER_CK by
     * clearing ORDER_ID in the same statement: the constraint allows exactly one owner.
     */
    int attachWindowToAsset(long maintenanceWindowId, long assetId, String userId);
}
