package org.example.am.shared.dao;

import java.util.List;

import org.example.am.shared.domain.SaveForLaterType;

/**
 * Parks a partially completed form in {@code AMS_SAVE_FOR_LATER} so the user can come back to it.
 *
 * <p>The form state is stored as an opaque serialised payload keyed by user, customer and flow;
 * only one saved form per key is kept, so saving again replaces the previous one.</p>
 */
public interface SaveForLaterDAO {

    String getSavedForm(String userId, long customerId, SaveForLaterType type);

    List<Long> getSavedFormKeys(String userId, SaveForLaterType type);

    /** Inserts or replaces the saved payload. */
    int saveForm(String userId, long customerId, SaveForLaterType type, String payload);

    int deleteSavedForm(String userId, long customerId, SaveForLaterType type);
}
