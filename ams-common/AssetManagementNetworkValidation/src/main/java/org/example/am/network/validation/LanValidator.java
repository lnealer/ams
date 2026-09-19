package org.example.am.network.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.example.am.network.domain.IPRange;
import org.example.am.network.domain.RestrictedIPRanges;

/**
 * Rules every LAN side configuration has to satisfy, whatever its type.
 *
 * <p>Validators are stateless and return the accumulated messages rather than throwing, because the
 * configuration screen shows every problem at once rather than one per round trip.</p>
 */
public abstract class LanValidator {

    protected static final String LAN_IP = "lanIpAddress";
    protected static final String LAN_MASK = "lanSubnetMask";
    protected static final String LAN_GATEWAY = "lanGateway";

    /**
     * @return the LAN type code this validator handles, as stored on the configuration row
     */
    public abstract String getLanType();

    /**
     * @return the narrowest prefix length this LAN type accepts; a /30 leaves two usable hosts
     */
    protected abstract int getMaximumPrefixLength();

    /**
     * @return {@code true} when this LAN type may only be numbered out of RFC 1918 space
     */
    protected abstract boolean isPrivateAddressingRequired();

    /**
     * Runs the rules common to every LAN type, then the type specific ones.
     *
     * @return the validation messages, empty when the configuration is acceptable
     */
    public List<String> validate(final String ipAddress, final String subnetMask, final String gateway) {
        final List<String> messages = new ArrayList<String>();

        if (StringUtils.isBlank(ipAddress)) {
            messages.add("LAN IP address is required.");
        } else if (!NetworkUtils.isValidIpAddress(ipAddress)) {
            messages.add("LAN IP address '" + ipAddress + "' is not a valid IPv4 address.");
        }

        if (StringUtils.isBlank(subnetMask)) {
            messages.add("LAN subnet mask is required.");
        } else if (!NetworkUtils.isValidSubnetMask(subnetMask)) {
            messages.add("LAN subnet mask '" + subnetMask + "' is not a valid subnet mask.");
        }

        if (!messages.isEmpty()) {
            return messages;
        }

        final int prefixLength = NetworkUtils.getPrefixLength(subnetMask);
        if (prefixLength > getMaximumPrefixLength()) {
            messages.add("LAN type " + getLanType() + " requires a subnet no smaller than /"
                    + getMaximumPrefixLength() + "; /" + prefixLength + " was supplied.");
        }

        if (NetworkUtils.isNetworkOrBroadcastAddress(ipAddress, subnetMask)) {
            messages.add("LAN IP address '" + ipAddress
                    + "' is the network or broadcast address of its subnet.");
        }

        final IPRange restricting = RestrictedIPRanges.getRestrictingRange(ipAddress);
        if (restricting != null) {
            messages.add("LAN IP address '" + ipAddress + "' falls in the restricted range "
                    + restricting + ".");
        }

        if (isPrivateAddressingRequired() && !NetworkUtils.isPrivateAddress(ipAddress)) {
            messages.add("LAN type " + getLanType()
                    + " must be numbered from RFC 1918 private address space.");
        }

        messages.addAll(validateGateway(ipAddress, subnetMask, gateway));
        messages.addAll(validateLanType(ipAddress, subnetMask, gateway));
        return messages;
    }

    /**
     * The gateway is optional for LAN types that do not route off-subnet, so a blank one is only an
     * error when {@link #isGatewayRequired()} says so.
     */
    protected List<String> validateGateway(final String ipAddress, final String subnetMask,
            final String gateway) {
        final List<String> messages = new ArrayList<String>();
        if (StringUtils.isBlank(gateway)) {
            if (isGatewayRequired()) {
                messages.add("LAN gateway is required for LAN type " + getLanType() + ".");
            }
            return messages;
        }
        if (!NetworkUtils.isValidIpAddress(gateway)) {
            messages.add("LAN gateway '" + gateway + "' is not a valid IPv4 address.");
            return messages;
        }
        if (gateway.equals(ipAddress)) {
            messages.add("LAN gateway must differ from the LAN IP address.");
        }
        if (!NetworkUtils.isSameSubnet(ipAddress, gateway, subnetMask)) {
            messages.add("LAN gateway '" + gateway + "' is not inside the LAN subnet "
                    + NetworkUtils.getNetworkAddress(ipAddress, subnetMask) + "/"
                    + NetworkUtils.getPrefixLength(subnetMask) + ".");
        }
        if (NetworkUtils.isNetworkOrBroadcastAddress(gateway, subnetMask)) {
            messages.add("LAN gateway '" + gateway
                    + "' is the network or broadcast address of its subnet.");
        }
        return messages;
    }

    protected boolean isGatewayRequired() {
        return true;
    }

    /**
     * Hook for the type specific rules; the base implementation adds nothing.
     */
    protected List<String> validateLanType(final String ipAddress, final String subnetMask,
            final String gateway) {
        return new ArrayList<String>();
    }

    public boolean isValid(final String ipAddress, final String subnetMask, final String gateway) {
        return validate(ipAddress, subnetMask, gateway).isEmpty();
    }
}
