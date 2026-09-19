package org.example.am.shared.dao;

import java.util.Map;

/**
 * Loads the reference data cached at application start: property values and the loadable type
 * database ids, so that lookups do not hit the database on every request.
 */
public interface PreLoadServiceDAO {

    Map<String, Long> getLoadableTypeIds(String typeName);

    int getReferenceDataRowCount();
}
