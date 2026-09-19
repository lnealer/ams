package org.example.am.shared.dao.procs;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Reserves capacity on a calendar timeslot.
 *
 * <p>The decrement has to be atomic against concurrent operators, so it lives in a PL/SQL procedure
 * that locks the slot row rather than in a read-then-update pair here. The procedure returns a
 * status so that losing the race is an ordinary outcome rather than an exception.</p>
 */
public class ReserveTimeslotProcedure extends StoredProcedure {

    private static final String PROCEDURE_NAME = "AMS_SCHEDULING_PG.reserve_timeslot";

    private static final String IN_TIMESLOT_ID = "p_timeslot_id";
    private static final String IN_ENTITY_ID = "p_entity_id";
    private static final String IN_ENTITY_TYPE = "p_entity_type_cd";
    private static final String IN_SCHEDULED_DATE = "p_scheduled_dt";
    private static final String IN_USER_ID = "p_user_id";
    private static final String OUT_STATUS = "p_status_cd";
    private static final String OUT_MESSAGE = "p_message";

    /** Returned in {@code p_status_cd} when the slot filled up before this caller got to it. */
    public static final String STATUS_NO_CAPACITY = "NO_CAPACITY";
    public static final String STATUS_OK = "OK";

    public ReserveTimeslotProcedure(final DataSource dataSource) {
        super(dataSource, PROCEDURE_NAME);
        declareParameter(new SqlParameter(IN_TIMESLOT_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_ENTITY_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_ENTITY_TYPE, Types.VARCHAR));
        declareParameter(new SqlParameter(IN_SCHEDULED_DATE, Types.TIMESTAMP));
        declareParameter(new SqlParameter(IN_USER_ID, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_STATUS, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_MESSAGE, Types.VARCHAR));
        compile();
    }

    /**
     * @return the status code the procedure returned; {@link #STATUS_OK} when the slot was reserved
     */
    public String reserve(final long timeslotId, final long entityId, final String entityType,
            final Date scheduledDate, final String userId) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(IN_TIMESLOT_ID, Long.valueOf(timeslotId));
        inputs.put(IN_ENTITY_ID, Long.valueOf(entityId));
        inputs.put(IN_ENTITY_TYPE, entityType);
        inputs.put(IN_SCHEDULED_DATE,
                scheduledDate == null ? null : new java.sql.Timestamp(scheduledDate.getTime()));
        inputs.put(IN_USER_ID, userId);
        final Map<String, Object> results = execute(inputs);
        return (String) results.get(OUT_STATUS);
    }

    public String getLastMessage(final Map<String, Object> results) {
        return (String) results.get(OUT_MESSAGE);
    }
}
