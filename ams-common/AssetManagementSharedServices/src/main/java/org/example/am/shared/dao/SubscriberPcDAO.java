package org.example.am.shared.dao;

import java.util.List;

import org.example.am.shared.domain.SubscriberPc;

/** Reads and writes {@code AMS_SUBSCRIBER_PCS}, the machines captured with an order. */
public interface SubscriberPcDAO {

    List<SubscriberPc> getSubscriberPcs(long orderId);

    /** @return the generated id */
    long insertSubscriberPc(SubscriberPc subscriberPc, long orderId, String userId);

    /**
     * Replaces the whole list for an order.
     *
     * <p>Delete-then-insert rather than a per-row merge: the capture screen submits the grid as a
     * whole and rows have no identity of their own in it, so there is nothing stable to match an
     * existing row against.</p>
     *
     * @return how many rows were written
     */
    int replaceSubscriberPcs(List<SubscriberPc> subscriberPcs, long orderId, String userId);

    int deleteSubscriberPcs(long orderId);
}
