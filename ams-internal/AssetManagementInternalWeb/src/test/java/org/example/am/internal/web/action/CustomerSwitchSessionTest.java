package org.example.am.internal.web.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.example.am.internal.utils.InternalConstants;
import org.example.am.shared.domain.Customer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Switching the selected customer has to discard work in progress that belonged to the previous
 * one.
 *
 * <p>The bug this pins down was not a refusal but a silent mis-attribution. An in-progress order
 * carries the customer id it was created with; nothing updated it when the operator switched
 * customer, so the header showed the newly selected customer while the model still pointed at the
 * old one. Against a customer that cannot order, that surfaced as "Ordering is not enabled for this
 * customer" on a customer that plainly could. Against two customers that both could order, it
 * surfaced as nothing at all - the order was simply placed for the wrong one.</p>
 */
public class CustomerSwitchSessionTest {

    /** Concrete subclass: the methods under test are protected on the abstract base. */
    private static final class TestAction extends BaseAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void setCurrentCustomer(final Customer customer) {
            super.setCurrentCustomer(customer);
        }

        @Override
        public Long getCurrentCustomerId() {
            return super.getCurrentCustomerId();
        }
    }

    private TestAction action;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        action = new TestAction();
        action.setServletRequest(request);
    }

    private static Customer customer(final long id) {
        final Customer customer = new Customer();
        customer.setCustomerId(Long.valueOf(id));
        return customer;
    }

    private Object orderModel() {
        return request.getSession(true).getAttribute(InternalConstants.SESSION_ORDER_MODEL);
    }

    private void startAnOrderFor(final long customerId) {
        action.setCurrentCustomer(customer(customerId));
        final OrderModelHolder held = new OrderModelHolder(customerId);
        request.getSession(true).setAttribute(InternalConstants.SESSION_ORDER_MODEL, held);
    }

    /** Stands in for the real OrderModel; only the customer id it pins matters here. */
    private static final class OrderModelHolder {
        private final long customerId;

        OrderModelHolder(final long customerId) {
            this.customerId = customerId;
        }

        long getCustomerId() {
            return customerId;
        }
    }

    @Test
    public void switchingCustomerDiscardsTheOrderInProgress() {
        startAnOrderFor(1002L);
        assertNotNull("precondition: an order is in progress", orderModel());

        action.setCurrentCustomer(customer(1011L));

        assertNull("the order started for 1002 must not survive the switch to 1011", orderModel());
        assertEquals(Long.valueOf(1011L), action.getCurrentCustomerId());
    }

    @Test
    public void switchingCustomerDiscardsTheChangeRequestInProgress() {
        action.setCurrentCustomer(customer(1002L));
        request.getSession(true).setAttribute(InternalConstants.SESSION_NCR_MODEL, "in progress");

        action.setCurrentCustomer(customer(1011L));

        assertNull(request.getSession(true).getAttribute(InternalConstants.SESSION_NCR_MODEL));
    }

    @Test
    public void reselectingTheSameCustomerKeepsTheOrderInProgress() {
        startAnOrderFor(1011L);
        final Object before = orderModel();

        // Re-selecting the customer already being worked on is a no-op the operator does by
        // accident; throwing their half-filled order away for it would be its own bug.
        action.setCurrentCustomer(customer(1011L));

        assertSame("the same customer must not discard the order", before, orderModel());
    }

    @Test
    public void clearingTheCustomerDiscardsTheOrderInProgress() {
        startAnOrderFor(1011L);

        action.setCurrentCustomer(null);

        assertNull(orderModel());
        assertNull(action.getCurrentCustomerId());
    }

    @Test
    public void firstSelectionKeepsAnythingAlreadyThere() {
        // No customer selected yet, so there is no previous owner to protect against: whatever is
        // in the session was created under this same selection and must survive.
        request.getSession(true).setAttribute(InternalConstants.SESSION_ORDER_MODEL,
                new OrderModelHolder(0L));

        action.setCurrentCustomer(customer(1011L));

        assertNotNull(orderModel());
    }
}
