package org.example.am.shared.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.example.am.shared.dao.AssetDAO;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetStatusType;
import org.example.am.shared.domain.AssetType;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.domain.Order;
import org.example.am.shared.helper.AbstractBaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

public class AssetDAOImplTest extends AbstractBaseTest {

    @Autowired
    @Qualifier("assetSharedDAO")
    private AssetDAO assetDAO;

    @Autowired
    private javax.sql.DataSource dataSource;

    @Test
    public void assetIsLoadedWithItsTypeAndStatusResolved() {
        final Asset asset = assetDAO.getAsset(CUSTOMER_ID, HEALTHY_ASSET_ID);
        assertNotNull(asset);
        assertEquals("AMS-000501", asset.getAssetTag());
        assertEquals("SN-ALPHA-001", asset.getSerialNumber());
        assertEquals(AssetType.ROUTER, asset.getAssetType());
        assertEquals(AssetStatusType.ACTIVE, asset.getAssetStatusType());
        assertTrue(asset.isMigratable());
        assertTrue(asset.isEmergencyReplacementEnabled());
    }

    @Test
    public void lookupIsScopedToTheCustomer() {
        // 5005 exists, but it belongs to customer 1002.
        assertNull(assetDAO.getAsset(CUSTOMER_ID, 5005L));
        assertNotNull(assetDAO.getAsset(OTHER_CUSTOMER_ID, 5005L));
    }

    @Test
    public void assetTagLookupIsCaseInsensitive() {
        assertNotNull(assetDAO.getAssetByAssetTag(CUSTOMER_ID, "ams-000501"));
        assertNotNull(assetDAO.getAssetByAssetTag(CUSTOMER_ID, "  AMS-000501  "));
        assertNull(assetDAO.getAssetByAssetTag(CUSTOMER_ID, "does-not-exist"));
        assertNull(assetDAO.getAssetByAssetTag(CUSTOMER_ID, "   "));
    }

    @Test
    public void serialNumberLookupCrossesCustomers() {
        final Asset asset = assetDAO.getAssetBySerialNumber("sn-beta-001");
        assertNotNull(asset);
        assertEquals(Long.valueOf(5005L), asset.getAssetId());
    }

    @Test
    public void mostRecentOrderIsReturnedForTheAsset() {
        final Order order = assetDAO.getOrderForAsset(CUSTOMER_ID, HEALTHY_ASSET_ID);
        assertNotNull(order);
        // 6003 was submitted in March, 6001 in January.
        assertEquals("ORD-6003", order.getOrderNumber());
    }

    @Test
    public void assetWithNoOrdersReturnsNull() {
        assertNull(assetDAO.getOrderForAsset(CUSTOMER_ID, MISMATCHED_ASSET_ID));
    }

    @Test
    public void statusLookupReturnsNullForAnUnknownAsset() {
        assertEquals(AssetStatusType.ACTIVE, assetDAO.getAssetStatus(CUSTOMER_ID, HEALTHY_ASSET_ID));
        assertNull(assetDAO.getAssetStatus(CUSTOMER_ID, 999999L));
    }

    @Test
    public void customerAssetsComeBackInTagOrder() {
        final List<Asset> assets = assetDAO.getAssetsForCustomer(CUSTOMER_ID);
        assertEquals(4, assets.size());
        assertEquals("AMS-000501", assets.get(0).getAssetTag());
        assertEquals("AMS-000504", assets.get(3).getAssetTag());
    }

    /**
     * The migratable query is where the eligibility rules actually live, so this covers each of the
     * blocking conditions with a fixture that trips exactly one of them.
     */
    @Test
    public void migratableQueryExcludesAssetsWithSomethingInFlight() {
        final Customer customer = new Customer();
        customer.setCustomerId(Long.valueOf(CUSTOMER_ID));

        final List<Asset> migratable = assetDAO.getMigratableAssetsForCustomer(customer);
        assertEquals(1, migratable.size());
        assertEquals(Long.valueOf(HEALTHY_ASSET_ID), migratable.get(0).getAssetId());
        // The owning customer is attached so the caller does not need a second lookup.
        assertEquals(customer, migratable.get(0).getCustomer());
    }

    @Test
    public void migratableQueryToleratesACustomerWithoutAnId() {
        assertTrue(assetDAO.getMigratableAssetsForCustomer(new Customer()).isEmpty());
        assertTrue(assetDAO.getMigratableAssetsForCustomer(null).isEmpty());
    }

    @Test
    public void assetReturnIsInsertedWithAGeneratedKey() {
        final long assetReturnId = assetDAO.addAssetReturn(HEALTHY_ASSET_ID, "SN-ALPHA-001", TEST_USER);
        assertTrue(assetReturnId > 0);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        assertEquals("PENDING", jdbcTemplate.queryForObject(
                "SELECT RETURN_STATUS_CD FROM AMS_ASSET_RETURNS WHERE ASSET_RETURN_ID = ?",
                String.class, Long.valueOf(assetReturnId)));
    }

    @Test
    public void statusUpdateIsScopedToTheCustomer() {
        assertEquals(1, assetDAO.updateAssetStatus(CUSTOMER_ID, HEALTHY_ASSET_ID,
                AssetStatusType.PENDING_DECOMMISSION, TEST_USER));
        assertEquals(AssetStatusType.PENDING_DECOMMISSION,
                assetDAO.getAssetStatus(CUSTOMER_ID, HEALTHY_ASSET_ID));

        // Same asset, wrong customer: no rows touched.
        assertEquals(0, assetDAO.updateAssetStatus(OTHER_CUSTOMER_ID, HEALTHY_ASSET_ID,
                AssetStatusType.ACTIVE, TEST_USER));
    }

    @Test
    public void decommissionIsLoadedForTheAsset() {
        assertNotNull(assetDAO.getDecommissionForAsset(CUSTOMER_ID, 5004L));
        assertNull(assetDAO.getDecommissionForAsset(CUSTOMER_ID, HEALTHY_ASSET_ID));
    }
}
