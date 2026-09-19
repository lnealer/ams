package org.example.am.shared.helper;

import java.util.Date;

import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.ContactType;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.domain.Order;
import org.example.am.shared.domain.OrderStatusType;
import org.example.am.shared.utils.ConversionUtils;

/**
 * Order-shaped helpers used by both the internal and external ordering flows.
 */
public final class OrderHelper {

    private OrderHelper() {
        super();
    }

    /**
     * Pre-populates the three contacts from the customer's defaults, leaving anything the user has
     * already keyed alone.
     */
    public static void applyDefaultContacts(final Order order, final Customer customer) {
        if (order == null || customer == null) {
            return;
        }
        if (order.getOrderingContact() == null) {
            order.setOrderingContact(customer.getContactByType(ContactType.ORDERING));
        }
        if (order.getShippingContact() == null) {
            order.setShippingContact(customer.getContactByType(ContactType.SHIPPING));
        }
        if (order.getInstallationContact() == null) {
            order.setInstallationContact(customer.getContactByType(ContactType.INSTALLATION));
        }
    }

    /**
     * @return the earliest installation date this order may be scheduled for, counted forward from
     *         the submission date in business days
     */
    public static Date getEarliestInstallationDate(final Order order) {
        if (order == null) {
            return null;
        }
        final Date from = order.getSubmittedDate() == null
                ? order.getCurrentTime() : order.getSubmittedDate();
        return addBusinessDays(from, order.getInstallLeadTimeDays());
    }

    /**
     * Counts forward skipping weekends. Public holidays are excluded separately by the calendar
     * service, which is the only component with the holiday table.
     */
    public static Date addBusinessDays(final Date from, final int businessDays) {
        if (from == null) {
            return null;
        }
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(ConversionUtils.truncateToDay(from));
        int remaining = businessDays;
        while (remaining > 0) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
            final int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
            if (dayOfWeek != java.util.Calendar.SATURDAY && dayOfWeek != java.util.Calendar.SUNDAY) {
                remaining--;
            }
        }
        return calendar.getTime();
    }

    /**
     * @return {@code true} when nothing further is expected to happen to this order
     */
    public static boolean isTerminal(final Order order) {
        if (order == null || order.getOrderStatusType() == null) {
            return false;
        }
        return OrderStatusType.COMPLETED.equals(order.getOrderStatusType())
                || OrderStatusType.CANCELLED.equals(order.getOrderStatusType());
    }

    public static String getContactEmailAddress(final Contact contact) {
        return contact == null ? null : contact.getEmailAddress();
    }
}
