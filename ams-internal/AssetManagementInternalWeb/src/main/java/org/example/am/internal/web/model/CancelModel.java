package org.example.am.internal.web.model;

import java.io.Serializable;
import java.util.Date;

/**
 * The cancellation form, shared by the order and modify-configuration flows.
 */
public class CancelModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private Long orderId;
    private Long configurationId;
    private Long assetId;
    private String reason;
    private boolean penaltyIncurred;
    private Date scheduledInstallationDate;
    private int minimumHoursToCancelWithoutPenalty;
    private String orderNumber;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public Long getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(final Long configurationId) {
        this.configurationId = configurationId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public boolean isPenaltyIncurred() {
        return penaltyIncurred;
    }

    public void setPenaltyIncurred(final boolean penaltyIncurred) {
        this.penaltyIncurred = penaltyIncurred;
    }

    public Date getScheduledInstallationDate() {
        return scheduledInstallationDate;
    }

    public void setScheduledInstallationDate(final Date scheduledInstallationDate) {
        this.scheduledInstallationDate = scheduledInstallationDate;
    }

    public int getMinimumHoursToCancelWithoutPenalty() {
        return minimumHoursToCancelWithoutPenalty;
    }

    public void setMinimumHoursToCancelWithoutPenalty(final int minimumHoursToCancelWithoutPenalty) {
        this.minimumHoursToCancelWithoutPenalty = minimumHoursToCancelWithoutPenalty;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * @return the warning shown above the confirm button, or {@code null} when cancelling now is
     *         free
     */
    public String getPenaltyWarning() {
        if (!penaltyIncurred) {
            return null;
        }
        return "This order is within " + minimumHoursToCancelWithoutPenalty
                + " hours of its scheduled installation. Cancelling now will incur the"
                + " cancellation charge.";
    }

}
