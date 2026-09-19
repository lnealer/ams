package org.example.am.shared.dao;

import org.example.am.shared.domain.AssetConfiguration;

/**
 * Supports the modify-configuration flow: what the device currently reports, versus the revision
 * AMS believes is applied.
 */
public interface ModifyConfigurationDAO {

    AssetConfiguration getDeviceReportedConfiguration(long assetId);

    int markMismatch(long configurationId, String userId);

    int resolveMismatch(long configurationId, String userId);
}
