package org.example.am.internal.service.dao.procs;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Queues a templated notification against a business entity, resolving recipients from the
 * entity's contacts and suppressing duplicates raised inside the de-duplication window.
 */
public class AddEntityEmailProcedure extends StoredProcedure {

    private static final String PROCEDURE_NAME = "AMS_EMAIL_PG.add_entity_email";

    private static final String IN_ENTITY_TYPE = "p_entity_type_cd";
    private static final String IN_ENTITY_ID = "p_entity_id";
    private static final String IN_TEMPLATE = "p_template_cd";
    private static final String IN_USER_ID = "p_user_id";
    private static final String OUT_STATUS = "p_status_cd";
    private static final String OUT_EMAIL_ID = "p_email_id";


    /** Returned in the status parameter when the procedure completed. */
    public static final String STATUS_OK = "OK";

    public AddEntityEmailProcedure(final DataSource dataSource) {
        super(dataSource, PROCEDURE_NAME);
        declareParameter(new SqlParameter(IN_ENTITY_TYPE, Types.VARCHAR));
        declareParameter(new SqlParameter(IN_ENTITY_ID, Types.NUMERIC));
        declareParameter(new SqlParameter(IN_TEMPLATE, Types.VARCHAR));
        declareParameter(new SqlParameter(IN_USER_ID, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_STATUS, Types.VARCHAR));
        declareParameter(new SqlOutParameter(OUT_EMAIL_ID, Types.NUMERIC));
        compile();
    }

    /**
     * Queues the notification.
     *
     * @return the status code the procedure returned
     */
    public String addEmail(final String entityTypeCode,
            final long entityId,
            final String templateCode,
            final String userId) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(IN_ENTITY_TYPE, entityTypeCode);
        inputs.put(IN_ENTITY_ID, Long.valueOf(entityId));
        inputs.put(IN_TEMPLATE, templateCode);
        inputs.put(IN_USER_ID, userId);
        return (String) execute(inputs).get(OUT_STATUS);
    }
}
