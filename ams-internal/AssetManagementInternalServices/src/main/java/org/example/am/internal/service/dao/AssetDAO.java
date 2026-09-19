package org.example.am.internal.service.dao;

import java.util.List;

import org.example.am.shared.domain.Asset;

/**
 * Internal-only asset reads.
 *
 * <p>The shared library's {@code AssetDAO} covers the reads both applications make; this one adds
 * the operator screens' projections, which reach across customers.</p>
 */
public interface AssetDAO {

    /** @return the assets an operator should look at first, across every customer */
    List<Asset> getAssetsNeedingAttention(int maxRows);

    /** @return every asset installed at the given postcode, for the move screen's sanity check */
    List<Asset> getAssetsAtZipCode(String zipCode);

    int getInstalledAssetCount(long customerId);
}
