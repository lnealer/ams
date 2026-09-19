package org.example.am.internal.web.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import org.example.am.shared.domain.Address;
import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.DueDiligence;
import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.domain.NetworkChangeRequestType;

/**
 * The form backing the network change request flow.
 *
 * <p>The selected change types drive everything downstream: whether an address is needed, whether a
 * move timeslot has to be picked, and which scheduling procedure the service layer will call.</p>
 */
public class NetworkChangeRequestModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private Long assetId;
    private Set<NetworkChangeRequestType> changeTypes;
    private Address newInstallationAddress;
    private Address shippingAddress;
    private Contact orderingContact;
    private Contact shippingContact;
    private Contact installationContact;
    private Long moveTimeslotId;
    private Date requestedDate;
    private String comments;
    private boolean dueDiligenceCompleted;
    private String dueDiligenceWaiverReason;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public Set<NetworkChangeRequestType> getChangeTypes() {
        return changeTypes;
    }

    public void setChangeTypes(final Set<NetworkChangeRequestType> changeTypes) {
        this.changeTypes = changeTypes;
    }

    public Address getNewInstallationAddress() {
        return newInstallationAddress;
    }

    public void setNewInstallationAddress(final Address newInstallationAddress) {
        this.newInstallationAddress = newInstallationAddress;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(final Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Contact getOrderingContact() {
        return orderingContact;
    }

    public void setOrderingContact(final Contact orderingContact) {
        this.orderingContact = orderingContact;
    }

    public Contact getShippingContact() {
        return shippingContact;
    }

    public void setShippingContact(final Contact shippingContact) {
        this.shippingContact = shippingContact;
    }

    public Contact getInstallationContact() {
        return installationContact;
    }

    public void setInstallationContact(final Contact installationContact) {
        this.installationContact = installationContact;
    }

    public Long getMoveTimeslotId() {
        return moveTimeslotId;
    }

    public void setMoveTimeslotId(final Long moveTimeslotId) {
        this.moveTimeslotId = moveTimeslotId;
    }

    public Date getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(final Date requestedDate) {
        this.requestedDate = requestedDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public boolean isDueDiligenceCompleted() {
        return dueDiligenceCompleted;
    }

    public void setDueDiligenceCompleted(final boolean dueDiligenceCompleted) {
        this.dueDiligenceCompleted = dueDiligenceCompleted;
    }

    public String getDueDiligenceWaiverReason() {
        return dueDiligenceWaiverReason;
    }

    public void setDueDiligenceWaiverReason(final String dueDiligenceWaiverReason) {
        this.dueDiligenceWaiverReason = dueDiligenceWaiverReason;
    }

    /** @return {@code true} when the request moves the asset to a new address */
    public boolean isMove() {
        return changeTypes != null
                && changeTypes.contains(NetworkChangeRequestType.MOVE);
    }

    /**
     * @return {@code true} when the request includes a site type change, which is what sends the
     *         scheduling down the complex path
     */
    public boolean isComplexScheduling() {
        return changeTypes != null
                && changeTypes.contains(NetworkChangeRequestType.SITE_TYPE_CHANGE);
    }

    /** Builds the domain object at submit time. */
    public NetworkChangeRequest toNetworkChangeRequest() {
        final NetworkChangeRequest request = new NetworkChangeRequest();
        request.setCustomerId(customerId);
        request.setAssetId(assetId);
        request.setNetworkChangeRequestTypes(changeTypes == null
                ? new LinkedHashSet<NetworkChangeRequestType>() : changeTypes);
        request.setNewInstallationAddress(newInstallationAddress);
        request.setShippingAddress(shippingAddress);
        request.setOrderingContact(orderingContact);
        request.setShippingContact(shippingContact);
        request.setInstallationContact(installationContact);
        request.setRequestedDate(requestedDate);
        request.setComments(comments);

        final DueDiligence dueDiligence = new DueDiligence();
        dueDiligence.setCompleted(dueDiligenceCompleted);
        if (!dueDiligenceCompleted && dueDiligenceWaiverReason != null
                && dueDiligenceWaiverReason.trim().length() > 0) {
            dueDiligence.setWaived(true);
            dueDiligence.setWaiverReason(dueDiligenceWaiverReason.trim());
        }
        request.setDueDiligence(dueDiligence);
        return request;
    }

}
