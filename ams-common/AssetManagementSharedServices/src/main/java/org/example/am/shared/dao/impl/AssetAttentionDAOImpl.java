package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.example.am.shared.dao.AssetAttentionDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetStatusType;
import org.example.am.shared.domain.AssetType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Drives the 'needs attention' banner: the flag and reason are denormalised onto the asset row so
 * the search grid can render them without a join.
 */
@Repository("assetAttentionSharedDAO")
public class AssetAttentionDAOImpl extends BaseDAO implements AssetAttentionDAO {

    private static final String SELECT_NEEDS_ATTENTION =
            "SELECT A.ASSET_ID, A.ASSET_TAG, A.LEGACY_ASSET_TAG, A.SERIAL_NUMBER, A.ASSET_TYPE_CD,"
          + "       A.ASSET_STATUS_CD, A.ATTENTION_REASON "
          + "  FROM AMS_ASSETS A "
          + " WHERE A.CUSTOMER_ID = :customerId AND A.NEEDS_ATTENTION_FL = 'Y' "
          + " ORDER BY A.MODIFIED_DT DESC ";

    private static final String FLAG_ASSET =
            "UPDATE AMS_ASSETS SET NEEDS_ATTENTION_FL = 'Y', ATTENTION_REASON = :reason,"
          + "       MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId WHERE ASSET_ID = :assetId ";

    private static final String CLEAR_FLAG =
            "UPDATE AMS_ASSETS SET NEEDS_ATTENTION_FL = 'N', ATTENTION_REASON = NULL,"
          + "       MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId WHERE ASSET_ID = :assetId ";

    private static final RowMapper<Asset> ATTENTION_MAPPER = new RowMapper<Asset>() {

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
            asset.setNeedsAttention(true);
            asset.setAttentionReason(ConversionUtils.getString(rs, "ATTENTION_REASON"));
            return asset;
        }
    };

    @Override
    public List<Asset> getAssetsNeedingAttention(final long customerId) {
        return getNamedParameterJdbcTemplate().query(SELECT_NEEDS_ATTENTION,
                ParameterRepository.of(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .build(), ATTENTION_MAPPER);
    }

    @Override
    public int flagAsset(final long assetId, final String reason, final String userId) {
        return getNamedParameterJdbcTemplate().update(FLAG_ASSET,
                ParameterRepository.create()
                        .with("reason", reason)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                        .build());
    }

    @Override
    public int clearFlag(final long assetId, final String userId) {
        return getNamedParameterJdbcTemplate().update(CLEAR_FLAG,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                        .build());
    }
}
