package org.example.am.shared.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.example.am.shared.dao.SubscriberPcDAO;
import org.example.am.shared.domain.SubscriberPc;
import org.example.am.shared.domain.SubscriberPcType;
import org.example.am.shared.helper.AbstractBaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class SubscriberPcDAOImplTest extends AbstractBaseTest {

    private static final long ORDER_ID = 6002L;

    @Autowired
    @Qualifier("subscriberPcSharedDAO")
    private SubscriberPcDAO subscriberPcDAO;

    private static SubscriberPc pc(final String hostName, final SubscriberPcType type) {
        final SubscriberPc pc = new SubscriberPc();
        pc.setHostName(hostName);
        pc.setSubscriberPcType(type);
        return pc;
    }

    @Test
    public void aSubscriberPcRoundTrips() {
        final SubscriberPc pc = pc("till-01", SubscriberPcType.POS_TERMINAL);
        pc.setOperatingSystem("Windows 11 IoT");
        pc.setMacAddress("00:1B:44:11:3A:B7");
        pc.setIpAddress("192.168.10.31");
        pc.setStaticAddress(true);
        pc.setUserCount(Integer.valueOf(3));
        pc.setNotes("Front counter");

        final long id = subscriberPcDAO.insertSubscriberPc(pc, ORDER_ID, "junit");
        assertTrue(id > 0);

        final List<SubscriberPc> loaded = subscriberPcDAO.getSubscriberPcs(ORDER_ID);
        assertEquals(1, loaded.size());
        final SubscriberPc read = loaded.get(0);
        assertEquals("till-01", read.getHostName());
        assertEquals(SubscriberPcType.POS_TERMINAL, read.getSubscriberPcType());
        assertEquals("Windows 11 IoT", read.getOperatingSystem());
        assertEquals("00:1B:44:11:3A:B7", read.getMacAddress());
        assertEquals("192.168.10.31", read.getIpAddress());
        assertTrue(read.isStaticAddress());
        assertEquals(Integer.valueOf(3), read.getUserCount());
        assertEquals("Front counter", read.getNotes());
    }

    /**
     * The capture screen posts a fixed grid, so most rows arrive untouched. Writing them would put
     * an empty line on the engineer's sheet for every row the user did not fill in.
     */
    @Test
    public void blankRowsAreNotWritten() {
        final List<SubscriberPc> rows = new ArrayList<SubscriberPc>();
        rows.add(pc("laptop-01", SubscriberPcType.LAPTOP));
        rows.add(new SubscriberPc());
        rows.add(new SubscriberPc());
        rows.add(pc("printer-01", SubscriberPcType.PRINTER));

        assertEquals(2, subscriberPcDAO.replaceSubscriberPcs(rows, ORDER_ID, "junit"));
        assertEquals(2, subscriberPcDAO.getSubscriberPcs(ORDER_ID).size());
    }

    /**
     * Replace is delete-then-insert: the grid has no stable identity per row, so a second save
     * must not leave the first save's rows behind alongside the new ones.
     */
    @Test
    public void replacingClearsWhatWasThereBefore() {
        final List<SubscriberPc> first = new ArrayList<SubscriberPc>();
        first.add(pc("old-01", SubscriberPcType.DESKTOP));
        first.add(pc("old-02", SubscriberPcType.DESKTOP));
        subscriberPcDAO.replaceSubscriberPcs(first, ORDER_ID, "junit");

        final List<SubscriberPc> second = new ArrayList<SubscriberPc>();
        second.add(pc("new-01", SubscriberPcType.KIOSK));
        subscriberPcDAO.replaceSubscriberPcs(second, ORDER_ID, "junit");

        final List<SubscriberPc> loaded = subscriberPcDAO.getSubscriberPcs(ORDER_ID);
        assertEquals(1, loaded.size());
        assertEquals("new-01", loaded.get(0).getHostName());
    }

    /**
     * A field tabbed through but left blank has to land as NULL, not as an empty string, or
     * SubscriberPc.isBlank would disagree with what is in the database.
     */
    @Test
    public void whitespaceOnlyFieldsAreStoredAsNull() {
        final SubscriberPc pc = pc("kiosk-01", SubscriberPcType.KIOSK);
        pc.setOperatingSystem("   ");
        pc.setNotes("");
        subscriberPcDAO.insertSubscriberPc(pc, ORDER_ID, "junit");

        final SubscriberPc read = subscriberPcDAO.getSubscriberPcs(ORDER_ID).get(0);
        assertNull(read.getOperatingSystem());
        assertNull(read.getNotes());
    }

    @Test
    public void pcsAreScopedToTheirOrder() {
        subscriberPcDAO.insertSubscriberPc(pc("till-01", SubscriberPcType.POS_TERMINAL),
                ORDER_ID, "junit");
        assertTrue(subscriberPcDAO.getSubscriberPcs(6003L).isEmpty());
    }

    @Test
    public void anUnknownTypeCodeReadsBackAsNullRatherThanThrowing() {
        final SubscriberPc pc = new SubscriberPc();
        pc.setHostName("mystery-01");
        subscriberPcDAO.insertSubscriberPc(pc, ORDER_ID, "junit");

        final SubscriberPc read = subscriberPcDAO.getSubscriberPcs(ORDER_ID).get(0);
        assertNotNull(read);
        assertNull(read.getSubscriberPcType());
    }
}
