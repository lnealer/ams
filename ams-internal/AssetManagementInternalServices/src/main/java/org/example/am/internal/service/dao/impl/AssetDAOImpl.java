package org.example.am.internal.service.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.example.am.internal.service.dao.AssetDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetStatusType;
import org.example.am.shared.domain.AssetType;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("assetDAO")
public class AssetDAOImpl extends BaseDAO implements AssetDAO {

    private static final String ATTENTION_COLUMNS =
            "  A.ASSET_ID, A.CUSTOMER_ID, A.ASSET_TAG, A.LEGACY_ASSET_TAG, A.SERIAL_NUMBER,"
          + "  A.ASSET_TYPE_CD, A.ASSET_STATUS_CD, A.ATTENTION_REASON, C.CUSTOMER_NAME ";

    private static final String SELECT_NEEDING_ATTENTION =
            "SELECT * FROM ( SELECT " + ATTENTION_COLUMNS
          + "                  FROM AMS_ASSETS A "
          + "                  JOIN AMS_CUSTOMERS C ON C.CUSTOMER_ID = A.CUSTOMER_ID "
          + "                 WHERE A.NEEDS_ATTENTION_FL = 'Y' "
          + "                 ORDER BY A.MODIFIED_DT DESC, A.ASSET_ID ) "
          + " WHERE ROWNUM <= :maxRows ";

    private static final String SELECT_AT_ZIP_CODE =
            "SELECT " + ATTENTION_COLUMNS
          + "  FROM AMS_ASSETS A "
          + "  JOIN AMS_CUSTOMERS C ON C.CUSTOMER_ID = A.CUSTOMER_ID "
          + "  JOIN AMS_ADDRESSES D ON D.ADDRESS_ID = A.INSTALL_ADDRESS_ID "
          + " WHERE D.ZIP_CODE = :zipCode "
          + " ORDER BY A.ASSET_TAG ";

    private static final String SELECT_INSTALLED_COUNT =
            "SELECT COUNT(*) FROM AMS_ASSETS A "
          + " WHERE A.CUSTOMER_ID = :customerId AND A.ASSET_STATUS_CD IN ('INSTALLED', 'ACTIVE') ";

    private static final RowMapper<Asset> ATTENTION_MAPPER = new AttentionMapper();

    @Override
    public List<Asset> getAssetsNeedingAttention(final int maxRows) {
        return getNamedParameterJdbcTemplate().query(SELECT_NEEDING_ATTENTION,
                ParameterRepository.of(CommonConstants.PARAM_MAX_ROWS, Integer.valueOf(
                        maxRows <= 0 ? CommonConstants.MAX_SEARCH_RESULTS : maxRows)).build(),
                ATTENTION_MAPPER);
    }

    @Override
    public List<Asset> getAssetsAtZipCode(final String zipCode) {
        return getNamedParameterJdbcTemplate().query(SELECT_AT_ZIP_CODE,
                ParameterRepository.of("zipCode", zipCode).build(), ATTENTION_MAPPER);
    }

    @Override
    public int getInstalledAssetCount(final long customerId) {
        final Integer count = getNamedParameterJdbcTemplate().queryForObject(SELECT_INSTALLED_COUNT,
                ParameterRepository.of(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .build(), Integer.class);
        return count == null ? 0 : count.intValue();
    }

    /** Carries the owning customer, because these grids span customers. */
    private static class AttentionMapper implements RowMapper<Asset> {

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
            asset.setAttentionReason(ConversionUtils.getString(rs, "ATTENTION_REASON"));

            final Customer customer = new Customer();
            customer.setCustomerId(ConversionUtils.getLong(rs, "CUSTOMER_ID"));
            customer.setCustomerName(ConversionUtils.getString(rs, "CUSTOMER_NAME"));
            asset.setCustomer(customer);
            return asset;
        }
    }
}
