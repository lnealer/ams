package org.example.am.internal.service.dao.procs;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Releases a booked decommission date and reopens the asset for other work.
 */
public class CancelDecommissionDateProcedure extends StoredProcedure {

    private static final String PROCEDURE_NAME = "AMS_SCHEDULING_PG.cancel_decommission";

    private static final String IN_DECOMMISSION_ID = "p_decommission_id";
    private static final String IN_REASON = "p_reason";
    private static final String IN_USER_ID = "p_user_id";
    private static final String OUT_STATUS = "p_status_cd";


    /** Returned in the status parameter when the procedure completed. */
    public static final String STATUS_OK = "OK";

    public CancelDecommissionDateProcedure(final DataSource dataSource) {
        super(dataSource, PROCEDURE_NAME);
        declareParameter(new SqlParameter(IN_DECOMMISSION_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_REASON, Types.VARCHAR));
        declareParameter(new SqlParameter(IN_USER_ID, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_STATUS, Types.VARCHAR));
        compile();
    }

    /**
     * Releases the decommission date.
     *
     * @return the status code the procedure returned
     */
    public String cancel(final long decommissionId,
            final String reason,
            final String userId) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(IN_DECOMMISSION_ID, Long.valueOf(decommissionId));
        inputs.put(IN_REASON, reason);
        inputs.put(IN_USER_ID, userId);
        return (String) execute(inputs).get(OUT_STATUS);
    }
}
