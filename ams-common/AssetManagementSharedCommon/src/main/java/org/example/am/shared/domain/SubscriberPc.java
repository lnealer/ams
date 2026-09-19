package org.example.am.shared.domain;

import java.io.Serializable;

/**
 * One subscriber machine that will sit behind the ordered device.
 *
 * <p>Collected at order time rather than at install: the LAN addressing on the order's
 * {@link AssetConfiguration} has to be sized before the device is staged, and the engineer needs
 * the list in hand when they arrive. A row with a static address also reserves that address, which
 * is why {@link #isStaticAddress()} and {@link #getIpAddress()} are validated together.</p>
 */
public class SubscriberPc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long subscriberPcId;
    private Long orderId;
    private String hostName;
    private SubscriberPcType subscriberPcType;
    private String operatingSystem;
    private String macAddress;
    private String ipAddress;
    private boolean staticAddress;
    private Integer userCount;
    private String notes;

    public Long getSubscriberPcId() {
        return subscriberPcId;
    }

    public void setSubscriberPcId(final Long subscriberPcId) {
        this.subscriberPcId = subscriberPcId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public SubscriberPcType getSubscriberPcType() {
        return subscriberPcType;
    }

    public void setSubscriberPcType(final SubscriberPcType subscriberPcType) {
        this.subscriberPcType = subscriberPcType;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(final String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isStaticAddress() {
        return staticAddress;
    }

    public void setStaticAddress(final boolean staticAddress) {
        this.staticAddress = staticAddress;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(final Integer userCount) {
        this.userCount = userCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    /**
     * The type as a plain code, for form binding.
     *
     * <p>{@link SubscriberPcType} is a legacy typesafe enum with a private constructor, so a form
     * cannot bind {@code subscriberPcType.code} - OGNL would have to instantiate one and cannot.
     * This pair is what the drop-down binds to instead.</p>
     */
    public String getPcTypeCode() {
        return subscriberPcType == null ? null : subscriberPcType.getCode();
    }

    public void setPcTypeCode(final String pcTypeCode) {
        this.subscriberPcType = SubscriberPcType.lookup(pcTypeCode);
    }

    /**
     * @return {@code true} when nothing at all has been keyed on this row
     *
     * <p>The subscriber PC form renders a fixed number of blank rows, so most of them come back
     * empty. Treating an untouched row as "not a PC" rather than as an invalid one is what lets
     * the user fill in three of eight and carry on.</p>
     */
    public boolean isBlank() {
        return isEmpty(hostName) && subscriberPcType == null && isEmpty(operatingSystem)
                && isEmpty(macAddress) && isEmpty(ipAddress) && userCount == null
                && isEmpty(notes);
    }

    /** @return the name to show when this PC is listed, falling back to its type */
    public String getDisplayLabel() {
        if (!isEmpty(hostName)) {
            return hostName.trim();
        }
        return subscriberPcType == null ? "" : subscriberPcType.getDescription();
    }

    private static boolean isEmpty(final String value) {
        return value == null || value.trim().length() == 0;
    }
}
