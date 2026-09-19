package org.example.am.shared.dao;

import java.util.List;

import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.PortConfiguration;

/** Reads and writes {@code AMS_ASSET_CONFIGS} and its port child table. */
public interface AssetConfigDAO {

    /** @return the current revision, i.e. the highest numbered non superseded row */
    AssetConfiguration getCurrentConfiguration(long assetId);

    List<AssetConfiguration> getConfigurationHistory(long assetId);

    List<PortConfiguration> getPortConfigurations(long configurationId);

    /** Writes a new revision rather than updating in place. @return the generated configuration id */
    long insertConfigurationRevision(AssetConfiguration configuration, String userId);

    int markSuperseded(long configurationId, String userId);

    AssetConfiguration getConfigurationForOrder(long orderId);

    /**
     * Every non superseded configuration this customer has, newest first.
     *
     * <p>Read whole rather than as a set of summary queries because the suggestions drawn from it
     * are interdependent: the LAN block has to avoid every site they already run, and the WAN
     * address has to avoid every address already handed out of the delegated subnet. Two sites
     * numbered identically is not an error AMS can detect later - it surfaces as unreachable hosts
     * once the customer routes between them.</p>
     */
    List<AssetConfiguration> getConfigurationsForCustomer(long customerId);

    /**
     * Every WAN address configured anywhere, for any customer.
     *
     * <p>Customer scope is the wrong scope for this one. A WAN address is unique on the carrier's
     * network, so handing the same one to two different customers is the failure that matters, and
     * it is invisible from inside either customer's own estate.</p>
     */
    List<String> getWanAddressesInUse();

    /**
     * Moves an order-time configuration onto the asset it now describes.
     *
     * <p>The row was written with ORDER_ID set and ASSET_ID null because no device existed when it
     * was keyed. Despatch is where the device appears, so this is where the row acquires it.</p>
     */
    int attachConfigurationToAsset(long configurationId, long assetId, String userId);

    /** Marks the revision live once an engineer has confirmed it on site. */
    int markApplied(long configurationId, String userId);

    /**
     * Writes the external configuration keyed while ordering.
     *
     * <p>Stored against the order with a null {@code ASSET_ID}: the device is not created until it
     * is despatched, but the warehouse stages it from this revision before that happens.</p>
     *
     * @return the generated configuration id
     */
    long insertOrderConfiguration(AssetConfiguration configuration, long orderId, String userId);
}
