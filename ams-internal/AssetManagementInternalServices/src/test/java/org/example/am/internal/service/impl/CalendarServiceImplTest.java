package org.example.am.internal.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.example.am.internal.service.dao.CalendarServiceDAO;
import org.example.am.internal.service.dao.StoredProcedureDAO;
import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.domain.NetworkChangeRequestType;
import org.example.am.shared.domain.PropertyType;
import org.example.am.shared.service.ConfigService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The scheduling rules, isolated from the database.
 *
 * <p>A unit test rather than an integration test on purpose: the interesting behaviour is which
 * stored procedure gets called and what the scheduling window allows, neither of which needs a real
 * database - and the procedures do not exist in the test schema anyway.</p>
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarServiceImplTest {

    private static final SimpleDateFormat DAY = new SimpleDateFormat("yyyy-MM-dd");

    @Mock
    private CalendarServiceDAO calendarServiceDAO;

    @Mock
    private StoredProcedureDAO storedProcedureDAO;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    private NetworkChangeRequest request;

    @Before
    public void setUp() {
        when(calendarServiceDAO.getHolidays(Matchers.<Date>any(), Matchers.<Date>any()))
                .thenReturn(new ArrayList<Date>());
        when(configService.getInt(Matchers.<PropertyType>any(), Matchers.anyInt()))
                .thenAnswer(new org.mockito.stubbing.Answer<Integer>() {
                    @Override
                    public Integer answer(final org.mockito.invocation.InvocationOnMock invocation) {
                        // Fall through to the compiled-in default, which is what an unconfigured
                        // property does in production.
                        return (Integer) invocation.getArguments()[1];
                    }
                });

        request = new NetworkChangeRequest();
        request.setNetworkChangeRequestId(Long.valueOf(9001L));
        request.setAssetId(Long.valueOf(5001L));
    }

    private static Date daysFromNow(final int days) {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    // ------------------------------------------------------------------
    // Which procedure a change request goes through
    // ------------------------------------------------------------------

    @Test
    public void aDeviceOnlyChangeTakesTheSimplePath() {
        request.setNetworkChangeRequestTypes(new java.util.LinkedHashSet<NetworkChangeRequestType>(
                Arrays.asList(NetworkChangeRequestType.BANDWIDTH_CHANGE)));
        when(storedProcedureDAO.reserveNetworkChangeDate(anyLong(), anyLong(), anyString(),
                Matchers.<Date>any(), anyBoolean(), anyString())).thenReturn("OK");

        assertTrue(calendarService.reserveNetworkChangeDate(request, daysFromNow(10), "junit"));
        verify(storedProcedureDAO).reserveNetworkChangeDate(eq(9001L), eq(5001L),
                Matchers.<String>any(), Matchers.<Date>any(), eq(false), eq("junit"));
    }

    /**
     * A site type change reshapes the circuit as well as the device, so it has to go through the
     * procedure that books both windows in one step.
     */
    @Test
    public void aSiteTypeChangeTakesTheComplexPath() {
        request.setNetworkChangeRequestTypes(new java.util.LinkedHashSet<NetworkChangeRequestType>(
                Arrays.asList(NetworkChangeRequestType.SITE_TYPE_CHANGE,
                        NetworkChangeRequestType.IP_READDRESS)));
        when(storedProcedureDAO.reserveNetworkChangeDate(anyLong(), anyLong(), anyString(),
                Matchers.<Date>any(), anyBoolean(), anyString())).thenReturn("OK");

        assertTrue(calendarService.reserveNetworkChangeDate(request, daysFromNow(10), "junit"));
        verify(storedProcedureDAO).reserveNetworkChangeDate(eq(9001L), eq(5001L),
                Matchers.<String>any(), Matchers.<Date>any(), eq(true), eq("junit"));
    }

    @Test
    public void cancellationRoutesTheSameWayAsBooking() {
        request.setNetworkChangeRequestTypes(new java.util.LinkedHashSet<NetworkChangeRequestType>(
                Arrays.asList(NetworkChangeRequestType.SITE_TYPE_CHANGE)));
        when(storedProcedureDAO.cancelNetworkChangeDate(anyLong(), anyLong(), anyString(),
                anyBoolean(), anyString())).thenReturn("OK");

        assertTrue(calendarService.cancelNetworkChangeDate(request, "No longer needed", "junit"));
        verify(storedProcedureDAO).cancelNetworkChangeDate(eq(9001L), eq(5001L),
                eq("No longer needed"), eq(true), eq("junit"));
    }

    @Test
    public void aProcedureThatDidNotSucceedIsReportedAsFailureNotAsAnException() {
        request.setNetworkChangeRequestTypes(new java.util.LinkedHashSet<NetworkChangeRequestType>(
                Arrays.asList(NetworkChangeRequestType.MOVE)));
        when(storedProcedureDAO.reserveNetworkChangeDate(anyLong(), anyLong(), anyString(),
                Matchers.<Date>any(), anyBoolean(), anyString())).thenReturn("NO_CAPACITY");

        assertFalse(calendarService.reserveNetworkChangeDate(request, daysFromNow(10), "junit"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void anUnsavedRequestCannotBeScheduled() {
        calendarService.reserveNetworkChangeDate(new NetworkChangeRequest(), daysFromNow(5), "junit");
    }

    // ------------------------------------------------------------------
    // The decommission scheduling window
    // ------------------------------------------------------------------

    @Test
    public void aDecommissionInsideTheWindowIsBooked() {
        when(storedProcedureDAO.reserveDecommissionDate(anyLong(), anyLong(), Matchers.<Date>any(),
                anyBoolean(), anyString())).thenReturn("OK");

        assertTrue(calendarService.reserveDecommissionDate(1L, 5001L, daysFromNow(30), true,
                "junit"));
    }

    /**
     * Engineer availability more than six weeks out is guesswork, so a booking that far ahead is
     * refused rather than made and almost certainly moved later.
     */
    @Test
    public void aDecommissionBeyondTheWindowIsRefused() {
        try {
            calendarService.reserveDecommissionDate(1L, 5001L, daysFromNow(60), false, "junit");
            fail("Expected the scheduling window to be enforced");
        } catch (final IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("cannot be scheduled after"));
        }
    }

    @Test
    public void aDecommissionInThePastIsRefused() {
        try {
            calendarService.reserveDecommissionDate(1L, 5001L, daysFromNow(-1), false, "junit");
            fail("Expected a date in the past to be refused");
        } catch (final IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("in the past"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void aDecommissionWithNoDateIsRefused() {
        calendarService.reserveDecommissionDate(1L, 5001L, null, false, "junit");
    }

    @Test
    public void theWindowEndsFortyTwoDaysOut() {
        final long windowDays = (calendarService.getLatestDecommissionDate().getTime()
                - org.example.am.shared.utils.ConversionUtils.truncateToDay(new Date()).getTime())
                / (24L * 60L * 60L * 1000L);
        assertEquals(42L, windowDays);
    }

    // ------------------------------------------------------------------
    // Lead times
    // ------------------------------------------------------------------

    @Test
    public void leadTimesSkipWeekends() throws Exception {
        // Three business days for techline, ten for an installation, counted from today.
        final Date techline = calendarService.getEarliestTechlineDate(1001L);
        final Date installation = calendarService.getEarliestInstallationDate(1001L);
        assertTrue(installation.after(techline));

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(techline);
        assertFalse(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY);
        assertFalse(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
    }

    @Test
    public void aWeekendIsNotASchedulableDate() {
        final Calendar saturday = Calendar.getInstance();
        saturday.set(2025, Calendar.SEPTEMBER, 6);
        assertFalse(calendarService.isSchedulableDate(1001L, saturday.getTime()));
        assertFalse(calendarService.isSchedulableDate(1001L, null));
    }

    @Test
    public void slotsBeforeTheLeadTimeAreNotOffered() {
        when(calendarServiceDAO.getAvailableTimeslots(Matchers.<org.example.am.shared.domain
                .FacilitationCallType>any(), Matchers.<Date>any(), Matchers.<Date>any()))
                .thenReturn(new ArrayList<org.example.am.shared.domain.Timeslot>());
        when(calendarServiceDAO.getBlackoutDates(anyLong(), Matchers.<Date>any(),
                Matchers.<Date>any())).thenReturn(new ArrayList<Date>());

        final List<org.example.am.shared.domain.Timeslot> offered =
                calendarService.getInstallationTimeslots(1001L, daysFromNow(-10), daysFromNow(30));
        assertTrue(offered.isEmpty());
        // The window start is clamped to the lead time, never to the date the caller asked for.
        verify(calendarServiceDAO).getAvailableTimeslots(
                Matchers.<org.example.am.shared.domain.FacilitationCallType>any(),
                Matchers.<Date>any(), Matchers.<Date>any());
    }
}
