package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Return Merchandise Authorisation raised when hardware goes back to the depot.
 *
 * <p>Flat by design: the RMA grid is populated by a single projection query and nothing on this
 * object is lazily resolved.</p>
 */
public class Rma implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long rmaId;
    private String rmaNumber;
    private Long assetId;
    private String assetTag;
    private List<String> serialNumbers;
    private String trackingNumber;
    private ShippingCarrier shippingCarrier;
    private RmaStatusType rmaStatusType;
    private Date issuedDate;
    private Date dueDate;
    private Date receivedDate;
    private String reason;
    private String returnAddressLabelUrl;

    public Long getRmaId() {
        return rmaId;
    }

    public void setRmaId(final Long rmaId) {
        this.rmaId = rmaId;
    }

    public String getRmaNumber() {
        return rmaNumber;
    }

    public void setRmaNumber(final String rmaNumber) {
        this.rmaNumber = rmaNumber;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(final String assetTag) {
        this.assetTag = assetTag;
    }

    public List<String> getSerialNumbers() {
        return serialNumbers;
    }

    public void setSerialNumbers(final List<String> serialNumbers) {
        this.serialNumbers = serialNumbers;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(final String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public ShippingCarrier getShippingCarrier() {
        return shippingCarrier;
    }

    public void setShippingCarrier(final ShippingCarrier shippingCarrier) {
        this.shippingCarrier = shippingCarrier;
    }

    public RmaStatusType getRmaStatusType() {
        return rmaStatusType;
    }

    public void setRmaStatusType(final RmaStatusType rmaStatusType) {
        this.rmaStatusType = rmaStatusType;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(final Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(final Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(final Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public String getReturnAddressLabelUrl() {
        return returnAddressLabelUrl;
    }

    public void setReturnAddressLabelUrl(final String returnAddressLabelUrl) {
        this.returnAddressLabelUrl = returnAddressLabelUrl;
    }

    /**
     * @param now request-scoped clock
     * @return {@code true} when the hardware has not come back inside the return window.
     */
    public boolean isOverdue(final Date now) {
        if (receivedDate != null || dueDate == null || now == null) {
            return false;
        }
        return now.after(dueDate);
    }

}
