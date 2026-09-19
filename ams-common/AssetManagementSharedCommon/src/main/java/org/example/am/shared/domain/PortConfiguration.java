package org.example.am.shared.domain;

import java.io.Serializable;

/**
 * One physical port on a managed device.
 */
public class PortConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long portId;
    private String portName;
    private PortConfigurationType portConfigurationType;
    private String macAddress;
    private boolean active;
    private Integer vlanId;
    private String ipAddress;
    private String subnetMask;

    public Long getPortId() {
        return portId;
    }

    public void setPortId(final Long portId) {
        this.portId = portId;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(final String portName) {
        this.portName = portName;
    }

    public PortConfigurationType getPortConfigurationType() {
        return portConfigurationType;
    }

    public void setPortConfigurationType(final PortConfigurationType portConfigurationType) {
        this.portConfigurationType = portConfigurationType;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public Integer getVlanId() {
        return vlanId;
    }

    public void setVlanId(final Integer vlanId) {
        this.vlanId = vlanId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(final String subnetMask) {
        this.subnetMask = subnetMask;
    }

}
