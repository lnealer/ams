package org.example.am.internal.service.dao.procs;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Books the change window for a site type change.
 *
 * <p>A site type change reshapes the circuit as well as the device, so capacity has to be taken on
 * both the engineering calendar and the carrier's change window in one atomic step. That is what
 * makes this a different procedure rather than a flag on the simple one.</p>
 */
public class ReserveNCRDateProcedureComplex extends StoredProcedure {

    private static final String PROCEDURE_NAME = "AMS_NCR_SCHEDULING_PG.schedule_site_type_ncr";

    private static final String IN_NCR_ID = "p_ncr_id";
    private static final String IN_ASSET_ID = "p_asset_id";
    private static final String IN_SITE_TYPE = "p_site_type_cd";
    private static final String IN_SCHEDULED_DATE = "p_scheduled_dt";
    private static final String IN_USER_ID = "p_user_id";
    private static final String OUT_STATUS = "p_status_cd";
    private static final String OUT_CIRCUIT_WINDOW_ID = "p_circuit_window_id";


    /** Returned in the status parameter when the procedure completed. */
    public static final String STATUS_OK = "OK";

    public ReserveNCRDateProcedureComplex(final DataSource dataSource) {
        super(dataSource, PROCEDURE_NAME);
        declareParameter(new SqlParameter(IN_NCR_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_ASSET_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_SITE_TYPE, Types.VARCHAR));
        declareParameter(new SqlParameter(IN_SCHEDULED_DATE, Types.TIMESTAMP));
        declareParameter(new SqlParameter(IN_USER_ID, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_STATUS, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_CIRCUIT_WINDOW_ID, Types.NUMERIC));
        compile();
    }

    /**
     * Books the device and circuit windows together.
     *
     * @return the status code the procedure returned
     */
    public String reserve(final long networkChangeRequestId,
            final long assetId,
            final String siteTypeCode,
            final java.util.Date scheduledDate,
            final String userId) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(IN_NCR_ID, Long.valueOf(networkChangeRequestId));
        inputs.put(IN_ASSET_ID, Long.valueOf(assetId));
        inputs.put(IN_SITE_TYPE, siteTypeCode);
        inputs.put(IN_SCHEDULED_DATE, scheduledDate == null ? null : new java.sql.Timestamp(scheduledDate.getTime()));
        inputs.put(IN_USER_ID, userId);
        return (String) execute(inputs).get(OUT_STATUS);
    }
}
