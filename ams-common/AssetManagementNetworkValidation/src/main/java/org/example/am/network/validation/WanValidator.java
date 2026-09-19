package org.example.am.network.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.example.am.network.domain.IPRange;
import org.example.am.network.domain.RestrictedIPRanges;

/**
 * Rules for the carrier facing side of a managed device.
 *
 * <p>Unlike the LAN side the WAN address is assigned by the carrier, so the validator's job is to
 * catch transcription errors before an engineer is dispatched: private space keyed by mistake, a
 * next hop outside the delegated subnet, DNS servers pointed at unroutable addresses.</p>
 */
public class WanValidator {

    /** The carrier never delegates anything wider than a /24 for a WAN handoff. */
    private static final int MINIMUM_PREFIX_LENGTH = 24;

    public List<String> validate(final String ipAddress, final String subnetMask,
            final String gateway) {
        return validate(ipAddress, subnetMask, gateway, null, null);
    }

    public List<String> validate(final String ipAddress, final String subnetMask,
            final String gateway, final String primaryDns, final String secondaryDns) {
        final List<String> messages = new ArrayList<String>();

        if (StringUtils.isBlank(ipAddress)) {
            messages.add("WAN IP address is required.");
        } else if (!NetworkUtils.isValidIpAddress(ipAddress)) {
            messages.add("WAN IP address '" + ipAddress + "' is not a valid IPv4 address.");
        }

        if (StringUtils.isBlank(subnetMask)) {
            messages.add("WAN subnet mask is required.");
        } else if (!NetworkUtils.isValidSubnetMask(subnetMask)) {
            messages.add("WAN subnet mask '" + subnetMask + "' is not a valid subnet mask.");
        }

        if (StringUtils.isBlank(gateway)) {
            messages.add("WAN gateway is required.");
        } else if (!NetworkUtils.isValidIpAddress(gateway)) {
            messages.add("WAN gateway '" + gateway + "' is not a valid IPv4 address.");
        }

        if (!messages.isEmpty()) {
            return messages;
        }

        final int prefixLength = NetworkUtils.getPrefixLength(subnetMask);
        if (prefixLength < MINIMUM_PREFIX_LENGTH) {
            messages.add("WAN subnet /" + prefixLength + " is wider than the /"
                    + MINIMUM_PREFIX_LENGTH + " a carrier handoff delegates.");
        }

        if (NetworkUtils.isPrivateAddress(ipAddress)) {
            messages.add("WAN IP address '" + ipAddress
                    + "' is RFC 1918 private space and cannot be routed by the carrier.");
        }

        final IPRange restricting = RestrictedIPRanges.getRestrictingRange(ipAddress);
        if (restricting != null) {
            messages.add("WAN IP address '" + ipAddress + "' falls in the restricted range "
                    + restricting + ".");
        }

        if (NetworkUtils.isNetworkOrBroadcastAddress(ipAddress, subnetMask)) {
            messages.add("WAN IP address '" + ipAddress
                    + "' is the network or broadcast address of its subnet.");
        }

        if (gateway.equals(ipAddress)) {
            messages.add("WAN gateway must differ from the WAN IP address.");
        } else if (!NetworkUtils.isSameSubnet(ipAddress, gateway, subnetMask)) {
            messages.add("WAN gateway '" + gateway + "' is not inside the delegated subnet "
                    + NetworkUtils.getNetworkAddress(ipAddress, subnetMask) + "/" + prefixLength + ".");
        } else if (NetworkUtils.isNetworkOrBroadcastAddress(gateway, subnetMask)) {
            messages.add("WAN gateway '" + gateway
                    + "' is the network or broadcast address of its subnet.");
        }

        messages.addAll(validateDns("Primary", primaryDns));
        messages.addAll(validateDns("Secondary", secondaryDns));
        return messages;
    }

    private List<String> validateDns(final String label, final String address) {
        final List<String> messages = new ArrayList<String>();
        if (StringUtils.isBlank(address)) {
            return messages;
        }
        if (!NetworkUtils.isValidIpAddress(address)) {
            messages.add(label + " DNS address '" + address + "' is not a valid IPv4 address.");
            return messages;
        }
        if (NetworkUtils.isMulticastAddress(address) || NetworkUtils.isLoopbackAddress(address)
                || NetworkUtils.isLinkLocalAddress(address)) {
            messages.add(label + " DNS address '" + address + "' is not a reachable unicast address.");
        }
        return messages;
    }

    public boolean isValid(final String ipAddress, final String subnetMask, final String gateway) {
        return validate(ipAddress, subnetMask, gateway).isEmpty();
    }
}
