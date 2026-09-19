package org.example.am.internal.service;

import java.util.List;

import org.example.am.shared.domain.Asset;

/** Internal-only asset reads for the operator screens. */
public interface AssetService {

    /** @return the assets flagged for attention across every customer, newest first */
    List<Asset> getAttentionQueue(int maxRows);

    /**
     * @return other assets already installed at this postcode, which the move screen shows so an
     *         operator can spot a duplicate before dispatching an engineer
     */
    List<Asset> getAssetsAtZipCode(String zipCode);

    int getInstalledAssetCount(long customerId);
}
