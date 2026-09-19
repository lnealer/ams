package org.example.am.shared.dao.procs;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Queues a templated notification against a business entity.
 *
 * <p>A procedure rather than an insert because it also resolves the recipient list from the
 * entity's contacts and suppresses duplicates raised within the de-duplication window.</p>
 *
 * <p>The OUT parameters are declared status-then-id, matching the internal module's class of the
 * same name and every other procedure in this set. Spring binds positionally, so the two classes
 * calling this one procedure must agree on the order or one of them binds a NUMBER into a
 * VARCHAR2 formal.</p>
 */
public class AddEntityEmailProcedure extends StoredProcedure {

    private static final String PROCEDURE_NAME = "AMS_EMAIL_PG.add_entity_email";

    private static final String IN_ENTITY_TYPE = "p_entity_type_cd";
    private static final String IN_ENTITY_ID = "p_entity_id";
    private static final String IN_TEMPLATE = "p_template_cd";
    private static final String IN_USER_ID = "p_user_id";
    private static final String OUT_STATUS = "p_status_cd";
    private static final String OUT_EMAIL_ID = "p_email_id";

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
     * @return the queued email id, or {@code null} when the procedure suppressed a duplicate
     */
    public Long addEmail(final String entityTypeCode, final long entityId, final String templateCode,
            final String userId) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(IN_ENTITY_TYPE, entityTypeCode);
        inputs.put(IN_ENTITY_ID, Long.valueOf(entityId));
        inputs.put(IN_TEMPLATE, templateCode);
        inputs.put(IN_USER_ID, userId);
        final Map<String, Object> results = execute(inputs);
        final Object emailId = results.get(OUT_EMAIL_ID);
        return emailId == null ? null : Long.valueOf(((Number) emailId).longValue());
    }
}
