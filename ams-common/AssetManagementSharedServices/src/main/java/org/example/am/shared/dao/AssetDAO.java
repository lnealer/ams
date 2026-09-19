package org.example.am.shared.dao;

import java.util.List;

import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetStatusType;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.domain.Decommission;
import org.example.am.shared.domain.Order;

/**
 * Reads and writes {@code AMS_ASSETS} and the rows that hang off it.
 *
 * <p>Every lookup is scoped by customer as well as by asset. The internal application can act on
 * behalf of any customer, so the customer id is never implied from the session: it is passed in and
 * carried into the {@code WHERE} clause, which means a mistyped asset id returns nothing rather
 * than another customer's hardware.</p>
 */
public interface AssetDAO {

    Asset getAsset(long customerId, long assetId);

    Asset getAssetByAssetTag(long customerId, String assetTag);

    Asset getAssetBySerialNumber(String serialNumber);

    /**
     * @return the most recent order raised against this asset, or {@code null} when the asset has
     *         never been ordered through AMS
     */
    Order getOrderForAsset(long customerId, long assetId);

    AssetStatusType getAssetStatus(long customerId, long assetId);

    List<Asset> getAssetsForCustomer(long customerId);

    /**
     * @return the customer's assets that a migration order may be raised against, with the
     *         migration blocking flags already resolved
     */
    List<Asset> getMigratableAssetsForCustomer(Customer customer);

    Decommission getDecommissionForAsset(long customerId, long assetId);

    /**
     * Records that hardware is on its way back to the depot.
     *
     * @return the generated asset return id
     */
    /**
     * Creates the asset a despatched order becomes.
     *
     * <p>Until provisioning existed nothing inserted into {@code AMS_ASSETS} at all - every asset
     * in the system arrived through the seed or an external feed. The tag and serial are generated
     * here from {@code AMS_ASSETS_SQ} rather than keyed, so they are unique by construction.</p>
     *
     * @return the generated asset id, with the tag and serial set on the passed asset
     */
    long insertAsset(Asset asset, long customerId, String userId);

    long addAssetReturn(long assetId, String serialNumber, String userId);

    int updateAssetStatus(long customerId, long assetId, AssetStatusType status, String userId);

    int updateInstallationAddress(long customerId, long assetId, long addressId, String userId);
}
