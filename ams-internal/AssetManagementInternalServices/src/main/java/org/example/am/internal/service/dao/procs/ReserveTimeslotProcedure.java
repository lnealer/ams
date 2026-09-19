package org.example.am.internal.service.dao.procs;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Takes a place on an installation or techline calendar slot.
 *
 * <p>The capacity decrement locks the slot row inside the procedure, so two operators booking the
 * last place cannot both succeed. Losing that race comes back as a status, not an exception.</p>
 *
 * <p>The parameter list must stay identical to the shared module's class of the same name: both
 * call the one {@code AMS_SCHEDULING_PG.reserve_timeslot}, and Spring binds positionally, so a
 * difference in arity or order would make one of the two fail with ORA-06550. {@code p_message}
 * is declared but not read here; the shared wrapper exposes it.</p>
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


    /** Returned in the status parameter when the procedure completed. */
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
     * Reserves one place on the slot for the given entity.
     *
     * @return the status code the procedure returned
     */
    public String reserve(final long timeslotId,
            final long entityId,
            final String entityTypeCode,
            final java.util.Date scheduledDate,
            final String userId) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(IN_TIMESLOT_ID, Long.valueOf(timeslotId));
        inputs.put(IN_ENTITY_ID, Long.valueOf(entityId));
        inputs.put(IN_ENTITY_TYPE, entityTypeCode);
        inputs.put(IN_SCHEDULED_DATE, scheduledDate == null ? null : new java.sql.Timestamp(scheduledDate.getTime()));
        inputs.put(IN_USER_ID, userId);
        return (String) execute(inputs).get(OUT_STATUS);
    }
}
