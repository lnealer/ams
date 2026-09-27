package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.AssetService;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.service.AssetAttentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Serves the dashboard's attention queue, and clears a flag once an operator has dealt with it.
 */
@Component("AjaxAssetAttentionAction")
@Scope("prototype")
public class AjaxAssetAttentionAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private static final int MAX_ROWS = 50;

    private List<Asset> assets;
    private Long assetId;
    private boolean cleared;

    @Autowired
    private transient AssetService internalAssetService;

    @Autowired
    private transient AssetAttentionService assetAttentionService;

    public String getAttentionQueue() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_ATTENTION_QUEUE);
        if (denied != null) {
            return denied;
        }
        assets = internalAssetService.getAttentionQueue(MAX_ROWS);
        return Action.SUCCESS;
    }

    public String clearAttentionFlag() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CLEAR_ATTENTION_FLAG);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            return Action.ERROR;
        }
        assetAttentionService.clear(assetId.longValue(), getUserId());
        cleared = true;
        return Action.SUCCESS;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(final List<Asset> assets) {
        this.assets = assets;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(final boolean cleared) {
        this.cleared = cleared;
    }
}
