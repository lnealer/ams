package org.example.am.shared.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.example.am.shared.dao.AssetSearchDAO;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.SearchCriteriaType;
import org.example.am.shared.helper.AbstractBaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class AssetSearchDAOImplTest extends AbstractBaseTest {

    @Autowired
    @Qualifier("assetSearchSharedDAO")
    private AssetSearchDAO assetSearchDAO;

    @Test
    public void assetTagSearchAlsoMatchesTheLegacyTag() {
        assertEquals(1, assetSearchDAO.search(SearchCriteriaType.ASSET_TAG, "000501", 50).size());
        assertEquals(1, assetSearchDAO.search(SearchCriteriaType.ASSET_TAG, "NW-9", 50).size());
    }

    @Test
    public void searchResultsCarryTheOwningCustomer() {
        final List<Asset> hits = assetSearchDAO.search(SearchCriteriaType.SERIAL_NUMBER,
                "sn-alpha-001", 50);
        assertEquals(1, hits.size());
        assertNotNull(hits.get(0).getCustomer());
        assertEquals("Riverbend Dental Group", hits.get(0).getCustomer().getCustomerName());
    }

    @Test
    public void customerNameSearchIsCaseInsensitiveAndPartial() {
        assertEquals(4, assetSearchDAO.search(SearchCriteriaType.CUSTOMER_NAME, "riverbend", 50).size());
    }

    @Test
    public void customerIdSearchRequiresANumberAndMatchesExactly() {
        assertEquals(4, assetSearchDAO.search(SearchCriteriaType.CUSTOMER_ID, "1001", 50).size());
        // A non numeric term binds a sentinel that cannot match any row.
        assertTrue(assetSearchDAO.search(SearchCriteriaType.CUSTOMER_ID, "abc", 50).isEmpty());
    }

    @Test
    public void orderNumberAndCircuitSearchReachThroughToTheChildTables() {
        assertEquals(1, assetSearchDAO.search(SearchCriteriaType.ORDER_NUMBER, "ORD-6002", 50).size());
        assertEquals(1, assetSearchDAO.search(SearchCriteriaType.CIRCUIT_ID, "CKT-00099", 50).size());
        assertEquals(1, assetSearchDAO.search(SearchCriteriaType.RMA_NUMBER, "RMA-9501", 50).size());
    }

    @Test
    public void ipAddressSearchIsExactAndLooksAtBothInterfaces() {
        assertEquals(1, assetSearchDAO.search(SearchCriteriaType.IP_ADDRESS, "10.10.5.1", 50).size());
        assertEquals(1, assetSearchDAO.search(SearchCriteriaType.IP_ADDRESS, "64.12.30.6", 50).size());
        // Exact, so a prefix does not match.
        assertTrue(assetSearchDAO.search(SearchCriteriaType.IP_ADDRESS, "10.10.5", 50).isEmpty());
    }

    /**
     * A wildcard the user types must be matched literally, not treated as a wildcard, or a search
     * for "%" would return the whole table.
     */
    @Test
    public void userSuppliedWildcardsAreEscaped() {
        assertTrue(assetSearchDAO.search(SearchCriteriaType.ASSET_TAG, "%", 50).isEmpty());
        assertTrue(assetSearchDAO.search(SearchCriteriaType.ASSET_TAG, "_", 50).isEmpty());
    }

    @Test
    public void rowCapIsApplied() {
        assertEquals(2, assetSearchDAO.search(SearchCriteriaType.CUSTOMER_NAME, "riverbend", 2).size());
        // The count is of matches, not of returned rows.
        assertEquals(4, assetSearchDAO.countMatches(SearchCriteriaType.CUSTOMER_NAME, "riverbend"));
    }

    @Test
    public void blankAndUnsupportedCriteriaReturnNothing() {
        assertTrue(assetSearchDAO.search(SearchCriteriaType.ASSET_TAG, "  ", 50).isEmpty());
        assertTrue(assetSearchDAO.search(null, "anything", 50).isEmpty());
        assertEquals(0, assetSearchDAO.countMatches(null, "anything"));
    }
}
