package org.example.am.shared.dao;

/**
 * Controls which assets may be replaced on the expedited path. The switch is per asset and is set
 * by operations, because an emergency replacement bypasses the normal lead time.
 */
public interface EmergencyReplacementDAO {

    boolean isEnabled(long assetId);

    int setEnabled(long assetId, boolean enabled, String userId);
}
