package org.example.am.shared.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import org.example.am.shared.dao.AddressDAO;
import org.example.am.shared.dao.AssetConfigDAO;
import org.example.am.shared.dao.ContactDAO;
import org.example.am.shared.dao.MaintenanceWindowDAO;
import org.example.am.shared.dao.OrderDAO;
import org.example.am.shared.dao.RequestDAO;
import org.example.am.shared.dao.StoredProcedureDAO;
import org.example.am.shared.dao.SubscriberPcDAO;
import org.example.am.shared.domain.Address;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.ContactType;
import org.example.am.shared.domain.DayType;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.EventType;
import org.example.am.shared.domain.HourType;
import org.example.am.shared.domain.MaintenanceWindow;
import org.example.am.shared.domain.Order;
import org.example.am.shared.domain.OrderStatusType;
import org.example.am.shared.domain.OrderType;
import org.example.am.shared.domain.PropertyType;
import org.example.am.shared.domain.SubscriberPc;
import org.example.am.shared.domain.SubscriberPcType;
import org.example.am.shared.domain.Timeslot;
import org.example.am.shared.service.CalendarService;
import org.example.am.shared.service.ConfigService;
import org.example.am.shared.service.ShippingCalendarService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test rather than an integration test: the point is the branching around the penalty window
 * and the notification side effects, not the SQL.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceImplTest {

    private static final long CUSTOMER_ID = 1001L;
    private static final long ORDER_ID = 6002L;

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private RequestDAO requestDAO;

    @Mock
    private StoredProcedureDAO storedProcedureDAO;

    @Mock
    private ConfigService configService;

    @Mock
    private CalendarService calendarService;

    @Mock
    private AddressDAO addressDAO;

    @Mock
    private ContactDAO contactDAO;

    @Mock
    private MaintenanceWindowDAO maintenanceWindowDAO;

    @Mock
    private AssetConfigDAO assetConfigDAO;

    @Mock
    private SubscriberPcDAO subscriberPcDAO;

    @Mock
    private ShippingCalendarService shippingCalendarService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;

    @Before
    public void setUp() {
        order = new Order();
        order.setOrderId(Long.valueOf(ORDER_ID));
        order.setOrderNumber("ORD-6002");
        order.setOrderType(OrderType.NEW_INSTALL);
        order.setOrderStatusType(OrderStatusType.SUBMITTED);
        order.setCustomerId(Long.valueOf(CUSTOMER_ID));

        when(configService.getInt(
                eq(PropertyType.MIN_HOURS_BEFORE_INSTALLATION_TO_CANCEL_ORDER_WITHOUT_PENALTY),
                Matchers.anyInt())).thenReturn(Integer.valueOf(48));
        when(orderDAO.getOrder(CUSTOMER_ID, ORDER_ID)).thenReturn(order);
    }

    private static Date hoursFromNow(final int hours) {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    @Test
    public void submitStampsTheStatusAndQueuesTheConfirmation() {
        when(orderDAO.insertOrder(order, "junit")).thenReturn(Long.valueOf(7777L));

        final long orderId = orderService.submitOrder(order, "junit");

        assertEquals(7777L, orderId);
        assertEquals(OrderStatusType.SUBMITTED, order.getOrderStatusType());
        assertTrue(order.getSubmittedDate() != null);
        verify(requestDAO).recordEvent(eq(EventType.ORDER_SUBMITTED), eq(EmailEntityType.ORDER),
                eq(7777L), anyString(), eq("junit"));
        verify(storedProcedureDAO).addEntityEmail(eq(EmailEntityType.ORDER.getCode()), eq(7777L),
                anyString(), eq("junit"));
    }

    @Test
    public void cancellingWellAheadOfInstallationIncursNoPenalty() {
        order.setRequestedInstallationDate(hoursFromNow(96));
        when(orderDAO.cancelOrder(anyLong(), anyString(), anyBoolean(),
                Matchers.<Date>any(), anyString())).thenReturn(Integer.valueOf(1));

        assertTrue(orderService.cancelOrder(CUSTOMER_ID, ORDER_ID, "Not needed", "junit"));
        verify(orderDAO).cancelOrder(eq(ORDER_ID), eq("Not needed"), eq(false),
                Matchers.<Date>any(), eq("junit"));
    }

    @Test
    public void cancellingInsideTheWindowIncursThePenalty() {
        order.setRequestedInstallationDate(hoursFromNow(12));
        when(orderDAO.cancelOrder(anyLong(), anyString(), anyBoolean(),
                Matchers.<Date>any(), anyString())).thenReturn(Integer.valueOf(1));

        assertTrue(orderService.cancelOrder(CUSTOMER_ID, ORDER_ID, "Site not ready", "junit"));
        verify(orderDAO).cancelOrder(eq(ORDER_ID), eq("Site not ready"), eq(true),
                Matchers.<Date>any(), eq("junit"));
    }

    @Test
    public void anOrderWithNoInstallationDateNeverIncursThePenalty() {
        assertFalse(orderService.isCancellationPenaltyIncurred(CUSTOMER_ID, ORDER_ID, new Date()));
    }

    /**
     * If the guarded update touched no rows, the order had already closed, so no event and no
     * notification should be raised.
     */
    @Test
    public void losingTheRaceToCancelRaisesNoSideEffects() {
        when(orderDAO.cancelOrder(anyLong(), anyString(), anyBoolean(),
                Matchers.<Date>any(), anyString())).thenReturn(Integer.valueOf(0));

        assertFalse(orderService.cancelOrder(CUSTOMER_ID, ORDER_ID, "Too late", "junit"));
        verify(requestDAO, never()).recordEvent(Matchers.<EventType>any(),
                Matchers.<EmailEntityType>any(), anyLong(), anyString(), anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cancellingAnUnknownOrderIsAProgrammingError() {
        orderService.cancelOrder(CUSTOMER_ID, 999999L, "Nope", "junit");
    }

    /**
     * A fully keyed order: everything the six ordering steps collect has to reach the database in
     * one transaction, and the order row has to end up pointing at all of it.
     */
    @Test
    public void submitPersistsEverythingTheOrderingFlowCollected() {
        when(orderDAO.insertOrder(order, "junit")).thenReturn(Long.valueOf(7777L));
        populateFullOrder();

        orderService.submitOrder(order, "junit");

        verify(addressDAO).insertAddress(Matchers.<Address>any(), eq("junit"));
        // Three contacts, three rows: the same person in two roles is still two records.
        verify(contactDAO, org.mockito.Mockito.times(3))
                .insertContact(Matchers.<Contact>any(), eq("junit"));
        verify(maintenanceWindowDAO).insertMaintenanceWindowForOrder(
                Matchers.<MaintenanceWindow>any(), eq(7777L), eq("junit"));
        verify(assetConfigDAO).insertOrderConfiguration(Matchers.<AssetConfiguration>any(),
                eq(7777L), eq("junit"));
        verify(subscriberPcDAO).replaceSubscriberPcs(Matchers.<java.util.List<SubscriberPc>>any(),
                eq(7777L), eq("junit"));
        verify(orderDAO).linkOrderArtifacts(eq(7777L), Matchers.<Long>any(), Matchers.<Long>any(),
                Matchers.<Long>any(), eq("junit"));
    }

    /**
     * A contact that already has an id was picked from the customer's file rather than keyed, so
     * it must not be written again - doing so would fork the record on every repeat order.
     */
    @Test
    public void anExistingContactIsNotWrittenAgain() {
        when(orderDAO.insertOrder(order, "junit")).thenReturn(Long.valueOf(7777L));
        populateFullOrder();
        order.getOrderingContact().setContactId(Long.valueOf(4242L));

        orderService.submitOrder(order, "junit");

        verify(contactDAO, org.mockito.Mockito.times(2))
                .insertContact(Matchers.<Contact>any(), eq("junit"));
    }

    @Test
    public void theDespatchWindowIsReservedAndLinked() {
        when(orderDAO.insertOrder(order, "junit")).thenReturn(Long.valueOf(7777L));
        when(storedProcedureDAO.reserveTimeslot(eq(9705L), eq(7777L), eq("SHIP"),
                Matchers.<Date>any(), eq("junit"))).thenReturn("OK");
        populateFullOrder();

        orderService.submitOrder(order, "junit");

        verify(orderDAO).linkOrderArtifacts(eq(7777L), Matchers.<Long>any(), Matchers.<Long>any(),
                eq(Long.valueOf(9705L)), eq("junit"));
    }

    /**
     * The window can fill between choosing it and pressing the button. The order must still be
     * placed - there is no review step to go back to - but nothing may claim a window the
     * reservation ledger does not back.
     */
    @Test
    public void losingTheDespatchWindowStillPlacesTheOrderWithoutOne() {
        when(orderDAO.insertOrder(order, "junit")).thenReturn(Long.valueOf(7777L));
        when(storedProcedureDAO.reserveTimeslot(anyLong(), anyLong(), anyString(),
                Matchers.<Date>any(), anyString())).thenReturn("NO_CAPACITY");
        populateFullOrder();

        final long orderId = orderService.submitOrder(order, "junit");

        assertEquals(7777L, orderId);
        assertEquals(null, order.getShippingWindowTimeslotId());
        verify(orderDAO).linkOrderArtifacts(eq(7777L), Matchers.<Long>any(), Matchers.<Long>any(),
                eq((Long) null), eq("junit"));
    }

    /** An order with no window chosen must not call the reservation procedure at all. */
    @Test
    public void noDespatchWindowMeansNoReservation() {
        when(orderDAO.insertOrder(order, "junit")).thenReturn(Long.valueOf(7777L));
        populateFullOrder();
        order.setShippingWindow(null);
        order.setShippingWindowTimeslotId(null);

        orderService.submitOrder(order, "junit");

        verify(storedProcedureDAO, never()).reserveTimeslot(anyLong(), anyLong(), eq("SHIP"),
                Matchers.<Date>any(), anyString());
    }

    /** Fills the order with what the six ordering steps would have collected. */
    private void populateFullOrder() {
        final Address address = new Address();
        address.setAddressLine1("100 Main Street");
        address.setCity("Springfield");
        order.setShippingAddress(address);

        order.setOrderingContact(contact("Ada", ContactType.ORDERING));
        order.setShippingContact(contact("Grace", ContactType.SHIPPING));
        order.setInstallationContact(contact("Alan", ContactType.INSTALLATION));

        order.setDeviceNickname("Front desk router");

        final MaintenanceWindow window = new MaintenanceWindow();
        window.setDayType(DayType.SUNDAY);
        window.setStartHour(HourType.lookup("0100"));
        window.setEndHour(HourType.lookup("0500"));
        window.setTimeZone("America/Chicago");
        order.setMaintenanceWindow(window);

        final AssetConfiguration configuration = new AssetConfiguration();
        configuration.setLanIpAddress("192.168.10.1");
        configuration.setLanSubnetMask("255.255.255.0");
        order.setAssetConfiguration(configuration);

        final SubscriberPc pc = new SubscriberPc();
        pc.setHostName("till-01");
        pc.setSubscriberPcType(SubscriberPcType.POS_TERMINAL);
        order.setSubscriberPcs(java.util.Collections.singletonList(pc));

        final Timeslot slot = new Timeslot();
        slot.setTimeslotId(Long.valueOf(9705L));
        slot.setStartTime(new Date());
        order.setShippingWindow(slot);
    }

    private static Contact contact(final String firstName, final ContactType type) {
        final Contact contact = new Contact();
        contact.setFirstName(firstName);
        contact.setLastName("Example");
        contact.setContactType(type);
        return contact;
    }

    @Test
    public void earliestInstallationDateUsesTheOrderTypeLeadTime() {
        order.setOrderType(OrderType.MIGRATION);
        order.setSubmittedDate(new Date());
        orderService.getEarliestInstallationDate(order);
        // 15 business days for a migration, per Order.getInstallLeadTimeDays().
        verify(calendarService).addBusinessDays(Matchers.<Date>any(), eq(15));
    }
}
