package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fields the internal asset search can be driven by.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class SearchCriteriaType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, SearchCriteriaType> VALUES = new LinkedHashMap<String, SearchCriteriaType>();

    public static final SearchCriteriaType ASSET_TAG = register("ASSETTAG", "Asset Tag", Long.valueOf(1L));
    public static final SearchCriteriaType SERIAL_NUMBER = register("SERIAL", "Serial Number", Long.valueOf(2L));
    public static final SearchCriteriaType CUSTOMER_NAME = register("CUSTNAME", "Customer Name", Long.valueOf(3L));
    public static final SearchCriteriaType CUSTOMER_ID = register("CUSTID", "Customer Id", Long.valueOf(4L));
    public static final SearchCriteriaType ORDER_NUMBER = register("ORDERNUM", "Order Number", Long.valueOf(5L));
    public static final SearchCriteriaType CIRCUIT_ID = register("CIRCUITID", "Circuit Id", Long.valueOf(6L));
    public static final SearchCriteriaType IP_ADDRESS = register("IPADDR", "IP Address", Long.valueOf(7L));
    public static final SearchCriteriaType RMA_NUMBER = register("RMANUM", "RMA Number", Long.valueOf(8L));

    private SearchCriteriaType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static SearchCriteriaType register(final String code, final String description, final Long databaseId) {
        final SearchCriteriaType type = new SearchCriteriaType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static SearchCriteriaType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static SearchCriteriaType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final SearchCriteriaType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<SearchCriteriaType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
