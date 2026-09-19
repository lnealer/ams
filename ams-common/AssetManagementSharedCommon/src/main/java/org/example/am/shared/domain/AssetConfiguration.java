package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * A revision of the network configuration held against an asset.
 *
 * <p>Revisions are immutable once {@link AssetConfigurationStatusType#APPLIED}; a modify-configuration
 * flow writes a new row rather than updating in place, which is what lets the compare-config screen
 * diff the stored revision against what the device actually reports.</p>
 */
public class AssetConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long configurationId;
    private Long assetId;
    private AssetConfigurationType assetConfigurationType;
    private AssetConfigurationStatusType assetConfigurationStatusType;
    private NetworkConfigurationType networkConfigurationType;
    private String lanIpAddress;
    private String lanSubnetMask;
    private String wanIpAddress;
    private String wanSubnetMask;
    /**
     * The carrier's next hop on the WAN side.
     *
     * <p>Not to be confused with {@link #lanGateway}: this one is where traffic leaves the site,
     * that one is the customer's own router inside it. Sites with their own internal routing have
     * both, and they are never the same address.</p>
     */
    private String defaultGateway;
    private String lanGateway;
    private String primaryDnsAddress;
    private String secondaryDnsAddress;
    private String circuitId;
    private Integer bandwidthKbps;
    private List<PortConfiguration> portConfigurations;
    private Date effectiveDate;
    private int revision;

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

    public AssetConfigurationType getAssetConfigurationType() {
        return assetConfigurationType;
    }

    public void setAssetConfigurationType(final AssetConfigurationType assetConfigurationType) {
        this.assetConfigurationType = assetConfigurationType;
    }

    public AssetConfigurationStatusType getAssetConfigurationStatusType() {
        return assetConfigurationStatusType;
    }

    public void setAssetConfigurationStatusType(final AssetConfigurationStatusType assetConfigurationStatusType) {
        this.assetConfigurationStatusType = assetConfigurationStatusType;
    }

    public NetworkConfigurationType getNetworkConfigurationType() {
        return networkConfigurationType;
    }

    public void setNetworkConfigurationType(final NetworkConfigurationType networkConfigurationType) {
        this.networkConfigurationType = networkConfigurationType;
    }

    public String getLanIpAddress() {
        return lanIpAddress;
    }

    public void setLanIpAddress(final String lanIpAddress) {
        this.lanIpAddress = lanIpAddress;
    }

    public String getLanSubnetMask() {
        return lanSubnetMask;
    }

    public void setLanSubnetMask(final String lanSubnetMask) {
        this.lanSubnetMask = lanSubnetMask;
    }

    public String getWanIpAddress() {
        return wanIpAddress;
    }

    public void setWanIpAddress(final String wanIpAddress) {
        this.wanIpAddress = wanIpAddress;
    }

    public String getWanSubnetMask() {
        return wanSubnetMask;
    }

    public void setWanSubnetMask(final String wanSubnetMask) {
        this.wanSubnetMask = wanSubnetMask;
    }

    public String getDefaultGateway() {
        return defaultGateway;
    }

    public void setDefaultGateway(final String defaultGateway) {
        this.defaultGateway = defaultGateway;
    }

    public String getLanGateway() {
        return lanGateway;
    }

    public void setLanGateway(final String lanGateway) {
        this.lanGateway = lanGateway;
    }

    public String getPrimaryDnsAddress() {
        return primaryDnsAddress;
    }

    public void setPrimaryDnsAddress(final String primaryDnsAddress) {
        this.primaryDnsAddress = primaryDnsAddress;
    }

    public String getSecondaryDnsAddress() {
        return secondaryDnsAddress;
    }

    public void setSecondaryDnsAddress(final String secondaryDnsAddress) {
        this.secondaryDnsAddress = secondaryDnsAddress;
    }

    public String getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(final String circuitId) {
        this.circuitId = circuitId;
    }

    public Integer getBandwidthKbps() {
        return bandwidthKbps;
    }

    public void setBandwidthKbps(final Integer bandwidthKbps) {
        this.bandwidthKbps = bandwidthKbps;
    }

    public List<PortConfiguration> getPortConfigurations() {
        return portConfigurations;
    }

    public void setPortConfigurations(final List<PortConfiguration> portConfigurations) {
        this.portConfigurations = portConfigurations;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(final Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(final int revision) {
        this.revision = revision;
    }

    /**
     * @return {@code true} when the stored revision no longer agrees with the device.
     */
    public boolean isMismatched() {
        return AssetConfigurationStatusType.MISMATCH.equals(assetConfigurationStatusType);
    }

    public boolean isEditable() {
        return AssetConfigurationStatusType.DRAFT.equals(assetConfigurationStatusType)
                || AssetConfigurationStatusType.PENDING.equals(assetConfigurationStatusType);
    }

}
