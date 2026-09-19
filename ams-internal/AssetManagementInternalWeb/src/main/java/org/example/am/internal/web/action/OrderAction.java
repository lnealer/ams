package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.PortConfiguration;
import org.example.am.shared.service.AssetConfigService;
import org.example.am.shared.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The order screen's data endpoint.
 *
 * <p>Answers the whole asset and network configuration as JSON, which is what the legacy configuration
 * form binds to. It is a read: every field is a projection of the stored configuration, and nothing
 * here writes.</p>
 */
@Component("OrderAction")
@Scope("prototype")
public class OrderAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long assetId;
    private Long customerId;
    private Asset asset;
    private AssetConfiguration configuration;
    private List<PortConfiguration> ports;

    @Autowired
    private transient AssetService assetService;

    @Autowired
    private transient AssetConfigService assetConfigService;

    public String getLegacyAssetConfig() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_ASSET_CONFIG_HISTORY);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            return Action.ERROR;
        }
        final long customer = customerId == null
                ? (getCurrentCustomerId() == null ? 0L : getCurrentCustomerId().longValue())
                : customerId.longValue();
        asset = assetService.getAssetDetail(customer, assetId.longValue());
        if (asset == null) {
            addActionError("That asset could not be found for this customer.");
            return Action.ERROR;
        }
        configuration = assetConfigService.getCurrentConfiguration(assetId.longValue());
        ports = configuration == null || configuration.getPortConfigurations() == null
                ? new ArrayList<PortConfiguration>() : configuration.getPortConfigurations();
        return Action.SUCCESS;
    }

    public String modifyConfiguration() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_MODIFY_CONFIG);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            return Action.ERROR;
        }
        configuration = assetConfigService.getCurrentConfiguration(assetId.longValue());
        if (configuration == null) {
            addActionError("This asset has no configuration to modify.");
            return Action.ERROR;
        }
        return Action.SUCCESS;
    }

    public String moveAsset() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_MOVE_ASSET);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            return Action.ERROR;
        }
        final long customer = customerId == null
                ? (getCurrentCustomerId() == null ? 0L : getCurrentCustomerId().longValue())
                : customerId.longValue();
        asset = assetService.getAssetDetail(customer, assetId.longValue());
        if (asset == null || !asset.isCanMove()) {
            addActionError("This asset cannot be moved at the moment.");
            return Action.ERROR;
        }
        return Action.SUCCESS;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(final Asset asset) {
        this.asset = asset;
    }

    public AssetConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final AssetConfiguration configuration) {
        this.configuration = configuration;
    }

    public List<PortConfiguration> getPorts() {
        return ports;
    }

    public void setPorts(final List<PortConfiguration> ports) {
        this.ports = ports;
    }
}
