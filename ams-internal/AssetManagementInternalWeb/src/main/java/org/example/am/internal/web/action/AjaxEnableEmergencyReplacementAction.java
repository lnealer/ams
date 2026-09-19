package org.example.am.internal.web.action;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.dao.EmergencyReplacementDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Turns the expedited replacement path on or off for one asset.
 *
 * <p>Gated by its own role because it bypasses the normal lead time, which has a real cost.</p>
 */
@Component("AjaxEnableEmergencyReplacementAction")
@Scope("prototype")
public class AjaxEnableEmergencyReplacementAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long assetId;
    private boolean enabled;
    private boolean updated;

    @Autowired
    private transient EmergencyReplacementDAO emergencyReplacementSharedDAO;

    public String toggle() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_ENABLE_EMERGENCY_REPLACEMENT);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            return Action.ERROR;
        }
        updated = emergencyReplacementSharedDAO.setEnabled(assetId.longValue(), enabled,
                getUserId()) > 0;
        logger.info("User {} {} emergency replacement for asset {}", getUserId(),
                enabled ? "enabled" : "disabled", assetId);
        return Action.SUCCESS;
    }

    public String isEnabledForAsset() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_ASSET);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            return Action.ERROR;
        }
        enabled = emergencyReplacementSharedDAO.isEnabled(assetId.longValue());
        return Action.SUCCESS;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(final boolean updated) {
        this.updated = updated;
    }
}
