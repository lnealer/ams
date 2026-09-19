package org.example.am.shared.domain;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A request to change something about an installed asset's network presence: its addressing, its
 * bandwidth, its physical location or the type of the site it serves.
 *
 * <p>Mirrors {@link Order}'s shipping/installation/contact triad because a change request can also
 * dispatch hardware, but it is scheduled against the NCR calendar rather than the install
 * calendar.</p>
 */
public class NetworkChangeRequest extends BaseDomain {

    private static final long serialVersionUID = 1L;

    private Long networkChangeRequestId;
    private String requestNumber;
    private Long assetId;
    private Long customerId;

    private Set<NetworkChangeRequestType> networkChangeRequestTypes =
            new LinkedHashSet<NetworkChangeRequestType>();
    private NetworkChangeRequestStatusType networkChangeRequestStatusType;

    private Address shippingAddress;
    private Address newInstallationAddress;
    private Contact orderingContact;
    private Contact shippingContact;
    private Contact installationContact;
    private Timeslot moveTimeslot;
    private DueDiligence dueDiligence;

    private Date requestedDate;
    private Date submittedDate;
    private Date scheduledDate;
    private Date completedDate;
    private Date cancelledDate;
    private String cancellationReason;
    private String comments;
    private AssetConfiguration proposedConfiguration;

    public Long getNetworkChangeRequestId() {
        return networkChangeRequestId;
    }

    public void setNetworkChangeRequestId(final Long networkChangeRequestId) {
        this.networkChangeRequestId = networkChangeRequestId;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(final String requestNumber) {
        this.requestNumber = requestNumber;
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

    public Set<NetworkChangeRequestType> getNetworkChangeRequestTypes() {
        return networkChangeRequestTypes;
    }

    public void setNetworkChangeRequestTypes(final Set<NetworkChangeRequestType> networkChangeRequestTypes) {
        this.networkChangeRequestTypes = networkChangeRequestTypes == null
                ? new LinkedHashSet<NetworkChangeRequestType>() : networkChangeRequestTypes;
    }

    public NetworkChangeRequestStatusType getNetworkChangeRequestStatusType() {
        return networkChangeRequestStatusType;
    }

    public void setNetworkChangeRequestStatusType(
            final NetworkChangeRequestStatusType networkChangeRequestStatusType) {
        this.networkChangeRequestStatusType = networkChangeRequestStatusType;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(final Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Address getNewInstallationAddress() {
        return newInstallationAddress;
    }

    public void setNewInstallationAddress(final Address newInstallationAddress) {
        this.newInstallationAddress = newInstallationAddress;
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

    public Timeslot getMoveTimeslot() {
        return moveTimeslot;
    }

    public void setMoveTimeslot(final Timeslot moveTimeslot) {
        this.moveTimeslot = moveTimeslot;
    }

    public DueDiligence getDueDiligence() {
        return dueDiligence;
    }

    public void setDueDiligence(final DueDiligence dueDiligence) {
        this.dueDiligence = dueDiligence;
    }

    public Date getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(final Date requestedDate) {
        this.requestedDate = requestedDate;
    }

    public Date getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(final Date submittedDate) {
        this.submittedDate = submittedDate;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(final Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(final Date completedDate) {
        this.completedDate = completedDate;
    }

    public Date getCancelledDate() {
        return cancelledDate;
    }

    public void setCancelledDate(final Date cancelledDate) {
        this.cancelledDate = cancelledDate;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(final String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public AssetConfiguration getProposedConfiguration() {
        return proposedConfiguration;
    }

    public void setProposedConfiguration(final AssetConfiguration proposedConfiguration) {
        this.proposedConfiguration = proposedConfiguration;
    }

    // ------------------------------------------------------------------
    // Business rules
    // ------------------------------------------------------------------

    public boolean isOpen() {
        if (networkChangeRequestStatusType == null) {
            return false;
        }
        return !NetworkChangeRequestStatusType.COMPLETED.equals(networkChangeRequestStatusType)
                && !NetworkChangeRequestStatusType.CANCELLED.equals(networkChangeRequestStatusType);
    }

    public boolean isMove() {
        return networkChangeRequestTypes.contains(NetworkChangeRequestType.MOVE);
    }

    /**
     * A site type change reshapes the circuit as well as the device, so scheduling has to go
     * through the complex stored procedure that reserves capacity on both calendars at once.
     *
     * @return {@code true} when this request must use the complex scheduling path
     */
    public boolean isComplexScheduling() {
        return networkChangeRequestTypes.contains(NetworkChangeRequestType.SITE_TYPE_CHANGE);
    }

    /**
     * Only a move dispatches an engineer to a new address, so only a move needs the move timeslot.
     */
    public boolean isTimeslotRequired() {
        return isMove();
    }

    public boolean isCancellable() {
        return isOpen()
                && !NetworkChangeRequestStatusType.IN_PROGRESS.equals(networkChangeRequestStatusType);
    }

    public boolean isSubmittable() {
        if (networkChangeRequestTypes.isEmpty()) {
            return false;
        }
        if (dueDiligence != null && !dueDiligence.isSatisfied()) {
            return false;
        }
        if (isTimeslotRequired() && moveTimeslot == null) {
            return false;
        }
        return !isMove() || (newInstallationAddress != null && newInstallationAddress.isComplete());
    }

    /**
     * @return the change types joined for display, e.g. "Move, IP Re-address".
     */
    public String getChangeTypeDescription() {
        final StringBuilder builder = new StringBuilder();
        for (final NetworkChangeRequestType type : networkChangeRequestTypes) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(type.getDescription());
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "NetworkChangeRequest[" + networkChangeRequestId + ", number=" + requestNumber
                + ", status=" + networkChangeRequestStatusType + "]";
    }
}
