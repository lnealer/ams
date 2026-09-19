package org.example.am.internal.web.action;

import java.util.List;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.service.AssetConfigService;
import org.example.am.shared.service.ModifyConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Diffs the stored configuration revision against what the device reports.
 */
@Component("CompareConfigAction")
@Scope("prototype")
public class CompareConfigAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    /** @return {@code true} when the two revisions agree */
    public boolean isMatching() {
        return differences != null && differences.isEmpty();
    }

    private Long assetId;
    private List<String> differences;
    private AssetConfiguration storedConfiguration;
    private AssetConfiguration deviceConfiguration;

    @Autowired
    private transient ModifyConfigurationService modifyConfigurationService;

    @Autowired
    private transient AssetConfigService assetConfigService;

    public String compareConfig() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_COMPARE_CONFIG);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            addActionError("No asset was identified.");
            return Action.ERROR;
        }
        storedConfiguration = assetConfigService.getCurrentConfiguration(assetId.longValue());
        deviceConfiguration =
                modifyConfigurationService.getDeviceReportedConfiguration(assetId.longValue());
        differences = modifyConfigurationService.compare(assetId.longValue());
        return Action.SUCCESS;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public List<String> getDifferences() {
        return differences;
    }

    public void setDifferences(final List<String> differences) {
        this.differences = differences;
    }

    public AssetConfiguration getStoredConfiguration() {
        return storedConfiguration;
    }

    public void setStoredConfiguration(final AssetConfiguration storedConfiguration) {
        this.storedConfiguration = storedConfiguration;
    }

    public AssetConfiguration getDeviceConfiguration() {
        return deviceConfiguration;
    }

    public void setDeviceConfiguration(final AssetConfiguration deviceConfiguration) {
        this.deviceConfiguration = deviceConfiguration;
    }
}
