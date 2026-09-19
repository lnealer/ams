package org.example.am.shared.utils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Null-safe conversions between what the Oracle driver hands back and what the domain model wants.
 *
 * <p>The row mappers lean on these heavily: {@code ResultSet.getLong} returns {@code 0} for a NULL
 * column, which for a surrogate key is indistinguishable from a real value, so every nullable
 * numeric column is read through {@link #getLong(ResultSet, String)} instead.</p>
 */
public final class ConversionUtils {

    private ConversionUtils() {
        super();
    }

    public static Long getLong(final ResultSet resultSet, final String column) throws SQLException {
        final long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : Long.valueOf(value);
    }

    public static Integer getInteger(final ResultSet resultSet, final String column) throws SQLException {
        final int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : Integer.valueOf(value);
    }

    public static BigDecimal getBigDecimal(final ResultSet resultSet, final String column)
            throws SQLException {
        final BigDecimal value = resultSet.getBigDecimal(column);
        return resultSet.wasNull() ? null : value;
    }

    /**
     * @return a plain {@link Date}, so that domain objects never leak {@code java.sql} types into
     *         the JSP layer where {@code equals} between the two is asymmetric
     */
    public static Date getDate(final ResultSet resultSet, final String column) throws SQLException {
        final Timestamp value = resultSet.getTimestamp(column);
        return value == null ? null : new Date(value.getTime());
    }

    /**
     * Reads an Oracle {@code CHAR(1)} flag column.
     */
    public static boolean getBoolean(final ResultSet resultSet, final String column) throws SQLException {
        final String value = resultSet.getString(column);
        return CommonConstants.YES.equalsIgnoreCase(trimToNull(value));
    }

    public static String toFlag(final boolean value) {
        return value ? CommonConstants.YES : CommonConstants.NO;
    }

    /**
     * Oracle stores {@code CHAR} columns space padded, so every string read from a row mapper is
     * trimmed and empty strings are normalised to {@code null}.
     */
    public static String getString(final ResultSet resultSet, final String column) throws SQLException {
        return trimToNull(resultSet.getString(column));
    }

    public static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }

    /**
     * @return the supplied date with the time of day removed, which is how the calendar tables
     *         store their day keys
     */
    public static Date truncateToDay(final Date value) {
        if (value == null) {
            return null;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(value);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date addDays(final Date value, final int days) {
        if (value == null) {
            return null;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(value);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    public static boolean isSameDay(final Date left, final Date right) {
        if (left == null || right == null) {
            return false;
        }
        return truncateToDay(left).equals(truncateToDay(right));
    }
}
