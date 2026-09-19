package org.example.am.network.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The address space AMS will not let a customer configure on a managed device: special-use blocks
 * that would black-hole traffic, plus the internal management space reserved for the platform's own
 * out-of-band network.
 */
public final class RestrictedIPRanges {

    private static final List<IPRange> RANGES;

    static {
        final List<IPRange> ranges = new ArrayList<IPRange>();
        ranges.add(IPRange.fromCidr("0.0.0.0/8", "This network"));
        ranges.add(IPRange.fromCidr("127.0.0.0/8", "Loopback"));
        ranges.add(IPRange.fromCidr("169.254.0.0/16", "Link local"));
        ranges.add(IPRange.fromCidr("192.0.0.0/24", "IETF protocol assignments"));
        ranges.add(IPRange.fromCidr("192.0.2.0/24", "Documentation TEST-NET-1"));
        ranges.add(IPRange.fromCidr("198.18.0.0/15", "Benchmarking"));
        ranges.add(IPRange.fromCidr("198.51.100.0/24", "Documentation TEST-NET-2"));
        ranges.add(IPRange.fromCidr("203.0.113.0/24", "Documentation TEST-NET-3"));
        ranges.add(IPRange.fromCidr("224.0.0.0/4", "Multicast"));
        ranges.add(IPRange.fromCidr("240.0.0.0/4", "Reserved for future use"));
        ranges.add(IPRange.fromCidr("10.255.0.0/16", "Reserved for platform out-of-band management"));
        RANGES = Collections.unmodifiableList(ranges);
    }

    private RestrictedIPRanges() {
        super();
    }

    public static List<IPRange> getRanges() {
        return RANGES;
    }

    public static boolean isRestricted(final String address) {
        return getRestrictingRange(address) != null;
    }

    /**
     * @return the range that blocks this address, so the validator can name it in the error message
     */
    public static IPRange getRestrictingRange(final String address) {
        for (final IPRange range : RANGES) {
            if (range.contains(address)) {
                return range;
            }
        }
        return null;
    }
}
