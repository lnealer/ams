package org.example.am.internal.web.action;

import java.util.List;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.AssetService;
import org.example.am.shared.domain.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Shows the assets already installed at an address.
 *
 * <p>Rendered on the move and new-order screens so an operator can spot that the site already has
 * hardware before dispatching an engineer to install more.</p>
 */
@Component("AssetInstallationsAction")
@Scope("prototype")
public class AssetInstallationsAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private List<Asset> assets;
    private String zipCode;

    @Autowired
    private transient AssetService internalAssetService;

    public String getInstallations() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_ASSET);
        if (denied != null) {
            return denied;
        }
        assets = internalAssetService.getAssetsAtZipCode(zipCode);
        return Action.SUCCESS;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(final List<Asset> assets) {
        this.assets = assets;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(final String zipCode) {
        this.zipCode = zipCode;
    }
}
