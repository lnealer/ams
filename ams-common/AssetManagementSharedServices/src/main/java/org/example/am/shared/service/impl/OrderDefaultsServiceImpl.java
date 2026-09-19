package org.example.am.shared.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.example.am.network.validation.NetworkUtils;
import org.example.am.shared.dao.AssetConfigDAO;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.PropertyType;
import org.example.am.shared.domain.SubscriberPc;
import org.example.am.shared.domain.SubscriberPcType;
import org.example.am.shared.service.ConfigService;
import org.example.am.shared.service.OrderDefaultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** @see OrderDefaultsService */
@Service("orderDefaultsService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class OrderDefaultsServiceImpl implements OrderDefaultsService {

    /** A site LAN is a /24 unless the operator says otherwise; big enough, and easy to read. */
    private static final String SITE_MASK = "255.255.255.0";

    /** Third octets below this are left for the sites an operator numbers by hand. */
    private static final int FIRST_SITE_OCTET = 10;
    private static final int LAST_SITE_OCTET = 250;

    private static final String DEFAULT_LAN_BLOCK = "192.168.0.0";
    private static final int DEFAULT_STATIC_START = 11;
    private static final int DEFAULT_BANDWIDTH_KBPS = 100000;

    @Autowired
    private AssetConfigDAO assetConfigDAO;

    @Autowired
    private ConfigService configService;

    @Override
    public List<String> applyConfigurationDefaults(final AssetConfiguration configuration,
            final long customerId) {
        final List<String> notes = new ArrayList<String>();
        if (configuration == null) {
            return notes;
        }
        final List<AssetConfiguration> estate = assetConfigDAO.getConfigurationsForCustomer(customerId);
        final AssetConfiguration previous = estate.isEmpty() ? null : estate.get(0);

        applyWan(configuration, previous, estate, notes);
        applyDns(configuration, previous, notes);
        applyCircuitAndBandwidth(configuration, previous, notes);
        applyLan(configuration, estate, notes);
        return notes;
    }

    /**
     * Fills the WAN side with a usable address rather than leaving it blank.
     *
     * <p>Two sources, in order. A customer who already has a site is numbered out of the subnet the
     * carrier delegated to them - same mask, same next hop, next free host - because a second device
     * behind the same handoff genuinely shares those values. A customer with no site yet is numbered
     * out of the pool named by {@code DEFWANSUBNET} and {@code DEFWANMASK}, with the gateway at the
     * first usable address, which is how a handoff is conventionally laid out.</p>
     *
     * <p>That pool is a configured property, not a constant, because it has to be <em>the
     * operator's own allocation</em>. The shipped value is a working default for development; an
     * operator points it at the range they actually hold. Whatever it names, an address is only
     * handed out once - {@code getWanAddressesInUse} is checked across every customer, not just this
     * one, because a WAN address is unique on the carrier's network.</p>
     *
     * <p>Everything here is a starting point the operator is expected to check against the carrier's
     * assignment, which is what the note on the screen says.</p>
     */
    private void applyWan(final AssetConfiguration configuration, final AssetConfiguration previous,
            final List<AssetConfiguration> estate, final List<String> notes) {
        final boolean carried = previous != null
                && StringUtils.isNotBlank(previous.getWanSubnetMask())
                && StringUtils.isNotBlank(previous.getDefaultGateway());

        final String mask;
        final String gateway;
        final String seed;
        if (carried) {
            mask = previous.getWanSubnetMask();
            gateway = previous.getDefaultGateway();
            seed = previous.getWanIpAddress();
        } else {
            final String pool = configService.getString(PropertyType.DEFAULT_WAN_SUBNET, null);
            mask = configService.getString(PropertyType.DEFAULT_WAN_MASK, null);
            if (StringUtils.isBlank(pool) || StringUtils.isBlank(mask)
                    || !NetworkUtils.isValidIpAddress(pool) || !NetworkUtils.isValidSubnetMask(mask)) {
                return;
            }
            gateway = NetworkUtils.getFirstUsableAddress(pool, mask);
            seed = pool;
        }

        final String source = carried ? "the subnet already delegated to this customer"
                : "the WAN pool this environment allocates from";

        if (StringUtils.isBlank(configuration.getWanSubnetMask())) {
            configuration.setWanSubnetMask(mask);
            notes.add("WAN subnet mask " + mask + " from " + source + ".");
        }
        if (StringUtils.isBlank(configuration.getDefaultGateway())) {
            configuration.setDefaultGateway(gateway);
            notes.add("Default gateway " + gateway + " from " + source + ".");
        }
        if (StringUtils.isNotBlank(configuration.getWanIpAddress())) {
            return;
        }
        final String suggestion = nextFreeWanAddress(seed, mask, gateway, estate);
        if (suggestion != null) {
            configuration.setWanIpAddress(suggestion);
            notes.add("WAN IP address " + suggestion + " is the next free address in " + source
                    + " - check it against the carrier's assignment before the device is staged.");
        }
    }

    /**
     * @return the lowest usable address in the delegated subnet that nothing already holds, or
     *         {@code null} when the subnet is full or its addressing does not make sense
     */
    private String nextFreeWanAddress(final String seedAddress, final String mask,
            final String gateway, final List<AssetConfiguration> estate) {
        if (StringUtils.isBlank(seedAddress) || !NetworkUtils.isValidIpAddress(seedAddress)
                || !NetworkUtils.isValidSubnetMask(mask)) {
            return null;
        }
        final Set<String> taken = new HashSet<String>();
        taken.add(gateway);
        for (final AssetConfiguration existing : estate) {
            if (StringUtils.isNotBlank(existing.getWanIpAddress())) {
                taken.add(existing.getWanIpAddress().trim());
            }
        }
        // Estate-wide: the same public address must not reach two different customers.
        for (final String inUse : assetConfigDAO.getWanAddressesInUse()) {
            if (StringUtils.isNotBlank(inUse)) {
                taken.add(inUse.trim());
            }
        }

        final long first = NetworkUtils.toLong(NetworkUtils.getFirstUsableAddress(seedAddress, mask));
        final long last = NetworkUtils.toLong(NetworkUtils.getLastUsableAddress(seedAddress, mask));
        for (long candidate = first; candidate <= last; candidate++) {
            final String address = NetworkUtils.toDottedQuad(candidate);
            if (!taken.contains(address)) {
                return address;
            }
        }
        return null;
    }

    private void applyDns(final AssetConfiguration configuration, final AssetConfiguration previous,
            final List<String> notes) {
        if (StringUtils.isBlank(configuration.getPrimaryDnsAddress())) {
            final String value = firstNotBlank(previous == null ? null : previous.getPrimaryDnsAddress(),
                    configService.getString(PropertyType.DEFAULT_PRIMARY_DNS, null));
            if (StringUtils.isNotBlank(value)) {
                configuration.setPrimaryDnsAddress(value);
                notes.add("Primary DNS " + value + " is the managed resolver for this platform.");
            }
        }
        if (StringUtils.isBlank(configuration.getSecondaryDnsAddress())) {
            final String value = firstNotBlank(previous == null ? null : previous.getSecondaryDnsAddress(),
                    configService.getString(PropertyType.DEFAULT_SECONDARY_DNS, null));
            if (StringUtils.isNotBlank(value)) {
                configuration.setSecondaryDnsAddress(value);
                notes.add("Secondary DNS " + value + " is the managed resolver for this platform.");
            }
        }
    }

    /**
     * The circuit is only ever carried forward. It names a real carrier circuit, and a new site has
     * no circuit until the carrier delivers one, so there is nothing to derive it from.
     */
    private void applyCircuitAndBandwidth(final AssetConfiguration configuration,
            final AssetConfiguration previous, final List<String> notes) {
        if (previous != null && StringUtils.isBlank(configuration.getCircuitId())
                && StringUtils.isNotBlank(previous.getCircuitId())) {
            configuration.setCircuitId(previous.getCircuitId());
            notes.add("Circuit " + previous.getCircuitId() + " carried forward - change it if this"
                    + " site is served by a new circuit.");
        }
        if (configuration.getBandwidthKbps() != null) {
            return;
        }
        final Integer carried = previous == null ? null : previous.getBandwidthKbps();
        final int value = carried != null ? carried.intValue()
                : configService.getInt(PropertyType.DEFAULT_BANDWIDTH_KBPS, DEFAULT_BANDWIDTH_KBPS);
        configuration.setBandwidthKbps(Integer.valueOf(value));
        notes.add("Bandwidth " + value + " Kbps"
                + (carried != null ? " carried forward from this customer's circuit." : " is the default for new sites."));
    }

    /**
     * The LAN is ours to choose, so it is computed rather than carried: a /24 out of the configured
     * private block, picking a third octet the customer is not already using. The device takes the
     * first usable address and the customer's own router the last, which is what {@code
     * LanTypeAValidator} requires.
     */
    private void applyLan(final AssetConfiguration configuration,
            final List<AssetConfiguration> estate, final List<String> notes) {
        if (StringUtils.isNotBlank(configuration.getLanIpAddress())) {
            return;
        }
        final String block = configService.getString(PropertyType.LAN_SUGGESTION_BLOCK, DEFAULT_LAN_BLOCK);
        final String[] octets = StringUtils.split(StringUtils.trimToEmpty(block), '.');
        if (octets == null || octets.length != 4) {
            return;
        }
        final Set<String> used = new HashSet<String>();
        for (final AssetConfiguration existing : estate) {
            if (StringUtils.isNotBlank(existing.getLanIpAddress())
                    && StringUtils.isNotBlank(existing.getLanSubnetMask())) {
                used.add(NetworkUtils.getNetworkAddress(existing.getLanIpAddress(),
                        existing.getLanSubnetMask()));
            }
        }

        for (int third = FIRST_SITE_OCTET; third <= LAST_SITE_OCTET; third++) {
            final String network = octets[0] + "." + octets[1] + "." + third + ".0";
            if (used.contains(network)) {
                continue;
            }
            configuration.setLanIpAddress(NetworkUtils.getFirstUsableAddress(network, SITE_MASK));
            configuration.setLanSubnetMask(SITE_MASK);
            configuration.setLanGateway(NetworkUtils.getLastUsableAddress(network, SITE_MASK));
            notes.add("LAN " + network + "/24 is the next block this customer is not already using;"
                    + " the device takes " + configuration.getLanIpAddress() + " and their router "
                    + configuration.getLanGateway() + ".");
            return;
        }
    }

    @Override
    public List<String> applySubscriberPcDefaults(final List<SubscriberPc> rows,
            final AssetConfiguration configuration, final String hostNameStem) {
        final List<String> notes = new ArrayList<String>();
        if (rows == null || rows.isEmpty()) {
            return notes;
        }
        final String stem = toHostNameStem(hostNameStem);
        final String lanIp = configuration == null ? null : configuration.getLanIpAddress();
        final String lanMask = configuration == null ? null : configuration.getLanSubnetMask();
        final boolean canAllocate = StringUtils.isNotBlank(lanIp) && StringUtils.isNotBlank(lanMask)
                && NetworkUtils.isValidIpAddress(lanIp) && NetworkUtils.isValidSubnetMask(lanMask);

        final Set<String> taken = new HashSet<String>();
        for (final SubscriberPc pc : rows) {
            if (pc != null && StringUtils.isNotBlank(pc.getIpAddress())) {
                taken.add(pc.getIpAddress().trim());
            }
        }

        int suggestedNames = 0;
        int allocated = 0;
        for (final SubscriberPc pc : rows) {
            if (pc == null) {
                continue;
            }
            // A row the operator has started is theirs; only the address is topped up, and only
            // when they have asked for a static one and not said which.
            if (pc.isBlank()) {
                if (suggestedNames == 0 && stem != null) {
                    pc.setHostName(stem + "-01");
                    pc.setSubscriberPcType(SubscriberPcType.DESKTOP);
                    pc.setUserCount(Integer.valueOf(1));
                    suggestedNames++;
                }
                continue;
            }
            if (!pc.isStaticAddress() || StringUtils.isNotBlank(pc.getIpAddress()) || !canAllocate) {
                continue;
            }
            final String address = nextFreeLanAddress(lanIp, lanMask, taken);
            if (address != null) {
                pc.setIpAddress(address);
                taken.add(address);
                allocated++;
            }
        }

        if (suggestedNames > 0) {
            notes.add("The first row is a starting point named after the device - rename it, change"
                    + " the type, or clear it if it is not wanted.");
        }
        if (allocated > 0) {
            notes.add(allocated + (allocated == 1 ? " machine marked static was" : " machines marked static were")
                    + " given the next free address inside the site LAN.");
        }
        return notes;
    }

    /**
     * Static addresses start above the device's own and below the DHCP pool the engineer configures
     * on the day, so the two allocations cannot collide. Where that boundary sits is an operational
     * choice, so it comes from {@code SUBSTATSTART} rather than being fixed here.
     *
     * <p>The LAN gateway is excluded by the same rule that excludes the device: it is the last
     * usable address, and the loop stops before it.</p>
     */
    private String nextFreeLanAddress(final String lanIp, final String lanMask,
            final Set<String> taken) {
        final int offset = configService.getInt(PropertyType.SUBSCRIBER_STATIC_START,
                DEFAULT_STATIC_START);
        final long network = NetworkUtils.toLong(NetworkUtils.getNetworkAddress(lanIp, lanMask));
        final long last = NetworkUtils.toLong(NetworkUtils.getLastUsableAddress(lanIp, lanMask));
        final long deviceAddress = NetworkUtils.toLong(lanIp);

        for (long candidate = network + offset; candidate < last; candidate++) {
            final String address = NetworkUtils.toDottedQuad(candidate);
            if (candidate == deviceAddress || taken.contains(address)) {
                continue;
            }
            return address;
        }
        return null;
    }

    /** @return a DNS safe stem, or {@code null} when there is nothing usable to build one from */
    private static String toHostNameStem(final String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        final String stem = source.trim().toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        return StringUtils.isBlank(stem) ? null : stem;
    }

    private static String firstNotBlank(final String preferred, final String fallback) {
        return StringUtils.isNotBlank(preferred) ? preferred : fallback;
    }
}
