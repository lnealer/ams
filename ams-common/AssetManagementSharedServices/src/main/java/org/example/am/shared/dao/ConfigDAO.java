package org.example.am.shared.dao;

import java.util.Map;

import org.example.am.shared.domain.PropertyType;

/**
 * Reads {@code AMS_PROPERTIES}, the runtime configuration table that lets operations change lead
 * times and feature switches without a redeploy.
 */
public interface ConfigDAO {

    String getPropertyValue(PropertyType propertyType);

    Map<String, String> getAllProperties();

    int updateProperty(PropertyType propertyType, String value, String userId);
}
