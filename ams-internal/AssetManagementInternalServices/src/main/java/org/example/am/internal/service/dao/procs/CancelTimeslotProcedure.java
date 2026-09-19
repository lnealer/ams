package org.example.am.internal.service.dao.procs;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Gives back a place on a calendar slot when the work it was booked for is cancelled.
 */
public class CancelTimeslotProcedure extends StoredProcedure {

    private static final String PROCEDURE_NAME = "AMS_SCHEDULING_PG.cancel_timeslot";

    private static final String IN_TIMESLOT_ID = "p_timeslot_id";
    private static final String IN_ENTITY_ID = "p_entity_id";
    private static final String IN_ENTITY_TYPE = "p_entity_type_cd";
    private static final String IN_USER_ID = "p_user_id";
    private static final String OUT_STATUS = "p_status_cd";


    /** Returned in the status parameter when the procedure completed. */
    public static final String STATUS_OK = "OK";

    public CancelTimeslotProcedure(final DataSource dataSource) {
        super(dataSource, PROCEDURE_NAME);
        declareParameter(new SqlParameter(IN_TIMESLOT_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_ENTITY_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_ENTITY_TYPE, Types.VARCHAR));
        declareParameter(new SqlParameter(IN_USER_ID, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_STATUS, Types.VARCHAR));
        compile();
    }

    /**
     * Releases the reservation held by the given entity.
     *
     * @return the status code the procedure returned
     */
    public String cancel(final long timeslotId,
            final long entityId,
            final String entityTypeCode,
            final String userId) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(IN_TIMESLOT_ID, Long.valueOf(timeslotId));
        inputs.put(IN_ENTITY_ID, Long.valueOf(entityId));
        inputs.put(IN_ENTITY_TYPE, entityTypeCode);
        inputs.put(IN_USER_ID, userId);
        return (String) execute(inputs).get(OUT_STATUS);
    }
}
