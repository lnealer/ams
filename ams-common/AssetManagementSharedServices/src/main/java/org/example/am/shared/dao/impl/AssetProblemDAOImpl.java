package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.example.am.shared.dao.AssetProblemDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.AssetProblemType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Resolves why an asset is ineligible for an action, evaluated in SQL so the reasons can be shown
 * without loading the asset's whole object graph.
 */
@Repository("assetProblemSharedDAO")
public class AssetProblemDAOImpl extends BaseDAO implements AssetProblemDAO {

    /**
     * One branch per problem, unioned so a single round trip returns every reason at once. Each
     * branch selects the problem's code literal when its condition holds.
     */
    private static final String SELECT_PROBLEMS =
            "SELECT 'PENDORDER' AS PROBLEM_CD FROM DUAL "
          + " WHERE EXISTS ( SELECT 1 FROM AMS_ORDERS O WHERE O.ASSET_ID = :assetId "
          + "                 AND O.ORDER_STATUS_CD NOT IN ('COMPLETED', 'CANCELLED') ) "
          + "UNION ALL "
          + "SELECT 'PENDNCR' FROM DUAL "
          + " WHERE EXISTS ( SELECT 1 FROM AMS_NETWORK_CHANGE_REQUESTS N WHERE N.ASSET_ID = :assetId "
          + "                 AND N.NCR_STATUS_CD NOT IN ('COMPLETED', 'CANCELLED') ) "
          + "UNION ALL "
          + "SELECT 'DECOMSCHED' FROM DUAL "
          + " WHERE EXISTS ( SELECT 1 FROM AMS_DECOMMISSIONS D WHERE D.ASSET_ID = :assetId "
          + "                 AND D.DECOM_STATUS_CD IN ('REQUESTED', 'SCHEDULED') ) "
          + "UNION ALL "
          + "SELECT 'CFGMISMATCH' FROM DUAL "
          + " WHERE EXISTS ( SELECT 1 FROM AMS_ASSET_CONFIGS G WHERE G.ASSET_ID = :assetId "
          + "                 AND G.CONFIG_STATUS_CD = 'MISMATCH' ) ";

    private static final RowMapper<String> PROBLEM_CODE_MAPPER = new RowMapper<String>() {

        @Override
        public String mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            return ConversionUtils.getString(rs, "PROBLEM_CD");
        }
    };

    @Override
    public List<AssetProblemType> getProblems(final long assetId) {
        final List<AssetProblemType> problems = new ArrayList<AssetProblemType>();
        for (final String code : getNamedParameterJdbcTemplate().query(SELECT_PROBLEMS,
                ParameterRepository.of(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId)).build(),
                PROBLEM_CODE_MAPPER)) {
            final AssetProblemType problem = AssetProblemType.lookup(code);
            if (problem != null) {
                problems.add(problem);
            }
        }
        return problems;
    }

    @Override
    public boolean hasBlockingProblem(final long assetId) {
        return !getProblems(assetId).isEmpty();
    }
}
