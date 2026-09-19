package org.example.am.shared.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.example.am.network.validation.LanTypeAValidator;
import org.example.am.network.validation.WanValidator;
import org.example.am.shared.dao.AssetConfigDAO;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.PropertyType;
import org.example.am.shared.domain.SubscriberPc;
import org.example.am.shared.domain.SubscriberPcType;
import org.example.am.shared.service.ConfigService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The suggestions have one hard requirement: whatever is put in the form has to pass the validator
 * that guards the same screen. A suggestion that fails validation is worse than a blank field,
 * because the operator did not type it and will not think to look at it.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderDefaultsServiceImplTest {

    private static final long CUSTOMER_ID = 1010L;

    @Mock
    private AssetConfigDAO assetConfigDAO;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private OrderDefaultsServiceImpl service;

    /** Stubbed in each test rather than in setUp, so unused stubs are not left lying around. */
    private void stubProperties() {
        when(configService.getString(eq(PropertyType.DEFAULT_PRIMARY_DNS), anyString()))
                .thenReturn("9.9.9.9");
        when(configService.getString(eq(PropertyType.DEFAULT_SECONDARY_DNS), anyString()))
                .thenReturn("149.112.112.112");
        when(configService.getString(eq(PropertyType.LAN_SUGGESTION_BLOCK), anyString()))
                .thenReturn("192.168.0.0");
        when(configService.getInt(eq(PropertyType.DEFAULT_BANDWIDTH_KBPS), anyInt()))
                .thenReturn(100000);
        when(configService.getInt(eq(PropertyType.SUBSCRIBER_STATIC_START), anyInt()))
                .thenReturn(11);
        when(configService.getString(eq(PropertyType.DEFAULT_WAN_SUBNET), anyString()))
                .thenReturn("198.51.45.0");
        when(configService.getString(eq(PropertyType.DEFAULT_WAN_MASK), anyString()))
                .thenReturn("255.255.255.0");
    }

    private static AssetConfiguration existing(final String wanIp, final String wanMask,
            final String gateway, final String lanIp, final String lanMask) {
        final AssetConfiguration configuration = new AssetConfiguration();
        configuration.setWanIpAddress(wanIp);
        configuration.setWanSubnetMask(wanMask);
        configuration.setDefaultGateway(gateway);
        configuration.setLanIpAddress(lanIp);
        configuration.setLanSubnetMask(lanMask);
        configuration.setCircuitId("CKT-1");
        configuration.setBandwidthKbps(Integer.valueOf(500000));
        return configuration;
    }

    @Test
    public void suggestedLanPassesTheTypeAValidator() {
        stubProperties();
        when(assetConfigDAO.getConfigurationsForCustomer(CUSTOMER_ID))
                .thenReturn(Collections.<AssetConfiguration>emptyList());

        final AssetConfiguration configuration = new AssetConfiguration();
        service.applyConfigurationDefaults(configuration, CUSTOMER_ID);

        final List<String> problems = new LanTypeAValidator().validate(configuration.getLanIpAddress(),
                configuration.getLanSubnetMask(), configuration.getLanGateway());
        assertTrue("suggested LAN was rejected: " + problems, problems.isEmpty());
        assertEquals("192.168.10.1", configuration.getLanIpAddress());
        assertEquals("192.168.10.254", configuration.getLanGateway());
    }

    @Test
    public void aFirstSiteIsNumberedOutOfTheConfiguredWanPool() {
        stubProperties();
        when(assetConfigDAO.getConfigurationsForCustomer(CUSTOMER_ID))
                .thenReturn(Collections.<AssetConfiguration>emptyList());
        when(assetConfigDAO.getWanAddressesInUse())
                .thenReturn(Collections.<String>emptyList());

        final AssetConfiguration configuration = new AssetConfiguration();
        service.applyConfigurationDefaults(configuration, CUSTOMER_ID);

        // Gateway at the first usable address of the pool, device at the next free one after it.
        assertEquals("255.255.255.0", configuration.getWanSubnetMask());
        assertEquals("198.51.45.1", configuration.getDefaultGateway());
        assertEquals("198.51.45.2", configuration.getWanIpAddress());
        assertEquals("9.9.9.9", configuration.getPrimaryDnsAddress());

        final List<String> problems = new WanValidator().validate(configuration.getWanIpAddress(),
                configuration.getWanSubnetMask(), configuration.getDefaultGateway(),
                configuration.getPrimaryDnsAddress(), configuration.getSecondaryDnsAddress());
        assertTrue("pool allocation was rejected: " + problems, problems.isEmpty());
    }

    @Test
    public void aWanAddressIsNeverHandedToTwoCustomers() {
        stubProperties();
        when(assetConfigDAO.getConfigurationsForCustomer(CUSTOMER_ID))
                .thenReturn(Collections.<AssetConfiguration>emptyList());
        // Held by some other customer entirely, which this customer's own estate cannot see.
        when(assetConfigDAO.getWanAddressesInUse())
                .thenReturn(Arrays.asList("198.51.45.2", "198.51.45.3"));

        final AssetConfiguration configuration = new AssetConfiguration();
        service.applyConfigurationDefaults(configuration, CUSTOMER_ID);

        assertEquals("198.51.45.4", configuration.getWanIpAddress());
    }

    @Test
    public void withNoPoolConfiguredTheWanIsLeftForTheOperator() {
        stubProperties();
        when(configService.getString(eq(PropertyType.DEFAULT_WAN_SUBNET), anyString()))
                .thenReturn(null);
        when(assetConfigDAO.getConfigurationsForCustomer(CUSTOMER_ID))
                .thenReturn(Collections.<AssetConfiguration>emptyList());

        final AssetConfiguration configuration = new AssetConfiguration();
        service.applyConfigurationDefaults(configuration, CUSTOMER_ID);

        assertNull(configuration.getWanIpAddress());
        assertNull(configuration.getDefaultGateway());
    }

    @Test
    public void wanIsCarriedForwardAndTheNextFreeAddressAllocated() {
        stubProperties();
        when(assetConfigDAO.getConfigurationsForCustomer(CUSTOMER_ID)).thenReturn(
                Arrays.asList(existing("64.12.30.42", "255.255.255.248", "64.12.30.41",
                        "10.21.0.1", "255.255.252.0")));
        when(assetConfigDAO.getWanAddressesInUse()).thenReturn(Arrays.asList("64.12.30.42"));

        final AssetConfiguration configuration = new AssetConfiguration();
        service.applyConfigurationDefaults(configuration, CUSTOMER_ID);

        assertEquals("255.255.255.248", configuration.getWanSubnetMask());
        assertEquals("64.12.30.41", configuration.getDefaultGateway());
        // .41 is the gateway and .42 is already in use, so the first free host is .43.
        assertEquals("64.12.30.43", configuration.getWanIpAddress());
        assertEquals("CKT-1", configuration.getCircuitId());
        assertEquals(Integer.valueOf(500000), configuration.getBandwidthKbps());

        final List<String> problems = new WanValidator().validate(configuration.getWanIpAddress(),
                configuration.getWanSubnetMask(), configuration.getDefaultGateway(),
                configuration.getPrimaryDnsAddress(), configuration.getSecondaryDnsAddress());
        assertTrue("carried WAN was rejected: " + problems, problems.isEmpty());
    }

    @Test
    public void suggestedLanAvoidsSubnetsTheCustomerAlreadyUses() {
        stubProperties();
        when(assetConfigDAO.getConfigurationsForCustomer(CUSTOMER_ID)).thenReturn(
                Arrays.asList(existing(null, null, null, "192.168.10.1", "255.255.255.0"),
                        existing(null, null, null, "192.168.11.1", "255.255.255.0")));

        final AssetConfiguration configuration = new AssetConfiguration();
        service.applyConfigurationDefaults(configuration, CUSTOMER_ID);

        assertEquals("192.168.12.1", configuration.getLanIpAddress());
    }

    @Test
    public void nothingTheOperatorTypedIsOverwritten() {
        stubProperties();
        when(assetConfigDAO.getConfigurationsForCustomer(CUSTOMER_ID)).thenReturn(
                Arrays.asList(existing("64.12.30.42", "255.255.255.248", "64.12.30.41",
                        "10.21.0.1", "255.255.252.0")));

        final AssetConfiguration configuration = new AssetConfiguration();
        configuration.setWanIpAddress("64.12.30.44");
        configuration.setLanIpAddress("172.16.5.1");
        configuration.setPrimaryDnsAddress("1.1.1.1");
        configuration.setBandwidthKbps(Integer.valueOf(20000));

        service.applyConfigurationDefaults(configuration, CUSTOMER_ID);

        assertEquals("64.12.30.44", configuration.getWanIpAddress());
        assertEquals("172.16.5.1", configuration.getLanIpAddress());
        assertEquals("1.1.1.1", configuration.getPrimaryDnsAddress());
        assertEquals(Integer.valueOf(20000), configuration.getBandwidthKbps());
    }

    @Test
    public void staticSubscriberRowsAreGivenAddressesInsideTheLan() {
        stubProperties();
        final AssetConfiguration configuration = new AssetConfiguration();
        configuration.setLanIpAddress("192.168.10.1");
        configuration.setLanSubnetMask("255.255.255.0");

        final List<SubscriberPc> rows = new ArrayList<SubscriberPc>();
        rows.add(pc("till-01", true));
        rows.add(pc("till-02", true));
        rows.add(pc("office-01", false));

        service.applySubscriberPcDefaults(rows, configuration, "front-desk");

        assertEquals("192.168.10.11", rows.get(0).getIpAddress());
        assertEquals("192.168.10.12", rows.get(1).getIpAddress());
        // Not static, so it stays on DHCP and gets no address.
        assertNull(rows.get(2).getIpAddress());
    }

    @Test
    public void anAddressTheOperatorChoseIsKeptAndNotHandedToAnotherMachine() {
        stubProperties();
        final AssetConfiguration configuration = new AssetConfiguration();
        configuration.setLanIpAddress("192.168.10.1");
        configuration.setLanSubnetMask("255.255.255.0");

        final List<SubscriberPc> rows = new ArrayList<SubscriberPc>();
        final SubscriberPc chosen = pc("server", true);
        chosen.setIpAddress("192.168.10.11");
        rows.add(chosen);
        rows.add(pc("till-01", true));

        service.applySubscriberPcDefaults(rows, configuration, null);

        assertEquals("192.168.10.11", rows.get(0).getIpAddress());
        assertEquals("192.168.10.12", rows.get(1).getIpAddress());
    }

    @Test
    public void withNoLanKeyedNoAddressesAreInvented() {
        stubProperties();
        final List<SubscriberPc> rows = new ArrayList<SubscriberPc>();
        rows.add(pc("till-01", true));

        service.applySubscriberPcDefaults(rows, new AssetConfiguration(), null);

        assertNull(rows.get(0).getIpAddress());
    }

    private static SubscriberPc pc(final String hostName, final boolean isStatic) {
        final SubscriberPc pc = new SubscriberPc();
        pc.setHostName(hostName);
        pc.setSubscriberPcType(SubscriberPcType.DESKTOP);
        pc.setStaticAddress(isStatic);
        return pc;
    }
}
