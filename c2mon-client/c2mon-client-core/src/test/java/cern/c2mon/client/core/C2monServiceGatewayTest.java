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
package cern.c2mon.client.core;


import cern.c2mon.client.core.config.C2monAutoConfiguration;
import cern.c2mon.client.core.config.mock.MockServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import javax.jms.ConnectionFactory;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static org.junit.Assert.assertNotNull;
import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.jms.TestBrokerService;

/**
 * Integration test of Client API modules.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {
        C2monAutoConfiguration.class,
        MockServerConfig.class
    }
)
public class C2monServiceGatewayTest {

  @BeforeClass
  public static void setSystemProperty() {
    System.setProperty("c2mon.client.jms.url", "vm://localhost:61616?broker.persistent=false");
  }

  private static TestBrokerService testBrokerService = new TestBrokerService();

  @BeforeClass
  public static void startBroker() throws Exception {
    testBrokerService.createAndStartBroker();
  }

  @Test
  public void startClient() throws Exception {
    C2monServiceGateway.startC2monClient();
    assertNotNull(C2monServiceGateway.getCommandManager());
    assertNotNull(C2monServiceGateway.getCommandService());
    assertNotNull(C2monServiceGateway.getSupervisionService());
    assertNotNull(C2monServiceGateway.getSupervisionManager());
    assertNotNull(C2monServiceGateway.getTagService());
    assertNotNull(C2monServiceGateway.getAlarmService());
    assertNotNull(C2monServiceGateway.getConfigurationService());
//    assertNotNull(C2monServiceGateway.getSessionService());
    assertNotNull(C2monServiceGateway.getStatisticsService());
    assertNotNull(C2monServiceGateway.getTagManager());
  }
}
