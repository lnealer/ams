package org.example.am.internal.service.dao.procs;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Releases both the device window and the circuit window held by a site type change.
 */
public class CancelNCRDateProcedureComplex extends StoredProcedure {

    private static final String PROCEDURE_NAME = "AMS_NCR_SCHEDULING_PG.cancel_site_type_ncr";

    private static final String IN_NCR_ID = "p_ncr_id";
    private static final String IN_ASSET_ID = "p_asset_id";
    private static final String IN_REASON = "p_reason";
    private static final String IN_USER_ID = "p_user_id";
    private static final String OUT_STATUS = "p_status_cd";
    private static final String OUT_RELEASED_COUNT = "p_released_count";


    /** Returned in the status parameter when the procedure completed. */
    public static final String STATUS_OK = "OK";

    public CancelNCRDateProcedureComplex(final DataSource dataSource) {
        super(dataSource, PROCEDURE_NAME);
        declareParameter(new SqlParameter(IN_NCR_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_ASSET_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_REASON, Types.VARCHAR));
        declareParameter(new SqlParameter(IN_USER_ID, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_STATUS, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_RELEASED_COUNT, Types.NUMERIC));
        compile();
    }

    /**
     * Releases both reservations.
     *
     * @return the status code the procedure returned
     */
    public String cancel(final long networkChangeRequestId,
            final long assetId,
            final String reason,
            final String userId) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(IN_NCR_ID, Long.valueOf(networkChangeRequestId));
        inputs.put(IN_ASSET_ID, Long.valueOf(assetId));
        inputs.put(IN_REASON, reason);
        inputs.put(IN_USER_ID, userId);
        return (String) execute(inputs).get(OUT_STATUS);
    }
}
