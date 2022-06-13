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

package cern.c2mon.daq.test;

import java.lang.reflect.Method;

import org.apache.xerces.parsers.DOMParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.EquipmentConfigurationFactory;
import cern.c2mon.daq.common.conf.core.EquipmentConfigurationHandler;
import cern.c2mon.daq.common.impl.EquipmentCommandHandler;
import cern.c2mon.daq.common.impl.EquipmentMessageSender;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.common.messaging.impl.RequestController;
import cern.c2mon.daq.common.timer.FreshnessMonitor;
import cern.c2mon.daq.config.DaqProperties;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.daq.filter.dynamic.TimeDifferenceMovingAverageTimeDeadbandActivator;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;

import static java.lang.String.format;
import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * This class implements a common parent class for JUnit testing framework for EquipmentMessageHandlers.
 *
 * @author wbuczak
 */

public abstract class GenericMessageHandlerTest {

  protected static final String TEST_PROCESS_NAME = "P_TEST_01";

  protected static final Long TEST_PROCESS_ID = 1L;

  protected EquipmentMessageHandler msgHandler;

  protected IProcessMessageSender messageSender;

  protected IFilterMessageSender filterMessageSender;

  protected IDynamicTimeDeadbandFilterActivator dynamicTimeDeadbandFilterActivator;

  protected EquipmentMessageSender equipmentMessageSender;

  protected ProcessConfiguration pconf = new ProcessConfiguration();

  protected EquipmentConfiguration equipmentConfiguration;

  protected ConfigurationController configurationController;

  protected FreshnessMonitor freshnessMonitorMock;

  static Logger log = LoggerFactory.getLogger(GenericMessageHandlerTest.class);

  @Rule
  public TestName testName = new TestName();

  protected final String configFileName(final String testName) throws Exception {

    Method testMethod = this.getClass().getMethod(testName, (Class<?>[]) null);

    if (testMethod.isAnnotationPresent(UseConf.class)) {
      return format("/conf/%s", testMethod.getAnnotation(UseConf.class).value());
    }

    return null;
  }

  protected final String getHandlerClass() throws Exception {
    Class<? extends GenericMessageHandlerTest> clazz = this.getClass();

    if (clazz.isAnnotationPresent(UseHandler.class)) {
      return clazz.getAnnotation(UseHandler.class).value().getName();
    }

    return null;
  }


  protected abstract void beforeTest() throws Exception;

  protected abstract void afterTest() throws Exception;


  @Before
  public void setUp() throws Exception {

    int numTests = 0;
    // check how many test cases are defined in that test class
    for (Method m : this.getClass().getMethods()) {
      if (m.isAnnotationPresent(Test.class)) {
        numTests++;
      }
    }

    log.debug(format("number of tests configured in that class: %d", numTests));

    if (log.isDebugEnabled()) {
      log.debug("****************************************************************\n");
      log.debug(format("executing test: %s", testName.getMethodName()));
      log.debug("****************************************************************\n");
    }

    // find out which configuration file shall be taken
    String testConfig = configFileName(testName.getMethodName());

    // configure the handler only if the test requires it - i.e. is annotated with UseConf annotation
    if (testConfig != null) {

      messageSender = createMock(IProcessMessageSender.class);
      filterMessageSender = createMock(IFilterMessageSender.class);
      freshnessMonitorMock = new FreshnessMonitor(new DaqProperties());

      dynamicTimeDeadbandFilterActivator = new TimeDifferenceMovingAverageTimeDeadbandActivator(10, 110, 150, 30000);

      equipmentMessageSender = new EquipmentMessageSender(filterMessageSender, messageSender, dynamicTimeDeadbandFilterActivator, freshnessMonitorMock, new DaqProperties());

      configurationController = new ConfigurationController();
      configurationController.setProcessConfiguration(pconf);
      configurationController.setFreshnessMonitor(freshnessMonitorMock);
      pconf.setProcessID(TEST_PROCESS_ID);
      pconf.setProcessName(TEST_PROCESS_NAME);

      pconf.setAliveTagID(1000L);
      pconf.setAliveInterval(20000);

      if (log.isDebugEnabled()) {
        log
                .debug(format("configuring equipment with following test configuration file: %s\n",
                        testConfig));
      }


      String confFileAbsPath = null;
      try {
        confFileAbsPath = this.getClass().getResource(testConfig).getPath();
      }
      catch (Exception ex) {
        fail("configuration file not found: " + testConfig);
      }


      Element rootConfElement = parseEquipmentConfiguration(confFileAbsPath);
      //ProcessConfigurationLoader processConfigurationLoader = new ProcessConfigurationLoader();
      assertNotNull(rootConfElement);


      assertNotNull("Did you annotate your test with @UseHandler annotation ?!", getHandlerClass());

      // update handler name
      rootConfElement.getElementsByTagName("handler-class-name").item(0).getFirstChild().setNodeValue(
              getHandlerClass());

      equipmentConfiguration = new EquipmentConfigurationFactory().createEquipmentConfiguration(rootConfElement);
      long equipmentId = equipmentConfiguration.getId();
      msgHandler = EquipmentMessageHandler.createEquipmentMessageHandler(
              equipmentConfiguration.getHandlerClassName(),
              new EquipmentCommandHandler(equipmentId, new RequestController(configurationController)),
              new EquipmentConfigurationHandler(equipmentId, configurationController),
              equipmentMessageSender, null);
//            equipmentMessageSender.setEquipmentConfiguration(equipmentConfiguration);
//            equipmentMessageSender.setEquipmentLoggerFactory(factory);
      this.equipmentMessageSender.init(equipmentConfiguration);
      pconf.getEquipmentConfigurations().put(equipmentId, equipmentConfiguration);
      msgHandler.setEquipmentMessageSender(equipmentMessageSender);


      // call some test-specific before test initializer
      beforeTest();

      Thread.sleep(120);
    }

  }


  @After
  public void cleanUp() throws Exception {
    if (msgHandler != null)
      msgHandler.disconnectFromDataSource();
    afterTest();
  }


  protected static final Element parseEquipmentConfiguration(final String confXML) {
    Element result = null;
    try {
      DOMParser parser = new DOMParser();
      parser.parse(confXML);
      result = parser.getDocument().getDocumentElement();
    }
    catch (Exception ex) {
      log.error("could not parse configuration document", ex);
    }
    return result;
  }

}
