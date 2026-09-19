package org.example.am.internal.web.action;

import java.util.List;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.action.util.AssetStatusHelper;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetActionType;
import org.example.am.shared.helper.AssetHelper;
import org.example.am.shared.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The asset detail screen.
 */
@Component("AssetAction")
@Scope("prototype")
public class AssetAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private long resolveCustomerId() {
        if (customerId != null) {
            return customerId.longValue();
        }
        final Long sessionCustomerId = getCurrentCustomerId();
        return sessionCustomerId == null ? 0L : sessionCustomerId.longValue();
    }

    private Asset asset;
    private List<AssetActionType> availableActions;
    private Long assetId;
    private Long customerId;
    private String rowStyleClass;
    private String statusExplanation;

    @Autowired
    private transient AssetService assetService;

    public String viewAsset() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_ASSET);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            addActionError("No asset was identified.");
            return Action.ERROR;
        }
        asset = assetService.getAssetDetail(resolveCustomerId(), assetId.longValue());
        if (asset == null) {
            addActionError("That asset could not be found for this customer.");
            return Action.ERROR;
        }
        asset.setCurrentTime(getCurrentTime());
        availableActions = AssetHelper.getAvailableActions(asset);
        rowStyleClass = AssetStatusHelper.getRowStyleClass(asset);
        statusExplanation = AssetStatusHelper.getStatusExplanation(asset);
        return Action.SUCCESS;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(final Asset asset) {
        this.asset = asset;
    }

    public List<AssetActionType> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(final List<AssetActionType> availableActions) {
        this.availableActions = availableActions;
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

    public String getRowStyleClass() {
        return rowStyleClass;
    }

    public void setRowStyleClass(final String rowStyleClass) {
        this.rowStyleClass = rowStyleClass;
    }

    public String getStatusExplanation() {
        return statusExplanation;
    }

    public void setStatusExplanation(final String statusExplanation) {
        this.statusExplanation = statusExplanation;
    }
}
