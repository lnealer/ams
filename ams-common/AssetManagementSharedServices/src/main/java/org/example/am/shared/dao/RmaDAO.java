package org.example.am.shared.dao;

import java.util.List;
import org.example.am.shared.domain.Rma;
import org.example.am.shared.domain.RmaStatusType;

/**
 * Reads and writes {@code AMS_RMAS}.
 */
public interface RmaDAO {

    Rma getRma(long rmaId);

    List<Rma> getRmasForAsset(long assetId);

    List<Rma> getOverdueRmas();

    int updateStatus(long rmaId, RmaStatusType status, String userId);
}
