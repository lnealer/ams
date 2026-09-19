package org.example.am.shared.dao;

import java.util.List;
import org.example.am.shared.domain.AssetProblemType;

/**
 * Resolves why an asset is ineligible for an action, evaluated in SQL so the reasons can be shown
 * without loading the asset's whole object graph.
 */
public interface AssetProblemDAO {

    List<AssetProblemType> getProblems(long assetId);

    boolean hasBlockingProblem(long assetId);
}
