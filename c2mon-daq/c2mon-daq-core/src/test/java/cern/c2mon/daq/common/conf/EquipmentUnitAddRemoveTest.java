/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.common.conf;

import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.common.ProcessMessageSenderMock;
import cern.c2mon.daq.common.messaging.impl.ProcessMessageSender;
import cern.c2mon.daq.config.DaqCoreModule;
import cern.c2mon.daq.common.timer.FreshnessMonitor;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.EquipmentUnitAdd;
import cern.c2mon.shared.daq.config.EquipmentUnitRemove;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    DaqCoreModule.class,
    ProcessMessageSenderMock.class
})
@TestPropertySource(
    value = "classpath:c2mon-daq-default.properties",
    properties = {
        "c2mon.daq.name=P_TEST",
        "c2mon.daq.jms.url=vm://localhost:61616?broker.persistent=false&broker.useShutdownHook=false&broker.useJmx=false"
    }
)
public class EquipmentUnitAddRemoveTest {

  @Autowired
  DriverKernel kernel;

  @Before
  public void setUp() throws Exception {
    kernel.setProcessMessageSender(EasyMock.createMock(ProcessMessageSender.class));
  }

  @DirtiesContext
  @Test
  public void testonEquipmentUnitAdd1() throws Exception {

    StringBuilder str = new StringBuilder();
    str.append("<EquipmentUnit id=\"2\" name=\"E_TEST2\">");
    str.append("<handler-class-name>cern.c2mon.daq.common.conf.DummyMessageHandler</handler-class-name>");
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
    str.append("<handler-class-name>cern.c2mon.daq.common.conf.DummyMessageHandler</handler-class-name>");
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
    str.append("<handler-class-name>cern.c2mon.daq.common.conf.DummyMessageHandler</handler-class-name>");
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
    str.append("<handler-class-name>cern.c2mon.daq.common.conf.DummyMessageHandler</handler-class-name>");
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
