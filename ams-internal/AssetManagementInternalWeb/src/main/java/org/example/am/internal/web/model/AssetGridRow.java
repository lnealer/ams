package org.example.am.internal.web.model;

import java.io.Serializable;

import org.example.am.shared.domain.Asset;

/**
 * One row of the asset search grid.
 *
 * <p>A flat projection rather than the {@link Asset} itself: the grid is rendered by a client-side
 * Dojo store from JSON, and serialising the whole asset graph would drag in the customer, the order
 * and the configuration for every row.</p>
 *
 * <p>Nothing here is pre-rendered HTML. The grid builds its own links from {@link #getAssetId()}
 * and the identifiers, so a value that happens to contain markup is escaped by the grid rather
 * than being trusted as markup.</p>
 */
public class AssetGridRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long assetId;
    private Long customerId;
    private String assetTag;
    private String legacyAssetTag;
    private String serialNumber;
    private String assetTypeDescription;
    private String statusDescription;
    private String statusCode;
    private String customerName;
    private boolean needsAttention;
    private String attentionReason;

    public static AssetGridRow from(final Asset asset) {
        final AssetGridRow row = new AssetGridRow();
        row.assetId = asset.getAssetId();
        row.assetTag = asset.getDisplayTag();
        row.legacyAssetTag = asset.getLegacyAssetTag();
        row.serialNumber = asset.getSerialNumber();
        row.assetTypeDescription =
                asset.getAssetType() == null ? null : asset.getAssetType().getDescription();
        row.statusDescription = asset.getAssetStatusType() == null
                ? null : asset.getAssetStatusType().getDescription();
        row.statusCode =
                asset.getAssetStatusType() == null ? null : asset.getAssetStatusType().getCode();
        row.needsAttention = asset.isNeedsAttention();
        row.attentionReason = asset.getAttentionReason();
        if (asset.getCustomer() != null) {
            row.customerId = asset.getCustomer().getCustomerId();
            row.customerName = asset.getCustomer().getCustomerName();
        }
        return row;
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

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(final String assetTag) {
        this.assetTag = assetTag;
    }

    public String getLegacyAssetTag() {
        return legacyAssetTag;
    }

    public void setLegacyAssetTag(final String legacyAssetTag) {
        this.legacyAssetTag = legacyAssetTag;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getAssetTypeDescription() {
        return assetTypeDescription;
    }

    public void setAssetTypeDescription(final String assetTypeDescription) {
        this.assetTypeDescription = assetTypeDescription;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(final String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(final String statusCode) {
        this.statusCode = statusCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(final String customerName) {
        this.customerName = customerName;
    }

    public boolean isNeedsAttention() {
        return needsAttention;
    }

    public void setNeedsAttention(final boolean needsAttention) {
        this.needsAttention = needsAttention;
    }

    public String getAttentionReason() {
        return attentionReason;
    }

    public void setAttentionReason(final String attentionReason) {
        this.attentionReason = attentionReason;
    }
}
