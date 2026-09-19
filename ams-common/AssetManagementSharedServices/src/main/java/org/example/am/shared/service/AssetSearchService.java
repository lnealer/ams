package org.example.am.shared.service;

import java.util.List;

import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.SearchCriteriaType;

/** The internal asset search, capped and sorted for the grid. */
public interface AssetSearchService {

    List<Asset> search(SearchCriteriaType criteriaType, String term);

    List<Asset> search(SearchCriteriaType criteriaType, String term, int maxRows);

    /** @return the total number of matches, which may exceed the number of rows returned */
    int countMatches(SearchCriteriaType criteriaType, String term);

    /** @return {@code true} when the search matched more rows than were returned */
    boolean isTruncated(SearchCriteriaType criteriaType, String term, int returnedRows);
}
