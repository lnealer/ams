package org.example.am.shared.service.impl;

import java.util.List;

import org.example.am.shared.dao.AssetSearchDAO;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.SearchCriteriaType;
import org.example.am.shared.helper.AssetHelper;
import org.example.am.shared.service.AssetSearchService;
import org.example.am.shared.utils.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("assetSearchService")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class AssetSearchServiceImpl implements AssetSearchService {

    @Autowired
    private AssetSearchDAO assetSearchDAO;

    @Override
    public List<Asset> search(final SearchCriteriaType criteriaType, final String term) {
        return search(criteriaType, term, CommonConstants.MAX_SEARCH_RESULTS);
    }

    @Override
    public List<Asset> search(final SearchCriteriaType criteriaType, final String term,
            final int maxRows) {
        return AssetHelper.sortForDisplay(assetSearchDAO.search(criteriaType, term, maxRows));
    }

    @Override
    public int countMatches(final SearchCriteriaType criteriaType, final String term) {
        return assetSearchDAO.countMatches(criteriaType, term);
    }

    @Override
    public boolean isTruncated(final SearchCriteriaType criteriaType, final String term,
            final int returnedRows) {
        return countMatches(criteriaType, term) > returnedRows;
    }

    public void setAssetSearchDAO(final AssetSearchDAO assetSearchDAO) {
        this.assetSearchDAO = assetSearchDAO;
    }
}
