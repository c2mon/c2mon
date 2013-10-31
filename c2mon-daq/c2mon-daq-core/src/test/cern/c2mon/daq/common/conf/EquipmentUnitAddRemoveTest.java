package cern.c2mon.daq.common.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.common.messaging.impl.DummyJmsSender;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.EquipmentUnitAdd;
import cern.tim.shared.daq.config.EquipmentUnitRemove;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:resources/daq-core-service.xml" })
public class EquipmentUnitAddRemoveTest {

    @Autowired
    DriverKernel kernel;
   

    @Autowired
    @Qualifier("dummyJmsSender")
    DummyJmsSender sender;

    @Before
    public void setUp() throws Exception {

    }

    @DirtiesContext
    @Test
    public void testonEquipmentUnitAdd1() throws Exception {

        StringBuilder str = new StringBuilder();
        str.append("<EquipmentUnit id=\"2\" name=\"E_TEST2\">");
        str.append("<handler-class-name>cern.c2mon.driver.common.conf.DummyMessageHandler</handler-class-name>");
        str.append("<commfault-tag-id>47015</commfault-tag-id>");
        str.append("<commfault-tag-value>false</commfault-tag-value>");
        str.append("<alive-tag-id>47322</alive-tag-id>");
        str.append("<alive-interval>60000</alive-interval>");
        str.append("<address/>");
        str.append("<SubEquipmentUnits/>");
        str.append("<DataTags/>");
        str.append("<CommandTags/>");
        str.append("</EquipmentUnit>");

        EquipmentUnitAdd equipmentUnitAddEvent = new EquipmentUnitAdd(1L, 2L, str.toString());

        // before event is received
        assertEquals(1, kernel.getEquipmentMessageHandlersTable().size());

        ChangeReport report = kernel.onEquipmentUnitAdd(equipmentUnitAddEvent);
        assertNotNull(report);
        assertTrue(report.isSuccess());

        // after event is received
        assertEquals(2, kernel.getEquipmentMessageHandlersTable().size());

    }

    @DirtiesContext
    @Test
    public void testonEquipmentUnitAdd2() {

        StringBuilder str = new StringBuilder();
        str.append("<EquipmentUnit id=\"1\" name=\"E_TEST2\">");
        str.append("<handler-class-name>cern.c2mon.driver.common.conf.DummyMessageHandler</handler-class-name>");
        str.append("<commfault-tag-id>47015</commfault-tag-id>");
        str.append("<commfault-tag-value>false</commfault-tag-value>");
        str.append("<alive-tag-id>47322</alive-tag-id>");
        str.append("<alive-interval>60000</alive-interval>");
        str.append("<address/>");
        str.append("<SubEquipmentUnits/>");
        str.append("<DataTags/>");
        str.append("<CommandTags/>");
        str.append("</EquipmentUnit>");

        EquipmentUnitAdd equipmentUnitAddEvent = new EquipmentUnitAdd(1L, 1L, str.toString());

        // before event is received
        assertEquals(1, kernel.getEquipmentMessageHandlersTable().size());

        ChangeReport report = kernel.onEquipmentUnitAdd(equipmentUnitAddEvent);
        assertNotNull(report);

        assertEquals(1, report.getChangeId());
        // adding new equipment should fail, since equipment unit with id 1 is already registered
        assertTrue(report.isFail());

        // after event is received
        assertEquals(1, kernel.getEquipmentMessageHandlersTable().size());

    }

    @DirtiesContext
    @Test
    public void testonEquipmentUnitAddRemove() {

        StringBuilder str = new StringBuilder();
        str.append("<EquipmentUnit id=\"2\" name=\"E_TEST2\">");
        str.append("<handler-class-name>cern.c2mon.driver.common.conf.DummyMessageHandler</handler-class-name>");
        str.append("<commfault-tag-id>47015</commfault-tag-id>");
        str.append("<commfault-tag-value>false</commfault-tag-value>");
        str.append("<alive-tag-id>47322</alive-tag-id>");
        str.append("<alive-interval>60000</alive-interval>");
        str.append("<address/>");
        str.append("<SubEquipmentUnits/>");
        str.append("<DataTags/>");
        str.append("<CommandTags/>");
        str.append("</EquipmentUnit>");

        EquipmentUnitAdd equipmentUnitAddEvent = new EquipmentUnitAdd(1L, 2L, str.toString());

        str = new StringBuilder();
        str.append("<EquipmentUnit id=\"3\" name=\"E_TEST3\">");
        str.append("<handler-class-name>cern.c2mon.driver.common.conf.DummyMessageHandler</handler-class-name>");
        str.append("<commfault-tag-id>47016</commfault-tag-id>");
        str.append("<commfault-tag-value>false</commfault-tag-value>");
        str.append("<alive-tag-id>47323</alive-tag-id>");
        str.append("<alive-interval>60000</alive-interval>");
        str.append("<address/>");
        str.append("<SubEquipmentUnits/>");
        str.append("<DataTags/>");
        str.append("<CommandTags/>");
        str.append("</EquipmentUnit>");

        EquipmentUnitAdd equipmentUnitAddEvent2 = new EquipmentUnitAdd(2L, 3L, str.toString());

        // before event is received
        assertEquals(1, kernel.getEquipmentMessageHandlersTable().size());

        ChangeReport report1 = kernel.onEquipmentUnitAdd(equipmentUnitAddEvent);
        ChangeReport report2 = kernel.onEquipmentUnitAdd(equipmentUnitAddEvent2);
        assertNotNull(report1);
        assertNotNull(report2);

        assertEquals(1, report1.getChangeId());
        assertEquals(2, report2.getChangeId());

        assertTrue(report1.isSuccess());
        assertTrue(report2.isSuccess());

        // after event is received
        assertEquals(3, kernel.getEquipmentMessageHandlersTable().size());

        ChangeReport report3 = kernel.onEquipmentUnitRemove(new EquipmentUnitRemove(4L, 1L));

        assertNotNull(report2);
        assertTrue(report3.isSuccess());

        // after event is received
        assertEquals(2, kernel.getEquipmentMessageHandlersTable().size());

    }

}
