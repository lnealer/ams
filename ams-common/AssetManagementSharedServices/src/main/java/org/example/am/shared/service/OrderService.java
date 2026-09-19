package org.example.am.shared.service;

import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.Order;

/** Order reads, submission and cancellation. */
public interface OrderService {

    Order getOrder(long customerId, long orderId);

    List<Order> getOpenOrders(long customerId);

    /**
     * Persists a new order and queues its confirmation email inside the same transaction.
     *
     * @return the generated order id
     */
    long submitOrder(Order order, String userId);

    /**
     * @return {@code true} when the order was cancelled; {@code false} when it had already closed
     */
    boolean cancelOrder(long customerId, long orderId, String reason, String userId);

    /**
     * @return {@code true} when cancelling this order now would incur the cancellation charge
     */
    boolean isCancellationPenaltyIncurred(long customerId, long orderId, Date asOf);

    /** @return the earliest installation date this order may be scheduled for */
    Date getEarliestInstallationDate(Order order);

    /**
     * Reads an order together with everything captured when it was placed: the three contacts, the
     * shipping address, the maintenance window, the external configuration, the subscriber PCs and
     * the despatch window.
     *
     * <p>Separate from {@link #getOrder(long, long)} because it is eight queries rather than one,
     * and the list screens want the cheap version.</p>
     *
     * @return the fully populated order, or {@code null} when there is no such order
     */
    Order getOrderDetail(long customerId, long orderId);
}
