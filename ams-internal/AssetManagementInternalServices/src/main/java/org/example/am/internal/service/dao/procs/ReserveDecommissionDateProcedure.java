package org.example.am.internal.service.dao.procs;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Books the date an asset will be taken out of service.
 */
public class ReserveDecommissionDateProcedure extends StoredProcedure {

    private static final String PROCEDURE_NAME = "AMS_SCHEDULING_PG.schedule_decommission";

    private static final String IN_DECOMMISSION_ID = "p_decommission_id";
    private static final String IN_ASSET_ID = "p_asset_id";
    private static final String IN_SCHEDULED_DATE = "p_scheduled_dt";
    private static final String IN_HARDWARE_RETURN = "p_hardware_return_fl";
    private static final String IN_USER_ID = "p_user_id";
    private static final String OUT_STATUS = "p_status_cd";


    /** Returned in the status parameter when the procedure completed. */
    public static final String STATUS_OK = "OK";

    public ReserveDecommissionDateProcedure(final DataSource dataSource) {
        super(dataSource, PROCEDURE_NAME);
        declareParameter(new SqlParameter(IN_DECOMMISSION_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_ASSET_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_SCHEDULED_DATE, Types.TIMESTAMP));
        declareParameter(new SqlParameter(IN_HARDWARE_RETURN, Types.VARCHAR));
        declareParameter(new SqlParameter(IN_USER_ID, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_STATUS, Types.VARCHAR));
        compile();
    }

    /**
     * Books the decommission date.
     *
     * @return the status code the procedure returned
     */
    public String reserve(final long decommissionId,
            final long assetId,
            final java.util.Date scheduledDate,
            final boolean hardwareReturnRequired,
            final String userId) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(IN_DECOMMISSION_ID, Long.valueOf(decommissionId));
        inputs.put(IN_ASSET_ID, Long.valueOf(assetId));
        inputs.put(IN_SCHEDULED_DATE, scheduledDate == null ? null : new java.sql.Timestamp(scheduledDate.getTime()));
        inputs.put(IN_HARDWARE_RETURN, hardwareReturnRequired ? "Y" : "N");
        inputs.put(IN_USER_ID, userId);
        return (String) execute(inputs).get(OUT_STATUS);
    }
}
