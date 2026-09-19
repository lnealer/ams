package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.service.NetworkChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Every change request ever raised against an asset.
 */
@Component("NetworkChangeRequestHistoryAction")
@Scope("prototype")
public class NetworkChangeRequestHistoryAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long assetId;
    private List<NetworkChangeRequest> requests;

    @Autowired
    private transient NetworkChangeRequestService networkChangeRequestService;

    public String viewHistory() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_NCR_HISTORY);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            requests = new ArrayList<NetworkChangeRequest>();
            return Action.SUCCESS;
        }
        requests = networkChangeRequestService.getHistoryForAsset(assetId.longValue());
        return Action.SUCCESS;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public List<NetworkChangeRequest> getRequests() {
        return requests;
    }

    public void setRequests(final List<NetworkChangeRequest> requests) {
        this.requests = requests;
    }
}
