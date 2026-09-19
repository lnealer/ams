package org.example.am.shared.service;

import java.util.Map;

import org.example.am.shared.domain.PropertyType;

/**
 * Typed access to the runtime configuration table.
 *
 * <p>Callers ask for the type they want and supply a default, so a property that has never been
 * inserted, or that has been given a value that will not parse, degrades to the compiled-in default
 * rather than failing the request.</p>
 */
public interface ConfigService {

    String getString(PropertyType propertyType, String defaultValue);

    int getInt(PropertyType propertyType, int defaultValue);

    boolean getBoolean(PropertyType propertyType, boolean defaultValue);

    Map<String, String> getAllProperties();

    /** @return {@code true} when the application is pointed at a non production environment */
    boolean isNonProductionEnvironment();

    String getEnvironmentName();
}
