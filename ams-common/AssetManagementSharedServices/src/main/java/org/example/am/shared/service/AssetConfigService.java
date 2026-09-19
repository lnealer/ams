package org.example.am.shared.service;

import java.util.List;
import org.example.am.shared.domain.AssetConfiguration;

/**
 * Reads and writes asset configuration revisions, validating the network settings before a
 * revision is stored.
 */
public interface AssetConfigService {

    AssetConfiguration getCurrentConfiguration(long assetId);

    List<AssetConfiguration> getHistory(long assetId);

    /**
     * @return the validation messages for this configuration; empty when it may be stored
     */
    List<String> validate(AssetConfiguration configuration);

    /**
     * @throws IllegalArgumentException when the configuration does not validate
     */
    long saveRevision(AssetConfiguration configuration, String userId);
}
