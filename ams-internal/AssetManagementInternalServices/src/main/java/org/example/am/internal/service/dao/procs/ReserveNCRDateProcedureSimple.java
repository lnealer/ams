package org.example.am.internal.service.dao.procs;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Books the change window for a network change request that only touches the managed device.
 */
public class ReserveNCRDateProcedureSimple extends StoredProcedure {

    private static final String PROCEDURE_NAME = "AMS_NCR_SCHEDULING_PG.schedule_ncr";

    private static final String IN_NCR_ID = "p_ncr_id";
    private static final String IN_SCHEDULED_DATE = "p_scheduled_dt";
    private static final String IN_USER_ID = "p_user_id";
    private static final String OUT_STATUS = "p_status_cd";


    /** Returned in the status parameter when the procedure completed. */
    public static final String STATUS_OK = "OK";

    public ReserveNCRDateProcedureSimple(final DataSource dataSource) {
        super(dataSource, PROCEDURE_NAME);
        declareParameter(new SqlParameter(IN_NCR_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_SCHEDULED_DATE, Types.TIMESTAMP));
        declareParameter(new SqlParameter(IN_USER_ID, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_STATUS, Types.VARCHAR));
        compile();
    }

    /**
     * Books the change window.
     *
     * @return the status code the procedure returned
     */
    public String reserve(final long networkChangeRequestId,
            final java.util.Date scheduledDate,
            final String userId) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(IN_NCR_ID, Long.valueOf(networkChangeRequestId));
        inputs.put(IN_SCHEDULED_DATE, scheduledDate == null ? null : new java.sql.Timestamp(scheduledDate.getTime()));
        inputs.put(IN_USER_ID, userId);
        return (String) execute(inputs).get(OUT_STATUS);
    }
}
