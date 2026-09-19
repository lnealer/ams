package org.example.am.shared.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A piece of managed hardware tracked through its whole life: ordered, shipped, installed, moved,
 * reconfigured and finally decommissioned.
 *
 * <p>The {@code isCanXxx()} methods are the single source of truth for whether the internal UI may
 * offer an action. Both the Struts actions and the JSPs call them, so a rule only ever has to
 * change here.</p>
 */
public class Asset extends BaseDomain {

    private static final long serialVersionUID = 1L;

    private Long assetId;
    private AssetType assetType;
    private String assetTag;
    private String serialNumber;
    private String legacyAssetTag;
    private AssetStatusType assetStatusType;
    private DataSourceType dataSourceType;

    private Installation installation;
    private Address installationAddress;
    private Contact contact;
    private AssetConfiguration assetConfiguration;
    private MaintenanceWindow maintenanceWindow;
    private Order order;
    private NetworkChangeRequest networkChangeRequest;
    private Asset replacementAsset;
    private Decommission decommission;
    private Customer customer;

    private boolean emergencyReplacementEnabled;
    private boolean migratable;
    private boolean needsAttention;
    private String attentionReason;

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(final AssetType assetType) {
        this.assetType = assetType;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(final String assetTag) {
        this.assetTag = assetTag;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getLegacyAssetTag() {
        return legacyAssetTag;
    }

    public void setLegacyAssetTag(final String legacyAssetTag) {
        this.legacyAssetTag = legacyAssetTag;
    }

    public AssetStatusType getAssetStatusType() {
        return assetStatusType;
    }

    public void setAssetStatusType(final AssetStatusType assetStatusType) {
        this.assetStatusType = assetStatusType;
    }

    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(final DataSourceType dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public Installation getInstallation() {
        return installation;
    }

    public void setInstallation(final Installation installation) {
        this.installation = installation;
    }

    public Address getInstallationAddress() {
        return installationAddress;
    }

    public void setInstallationAddress(final Address installationAddress) {
        this.installationAddress = installationAddress;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(final Contact contact) {
        this.contact = contact;
    }

    public AssetConfiguration getAssetConfiguration() {
        return assetConfiguration;
    }

    public void setAssetConfiguration(final AssetConfiguration assetConfiguration) {
        this.assetConfiguration = assetConfiguration;
    }

    public MaintenanceWindow getMaintenanceWindow() {
        return maintenanceWindow;
    }

    public void setMaintenanceWindow(final MaintenanceWindow maintenanceWindow) {
        this.maintenanceWindow = maintenanceWindow;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(final Order order) {
        this.order = order;
    }

    public NetworkChangeRequest getNetworkChangeRequest() {
        return networkChangeRequest;
    }

    public void setNetworkChangeRequest(final NetworkChangeRequest networkChangeRequest) {
        this.networkChangeRequest = networkChangeRequest;
    }

    public Asset getReplacementAsset() {
        return replacementAsset;
    }

    public void setReplacementAsset(final Asset replacementAsset) {
        this.replacementAsset = replacementAsset;
    }

    public Decommission getDecommission() {
        return decommission;
    }

    public void setDecommission(final Decommission decommission) {
        this.decommission = decommission;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    public boolean isEmergencyReplacementEnabled() {
        return emergencyReplacementEnabled;
    }

    public void setEmergencyReplacementEnabled(final boolean emergencyReplacementEnabled) {
        this.emergencyReplacementEnabled = emergencyReplacementEnabled;
    }

    public boolean isMigratable() {
        return migratable;
    }

    public void setMigratable(final boolean migratable) {
        this.migratable = migratable;
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

    // ------------------------------------------------------------------
    // Business rules
    // ------------------------------------------------------------------

    /**
     * An asset is in service once it has been installed and has not started down the
     * decommission path.
     */
    public boolean isInService() {
        return AssetStatusType.INSTALLED.equals(assetStatusType)
                || AssetStatusType.ACTIVE.equals(assetStatusType);
    }

    /**
     * Decommission is offered only for an in-service asset with nothing else already in flight
     * against it. A decommission that has been requested but cancelled does not block a new one.
     */
    public boolean isCanDecommission() {
        if (!isInService()) {
            return false;
        }
        if (hasOpenNetworkChangeRequest() || hasOpenOrder()) {
            return false;
        }
        return decommission == null || !decommission.isCancellable();
    }

    /**
     * A move is a network change request, so it is blocked by anything else in flight and by a
     * configuration the device has not accepted yet.
     */
    public boolean isCanMove() {
        if (!isInService()) {
            return false;
        }
        if (hasOpenNetworkChangeRequest() || hasOpenOrder()) {
            return false;
        }
        if (decommission != null && decommission.isCancellable()) {
            return false;
        }
        return assetConfiguration == null || !assetConfiguration.isMismatched();
    }

    /**
     * Configuration edits are allowed while the asset is in service and no other change is pending,
     * but unlike a move they are still allowed when the stored revision is mismatched - resolving
     * that mismatch <em>is</em> a configuration edit.
     */
    public boolean isCanModifyConfig() {
        if (!isInService()) {
            return false;
        }
        if (hasOpenNetworkChangeRequest()) {
            return false;
        }
        return decommission == null || !decommission.isCancellable();
    }

    public boolean isCanCreateRma() {
        return AssetStatusType.RETURNED.equals(assetStatusType)
                || AssetStatusType.IN_REPAIR.equals(assetStatusType)
                || (decommission != null && decommission.isHardwareReturnRequired());
    }

    private boolean hasOpenOrder() {
        return order != null && order.isOpen();
    }

    private boolean hasOpenNetworkChangeRequest() {
        return networkChangeRequest != null && networkChangeRequest.isOpen();
    }

    /**
     * Ports that are not carrying traffic are dropped back to auto-negotiate before a configuration
     * is written, so a stale hard-coded speed cannot survive a hardware swap.
     */
    public void setInactivePortConfigurationTypesToAuto() {
        if (assetConfiguration == null || assetConfiguration.getPortConfigurations() == null) {
            return;
        }
        for (final PortConfiguration port : assetConfiguration.getPortConfigurations()) {
            if (!port.isActive()) {
                port.setPortConfigurationType(PortConfigurationType.AUTO);
            }
        }
    }

    /**
     * MAC addresses belong to the physical unit, not to the configuration, so they are wiped when a
     * configuration is cloned onto replacement hardware.
     */
    public void clearAllMacAddresses() {
        if (assetConfiguration == null || assetConfiguration.getPortConfigurations() == null) {
            return;
        }
        for (final PortConfiguration port : assetConfiguration.getPortConfigurations()) {
            port.setMacAddress(null);
        }
    }

    /**
     * @return every reason this asset cannot take part in a migration order; empty when it can.
     */
    public List<AssetProblemType> getMigrationProblems() {
        final List<AssetProblemType> problems = new ArrayList<AssetProblemType>();
        if (!migratable) {
            problems.add(AssetProblemType.NOT_MIGRATABLE);
        }
        if (hasOpenOrder()) {
            problems.add(AssetProblemType.PENDING_ORDER);
        }
        if (hasOpenNetworkChangeRequest()) {
            problems.add(AssetProblemType.PENDING_NCR);
        }
        if (decommission != null && decommission.isCancellable()) {
            problems.add(AssetProblemType.DECOMMISSION_SCHEDULED);
        }
        if (assetConfiguration != null && assetConfiguration.isMismatched()) {
            problems.add(AssetProblemType.CONFIG_MISMATCH);
        }
        if (customer != null && !customer.hasActiveService()) {
            problems.add(AssetProblemType.NO_ACTIVE_SERVICE);
        }
        return problems;
    }

    /**
     * @return the tag operations staff recognise: the AMS tag, falling back to the legacy tag for
     *         assets that predate the migration.
     */
    public String getDisplayTag() {
        if (assetTag != null && assetTag.trim().length() > 0) {
            return assetTag;
        }
        return legacyAssetTag;
    }

    @Override
    public String toString() {
        return "Asset[" + assetId + ", tag=" + getDisplayTag() + ", serial=" + serialNumber + "]";
    }
}
