package org.example.am.shared.dao;

import java.util.List;

import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.SearchCriteriaType;

/**
 * The internal asset search. Separate from {@link AssetDAO} because it is not customer scoped: an
 * operator searching by serial number does not know which customer owns the hardware yet.
 */
public interface AssetSearchDAO {

    /**
     * @param criteriaType which column the term applies to
     * @param term         the user's search term
     * @param maxRows      hard cap on the result set
     */
    List<Asset> search(SearchCriteriaType criteriaType, String term, int maxRows);

    int countMatches(SearchCriteriaType criteriaType, String term);
}
