package org.example.am.internal.web.action;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.dao.AddressDAO;
import org.example.am.shared.dao.AssetDAO;
import org.example.am.shared.domain.Address;
import org.example.am.shared.domain.AddressType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Corrects the address an asset is recorded as installed at.
 *
 * <p>A correction, not a move: nothing is dispatched and no engineer is booked. Moving an asset to a
 * genuinely different site is a network change request.</p>
 */
@Component("UpdateInstallAddressAction")
@Scope("prototype")
public class UpdateInstallAddressAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long assetId;
    private Long customerId;
    private Address address;

    @Autowired
    private transient AddressDAO addressSharedDAO;

    @Autowired
    private transient AssetDAO assetSharedDAO;

    public String initUpdateInstallAddress() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_UPDATE_INSTALL_ADDRESS);
        if (denied != null) {
            return denied;
        }
        if (assetId == null) {
            return Action.ERROR;
        }
        if (address == null) {
            address = new Address();
        }
        return Action.SUCCESS;
    }

    public String submitUpdateInstallAddress() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_UPDATE_INSTALL_ADDRESS);
        if (denied != null) {
            return denied;
        }
        if (assetId == null || customerId == null) {
            addActionError("No asset was identified.");
            return Action.ERROR;
        }
        if (address == null || !address.isComplete()) {
            addActionError("Fill in the whole address.");
            return Action.INPUT;
        }
        if (address.isOverCarrierLabelLimits()) {
            addActionError("The address is too long to fit a shipping label.");
            return Action.INPUT;
        }
        address.setAddressType(AddressType.INSTALLATION);
        final long addressId = addressSharedDAO.insertAddress(address, getUserId());
        assetSharedDAO.updateInstallationAddress(customerId.longValue(), assetId.longValue(),
                addressId, getUserId());
        addActionMessage("The installation address has been updated.");
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }
}
