package org.example.am.internal.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.example.am.shared.domain.SearchCriteriaType;

/**
 * The asset search form and its results.
 */
public class SearchModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private SearchCriteriaType searchCriteriaType = SearchCriteriaType.ASSET_TAG;
    private String searchTerm;
    private int maxRows;

    private List<AssetGridRow> results = new ArrayList<AssetGridRow>();
    private int totalMatches;
    private boolean truncated;

    public SearchCriteriaType getSearchCriteriaType() {
        return searchCriteriaType;
    }

    public void setSearchCriteriaType(final SearchCriteriaType searchCriteriaType) {
        this.searchCriteriaType = searchCriteriaType;
    }

    /** Bound from the form, where the criteria arrives as its code. */
    public void setSearchCriteria(final String code) {
        final SearchCriteriaType resolved = SearchCriteriaType.lookup(code);
        if (resolved != null) {
            searchCriteriaType = resolved;
        }
    }

    public String getSearchCriteria() {
        return searchCriteriaType == null ? null : searchCriteriaType.getCode();
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(final String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(final int maxRows) {
        this.maxRows = maxRows;
    }

    public List<AssetGridRow> getResults() {
        return results;
    }

    public void setResults(final List<AssetGridRow> results) {
        this.results = results == null ? new ArrayList<AssetGridRow>() : results;
    }

    public int getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(final int totalMatches) {
        this.totalMatches = totalMatches;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(final boolean truncated) {
        this.truncated = truncated;
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    /** @return the available criteria, for the drop-down */
    public java.util.Collection<SearchCriteriaType> getSearchCriteriaTypes() {
        return SearchCriteriaType.values();
    }
}
