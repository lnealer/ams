package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.am.shared.dao.AssetSearchDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetStatusType;
import org.example.am.shared.domain.AssetType;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.domain.SearchCriteriaType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Builds the search predicate from a fixed map of criteria to SQL fragments.
 *
 * <p>The term itself is always a bind variable: the criteria type only selects which pre-written
 * fragment is used, so nothing the user types reaches the statement text.</p>
 */
@Repository("assetSearchSharedDAO")
public class AssetSearchDAOImpl extends BaseDAO implements AssetSearchDAO {

    private static final String SELECT_PREFIX =
            "SELECT A.ASSET_ID, A.CUSTOMER_ID, A.ASSET_TAG, A.LEGACY_ASSET_TAG, A.SERIAL_NUMBER,"
          + "       A.ASSET_TYPE_CD, A.ASSET_STATUS_CD, A.NEEDS_ATTENTION_FL, A.ATTENTION_REASON,"
          + "       C.CUSTOMER_NAME "
          + "  FROM AMS_ASSETS A "
          + "  JOIN AMS_CUSTOMERS C ON C.CUSTOMER_ID = A.CUSTOMER_ID "
          + " WHERE ";

    private static final String COUNT_PREFIX =
            "SELECT COUNT(*) "
          + "  FROM AMS_ASSETS A "
          + "  JOIN AMS_CUSTOMERS C ON C.CUSTOMER_ID = A.CUSTOMER_ID "
          + " WHERE ";

    private static final String ORDER_BY = " ORDER BY A.ASSET_TAG ";

    private static final RowMapper<Asset> SEARCH_RESULT_MAPPER = new AssetSearchResultMapper();

    @Override
    public List<Asset> search(final SearchCriteriaType criteriaType, final String term,
            final int maxRows) {
        final String predicate = predicateFor(criteriaType);
        if (predicate == null || term == null || term.trim().length() == 0) {
            return new ArrayList<Asset>();
        }
        final int cap = maxRows <= 0 ? CommonConstants.MAX_SEARCH_RESULTS : maxRows;
        final String sql = "SELECT * FROM ( " + SELECT_PREFIX + predicate + ORDER_BY
                + " ) WHERE ROWNUM <= :maxRows ";
        return getNamedParameterJdbcTemplate().query(sql,
                bind(criteriaType, term).with(CommonConstants.PARAM_MAX_ROWS, Integer.valueOf(cap))
                        .build(),
                SEARCH_RESULT_MAPPER);
    }

    @Override
    public int countMatches(final SearchCriteriaType criteriaType, final String term) {
        final String predicate = predicateFor(criteriaType);
        if (predicate == null || term == null || term.trim().length() == 0) {
            return 0;
        }
        final Integer count = getNamedParameterJdbcTemplate().queryForObject(
                COUNT_PREFIX + predicate, bind(criteriaType, term).build(), Integer.class);
        return count == null ? 0 : count.intValue();
    }

    /**
     * @return the pre-written {@code WHERE} fragment for this criteria, or {@code null} when the
     *         criteria is not searchable
     */
    private static String predicateFor(final SearchCriteriaType criteriaType) {
        if (SearchCriteriaType.ASSET_TAG.equals(criteriaType)) {
            return " ( UPPER(A.ASSET_TAG) LIKE :term ESCAPE '\\' "
                 + "OR UPPER(A.LEGACY_ASSET_TAG) LIKE :term ESCAPE '\\' ) ";
        }
        if (SearchCriteriaType.SERIAL_NUMBER.equals(criteriaType)) {
            return " UPPER(A.SERIAL_NUMBER) LIKE :term ESCAPE '\\' ";
        }
        if (SearchCriteriaType.CUSTOMER_NAME.equals(criteriaType)) {
            return " UPPER(C.CUSTOMER_NAME) LIKE :term ESCAPE '\\' ";
        }
        if (SearchCriteriaType.CUSTOMER_ID.equals(criteriaType)) {
            return " A.CUSTOMER_ID = :exactNumber ";
        }
        if (SearchCriteriaType.ORDER_NUMBER.equals(criteriaType)) {
            return " EXISTS ( SELECT 1 FROM AMS_ORDERS O WHERE O.ASSET_ID = A.ASSET_ID "
                 + "          AND UPPER(O.ORDER_NUMBER) LIKE :term ESCAPE '\\' ) ";
        }
        if (SearchCriteriaType.CIRCUIT_ID.equals(criteriaType)) {
            return " EXISTS ( SELECT 1 FROM AMS_ASSET_CONFIGS G WHERE G.ASSET_ID = A.ASSET_ID "
                 + "          AND UPPER(G.CIRCUIT_ID) LIKE :term ESCAPE '\\' ) ";
        }
        if (SearchCriteriaType.IP_ADDRESS.equals(criteriaType)) {
            return " EXISTS ( SELECT 1 FROM AMS_ASSET_CONFIGS G WHERE G.ASSET_ID = A.ASSET_ID "
                 + "          AND ( G.LAN_IP_ADDRESS = :exactTerm OR G.WAN_IP_ADDRESS = :exactTerm ) ) ";
        }
        if (SearchCriteriaType.RMA_NUMBER.equals(criteriaType)) {
            return " EXISTS ( SELECT 1 FROM AMS_RMAS R WHERE R.ASSET_ID = A.ASSET_ID "
                 + "          AND UPPER(R.RMA_NUMBER) LIKE :term ESCAPE '\\' ) ";
        }
        return null;
    }

    /**
     * Every predicate gets all three bindings, whether or not it references them: the named
     * parameter template ignores unused entries, and this keeps the binding logic out of the
     * per-criteria branches.
     */
    private static ParameterRepository bind(final SearchCriteriaType criteriaType, final String term) {
        final ParameterRepository parameters = ParameterRepository.create()
                .withLikeTerm("term", term)
                .with("exactTerm", term.trim());
        Long exactNumber = null;
        try {
            exactNumber = Long.valueOf(term.trim());
        } catch (final NumberFormatException notANumber) {
            exactNumber = Long.valueOf(-1L);
        }
        return parameters.with("exactNumber", exactNumber);
    }

    private static class AssetSearchResultMapper implements RowMapper<Asset> {

        @Override
        public Asset mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Asset asset = new Asset();
            asset.setAssetId(ConversionUtils.getLong(rs, "ASSET_ID"));
            asset.setAssetTag(ConversionUtils.getString(rs, "ASSET_TAG"));
            asset.setLegacyAssetTag(ConversionUtils.getString(rs, "LEGACY_ASSET_TAG"));
            asset.setSerialNumber(ConversionUtils.getString(rs, "SERIAL_NUMBER"));
            asset.setAssetType(AssetType.lookup(ConversionUtils.getString(rs, "ASSET_TYPE_CD")));
            asset.setAssetStatusType(
                    AssetStatusType.lookup(ConversionUtils.getString(rs, "ASSET_STATUS_CD")));
            asset.setNeedsAttention(ConversionUtils.getBoolean(rs, "NEEDS_ATTENTION_FL"));
            asset.setAttentionReason(ConversionUtils.getString(rs, "ATTENTION_REASON"));

            // The grid shows the owning customer, so a light customer is attached to every hit.
            final Customer customer = new Customer();
            customer.setCustomerId(ConversionUtils.getLong(rs, "CUSTOMER_ID"));
            customer.setCustomerName(ConversionUtils.getString(rs, "CUSTOMER_NAME"));
            asset.setCustomer(customer);
            return asset;
        }
    }
}
