package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.PreLoadServiceDAO;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

/**
 * Loads the reference data cached at application start: property values and the loadable type
 * database ids, so that lookups do not hit the database on every request.
 */
@Repository("preLoadServiceSharedDAO")
public class PreLoadServiceDAOImpl extends BaseDAO implements PreLoadServiceDAO {

    private static final String SELECT_TYPE_IDS =
            "SELECT L.TYPE_CD, L.TYPE_ID FROM AMS_LOADABLE_TYPES L "
          + " WHERE L.TYPE_NAME = :typeName ORDER BY L.SORT_ORDER ";

    private static final String SELECT_REFERENCE_COUNT =
            "SELECT COUNT(*) FROM AMS_LOADABLE_TYPES ";

    @Override
    public Map<String, Long> getLoadableTypeIds(final String typeName) {
        final Map<String, Long> ids = new LinkedHashMap<String, Long>();
        getNamedParameterJdbcTemplate().query(SELECT_TYPE_IDS,
                ParameterRepository.of("typeName", typeName).build(), new RowCallbackHandler() {

                    @Override
                    public void processRow(final ResultSet rs) throws SQLException {
                        ids.put(ConversionUtils.getString(rs, "TYPE_CD"),
                                ConversionUtils.getLong(rs, "TYPE_ID"));
                    }
                });
        return ids;
    }

    @Override
    public int getReferenceDataRowCount() {
        final Integer count = getNamedParameterJdbcTemplate().queryForObject(
                SELECT_REFERENCE_COUNT, ParameterRepository.create().build(), Integer.class);
        return count == null ? 0 : count.intValue();
    }
}
