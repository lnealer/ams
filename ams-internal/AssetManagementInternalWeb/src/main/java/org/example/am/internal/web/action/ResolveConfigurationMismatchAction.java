package org.example.am.internal.web.action;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.service.ModifyConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Resolves a mismatch, in whichever direction the operator judges correct.
 *
 * <p>Two outcomes, and the choice is a judgement only a person can make: either the device is right
 * and AMS should record what it reports, or AMS is right and the configuration should be pushed to
 * the device again.</p>
 */
@Component("ResolveConfigurationMismatchAction")
@Scope("prototype")
public class ResolveConfigurationMismatchAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long assetId;
    private String resolution;

    @Autowired
    private transient ModifyConfigurationService modifyConfigurationService;

    public String acceptDevice() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_RESOLVE_CONFIG_MISMATCH);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            return Action.ERROR;
        }
        modifyConfigurationService.acceptDeviceConfiguration(assetId.longValue(), getUserId());
        addActionMessage("The configuration AMS holds now matches what the device reports.");
        logger.info("User {} accepted the device configuration for asset {}", getUserId(), assetId);
        return Action.SUCCESS;
    }

    public String reapplyStored() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_RESOLVE_CONFIG_MISMATCH);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            return Action.ERROR;
        }
        modifyConfigurationService.reapplyStoredConfiguration(assetId.longValue(), getUserId());
        addActionMessage("The stored configuration has been queued to be applied to the device.");
        logger.info("User {} queued a re-apply for asset {}", getUserId(), assetId);
        return Action.SUCCESS;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(final String resolution) {
        this.resolution = resolution;
    }
}
