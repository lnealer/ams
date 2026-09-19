package org.example.am.shared.service;

import java.util.Map;

/**
 * Loads the reference data cached for the life of the application context.
 */
public interface PreLoadService {

    Map<String, Long> getLoadableTypeIds(String typeName);

    /**
     * @return {@code true} when the reference tables are populated; checked at start-up so a
     *         half-migrated database fails loudly rather than silently rendering empty drop-downs
     */
    boolean isReferenceDataPresent();
}
