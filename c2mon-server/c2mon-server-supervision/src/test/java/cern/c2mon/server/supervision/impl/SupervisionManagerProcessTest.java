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
package cern.c2mon.server.supervision.impl;

import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.supervision.SupervisionManager;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.server.supervision.junit.SupervisionCachePopulationRule;
import cern.c2mon.shared.daq.process.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

/**
 * Integration test of supervision module for all Process Messaging (PIK, Connection, Disconnection)
 *
 * @author Nacho Vilches
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheConfigModuleRef.class,
    CacheDbAccessModule.class,
    SupervisionModule.class
})
public class SupervisionManagerProcessTest {

  @Rule
  @Autowired
  public SupervisionCachePopulationRule supervisionCachePopulationRule;

  /**
   * The system's logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SupervisionManagerProcessTest.class);

  @Autowired
  private SupervisionManager supervisionManager;

  /**
   * Constant of the NO_PIK as default value
   */
  public static final Long NO_PIK = -1L;

  /**
   * Process PIK Request
   */
  private ProcessConnectionRequest processConnectionRequest;

  /**
   * Process PIK Response
   */
  private ProcessConnectionResponse processConnectionResponse;

  /**
   * Process Configuration Request
   */
  private ProcessConfigurationRequest processConfigurationRequest;

  /**
   * Process Configuration Response
   */
  private ProcessConfigurationResponse processConfigurationResponse;

  /**
   * Process Disconnection Request
   */
  private ProcessDisconnectionRequest processDisconnectionRequest;

  /**
   * XML Converter helper class
   */
  private XMLConverter xmlConverter = new XMLConverter();


  @Before
  public void setUp() {
    processConnectionRequest = null;
    processConnectionResponse = null;
    processConfigurationRequest = null;
    processConfigurationResponse = null;
    processDisconnectionRequest = null;
//    processDisconnectionBC = null;
  }

//  @After
//  public void disconnection() {
//    // Disconnection
//    if((processConnectionResponse != null) && (processConnectionRequest != null) /*&& (processDisconnectionBC == null)*/) {
//      processDisconnectionRequest = new ProcessDisconnectionRequest(GOOD_PROCESSNAME, processConnectionResponse.getProcessPIK(),
//          processConnectionRequest.getProcessStartupTime().getTime());
//
//      onProcessDisconnection();
//    }
//  }

  /**
   * Process Connection call
   */
  public void onProcessConnection() {
    LOGGER.info("onProcessConnection - Connection");

    LOGGER.info("{}", processConnectionRequest);

    String xmlprocessConnectionResponse = supervisionManager.onProcessConnection(processConnectionRequest);
    assertNotNull(xmlprocessConnectionResponse);

    LOGGER.info(xmlprocessConnectionResponse);

    try {
      processConnectionResponse = (ProcessConnectionResponse) xmlConverter.fromXml(xmlprocessConnectionResponse);
    }
    catch (Exception e) {
      LOGGER.error(e.toString());
    }
    assertNotNull(processConnectionResponse);
    LOGGER.info(processConnectionResponse.toString());
  }

  /**
   * Process Configuration call
   */
  public void onProcessConfiguration() {
    LOGGER.info("onProcessConfiguration - Configuration");

    LOGGER.info("{}", processConnectionRequest);

    String xmlProcessConfigurationResponse = supervisionManager.onProcessConfiguration(processConfigurationRequest);
    assertNotNull(xmlProcessConfigurationResponse);
    LOGGER.info(xmlProcessConfigurationResponse);

    try {
      processConfigurationResponse = (ProcessConfigurationResponse) xmlConverter.fromXml(xmlProcessConfigurationResponse);
    }
    catch (Exception e) {
      LOGGER.error(e.toString());
    }
    assertNotNull(processConfigurationResponse);
    LOGGER.info(processConfigurationResponse.toString());
  }

  /**
   * Process Disconnection call
   */
  public void onProcessDisconnection() {
    LOGGER.info("Disconnection");

    LOGGER.info("{}", processConnectionRequest);

    supervisionManager.onProcessDisconnection(processDisconnectionRequest);
  }
}
