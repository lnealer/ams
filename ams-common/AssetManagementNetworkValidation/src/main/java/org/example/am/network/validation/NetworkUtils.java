package org.example.am.network.validation;

import org.apache.commons.lang.StringUtils;

/**
 * Dotted-quad helpers shared by every LAN/WAN validator.
 *
 * <p>Deliberately does not use {@code java.net.InetAddress}: that resolves names, accepts shortened
 * forms such as {@code 10.1} and can hit DNS. Everything here is pure string and arithmetic work on
 * the four-octet form that the configuration screens capture.</p>
 */
public final class NetworkUtils {

    /** 255.255.255.255 as an unsigned value. */
    public static final long BROADCAST = 4294967295L;

    private NetworkUtils() {
        super();
    }

    /**
     * @return {@code true} only for a strict four octet dotted quad with no leading zeroes.
     */
    public static boolean isValidIpAddress(final String address) {
        if (StringUtils.isBlank(address)) {
            return false;
        }
        final String[] octets = address.trim().split("\\.", -1);
        if (octets.length != 4) {
            return false;
        }
        for (final String octet : octets) {
            if (!isValidOctet(octet)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidOctet(final String octet) {
        if (octet.length() == 0 || octet.length() > 3) {
            return false;
        }
        if (octet.length() > 1 && octet.charAt(0) == '0') {
            return false;
        }
        for (int i = 0; i < octet.length(); i++) {
            if (!Character.isDigit(octet.charAt(i))) {
                return false;
            }
        }
        return Integer.parseInt(octet) <= 255;
    }

    /**
     * A valid mask is a contiguous run of one bits followed by a contiguous run of zero bits.
     */
    public static boolean isValidSubnetMask(final String mask) {
        if (!isValidIpAddress(mask)) {
            return false;
        }
        final long value = toLong(mask);
        if (value == 0L) {
            return false;
        }
        // Inverting a contiguous mask yields a value whose successor is a power of two.
        final long inverted = (~value) & BROADCAST;
        return (inverted & (inverted + 1L)) == 0L;
    }

    public static long toLong(final String address) {
        final String[] octets = address.trim().split("\\.", -1);
        long value = 0L;
        for (final String octet : octets) {
            value = (value << 8) | Long.parseLong(octet);
        }
        return value;
    }

    public static String toDottedQuad(final long value) {
        return ((value >> 24) & 255L) + "." + ((value >> 16) & 255L) + "."
                + ((value >> 8) & 255L) + "." + (value & 255L);
    }

    /**
     * @return the number of leading one bits in the mask, i.e. the CIDR prefix length
     */
    public static int getPrefixLength(final String mask) {
        long value = toLong(mask);
        int length = 0;
        while (value != 0L) {
            length += (int) (value & 1L);
            value >>>= 1;
        }
        return length;
    }

    public static String getNetworkAddress(final String address, final String mask) {
        return toDottedQuad(toLong(address) & toLong(mask));
    }

    public static String getBroadcastAddress(final String address, final String mask) {
        final long maskValue = toLong(mask);
        return toDottedQuad((toLong(address) & maskValue) | ((~maskValue) & BROADCAST));
    }

    /**
     * @return the first address that may be assigned to a host in this subnet
     */
    public static String getFirstUsableAddress(final String address, final String mask) {
        if (getPrefixLength(mask) >= 31) {
            return getNetworkAddress(address, mask);
        }
        return toDottedQuad(toLong(getNetworkAddress(address, mask)) + 1L);
    }

    public static String getLastUsableAddress(final String address, final String mask) {
        if (getPrefixLength(mask) >= 31) {
            return getBroadcastAddress(address, mask);
        }
        return toDottedQuad(toLong(getBroadcastAddress(address, mask)) - 1L);
    }

    /**
     * @return {@code true} when both addresses sit inside the same subnet
     */
    public static boolean isSameSubnet(final String left, final String right, final String mask) {
        if (!isValidIpAddress(left) || !isValidIpAddress(right) || !isValidSubnetMask(mask)) {
            return false;
        }
        final long maskValue = toLong(mask);
        return (toLong(left) & maskValue) == (toLong(right) & maskValue);
    }

    /**
     * @return {@code true} when the address is the network or the broadcast address of its subnet,
     *         neither of which may be handed to an interface
     */
    public static boolean isNetworkOrBroadcastAddress(final String address, final String mask) {
        if (!isValidIpAddress(address) || !isValidSubnetMask(mask)) {
            return false;
        }
        if (getPrefixLength(mask) >= 31) {
            return false;
        }
        final long value = toLong(address);
        return value == toLong(getNetworkAddress(address, mask))
                || value == toLong(getBroadcastAddress(address, mask));
    }

    /** RFC 1918 space. */
    public static boolean isPrivateAddress(final String address) {
        if (!isValidIpAddress(address)) {
            return false;
        }
        final long value = toLong(address);
        return within(value, "10.0.0.0", "10.255.255.255")
                || within(value, "172.16.0.0", "172.31.255.255")
                || within(value, "192.168.0.0", "192.168.255.255");
    }

    public static boolean isLoopbackAddress(final String address) {
        return isValidIpAddress(address) && within(toLong(address), "127.0.0.0", "127.255.255.255");
    }

    public static boolean isMulticastAddress(final String address) {
        return isValidIpAddress(address) && within(toLong(address), "224.0.0.0", "239.255.255.255");
    }

    public static boolean isLinkLocalAddress(final String address) {
        return isValidIpAddress(address) && within(toLong(address), "169.254.0.0", "169.254.255.255");
    }

    private static boolean within(final long value, final String low, final String high) {
        return value >= toLong(low) && value <= toLong(high);
    }

    /**
     * @return the number of assignable host addresses in a subnet of this mask
     */
    public static long getUsableHostCount(final String mask) {
        final int prefix = getPrefixLength(mask);
        if (prefix >= 31) {
            // A /31 is a point to point link and a /32 is a host route: every address is usable.
            return 1L << (32 - prefix);
        }
        return (1L << (32 - prefix)) - 2L;
    }
}
