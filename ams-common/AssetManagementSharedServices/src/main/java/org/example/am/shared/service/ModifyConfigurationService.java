package org.example.am.shared.service;

import java.util.List;
import org.example.am.shared.domain.AssetConfiguration;

/**
 * Compares the stored configuration revision against what the device reports and resolves the
 * mismatch either way.
 */
public interface ModifyConfigurationService {

    AssetConfiguration getDeviceReportedConfiguration(long assetId);

    /**
     * @return one message per field that differs between the stored revision and the device
     */
    List<String> compare(long assetId);

    /** Treats what the device reports as correct and closes the mismatch. */
    void acceptDeviceConfiguration(long assetId, String userId);

    /** Treats the stored revision as correct and queues it to be pushed to the device again. */
    void reapplyStoredConfiguration(long assetId, String userId);
}
