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
package cern.c2mon.server.configuration.mybatis;

import static org.junit.Assert.*;

import java.util.List;

import cern.c2mon.server.configuration.junit.CachePopulationRule;
import cern.c2mon.server.configuration.junit.ConfigurationDatabasePopulationRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.configuration.mybatis.ConfigurationMapper;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;

/**
 * Integration tests of the configuration Mybatis mapper with
 * the Oracle DB. The tests should be run on an account
 * with the required test data (found in the attached scripts).
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "classpath:config/server-configuration.xml",
    "classpath:config/server-daqcommunication-in.xml",
    "classpath:config/server-daqcommunication-out.xml",
    "classpath:config/server-cache.xml",
    "classpath:config/server-cachedbaccess.xml",
    "classpath:config/server-rule.xml",
    "classpath:config/server-supervision.xml",
    "classpath:test-config/server-test-properties.xml"
})
@TestPropertySource("classpath:c2mon-server-default.properties")
public class ConfigurationMapperTest {

  @Rule
  @Autowired
  public ConfigurationDatabasePopulationRule populationRule;

  @Autowired
  private ConfigurationMapper configurationMapper;
  
  @Test
  public void testGetConfigName() {
    assertEquals("create subequipment", configurationMapper.getConfigName(19)); 
  }

  @Test
  public void testGetConfigElements() {
    List<ConfigurationElement> configElements = configurationMapper.getConfigElements(25);
    
    //retrieved all
    assertEquals(2, configElements.size());
    
    //in the right order
    ConfigurationElement elementFirst = configElements.get(0);
    ConfigurationElement elementLast = configElements.get(1);
    assertEquals(new Long(25), elementFirst.getSequenceId());
    assertEquals(new Long(26), elementLast.getSequenceId());
    
    //first element
    assertEquals(Action.CREATE, elementFirst.getAction());
    assertEquals(9, elementFirst.getElementProperties().size());
    assertEquals(Entity.CONTROLTAG, elementFirst.getEntity());
    assertEquals(new Long(25), elementFirst.getConfigId());
    
    assertEquals("Equipment alive", elementFirst.getElementProperties().get("name"));
    assertEquals("test", elementFirst.getElementProperties().get("description"));
    assertEquals("Integer", elementFirst.getElementProperties().get("dataType"));
    assertEquals("2", elementFirst.getElementProperties().get("mode"));
    assertEquals("false", elementFirst.getElementProperties().get("isLogged"));
    assertEquals("<DataTagAddress>"
                  + "<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl\">"
                  + "<opc-item-name>CW_TEMP_IN_COND4</opc-item-name>"
                  + "</HardwareAddress>"
                  + "</DataTagAddress>"
                  , elementFirst.getElementProperties().get("address"));
    assertEquals("12", elementFirst.getElementProperties().get("minValue"));
    assertEquals("22", elementFirst.getElementProperties().get("maxValue"));
    
    
    //second element
    assertEquals(Action.UPDATE, elementLast.getAction());
    assertEquals(Entity.EQUIPMENT, elementLast.getEntity());
    assertEquals(3, elementLast.getElementProperties().size());
    assertEquals(new Long(25), elementLast.getConfigId());
    
    assertEquals("serverHostName=VGTCVENTTEST;test", elementLast.getElementProperties().get("address"));
    assertEquals("updated description", elementLast.getElementProperties().get("description"));
    assertEquals("1251", elementLast.getElementProperties().get("aliveTagId"));
     
  }
  
  /**
   * Checks executes (no check if insertion was successful).
   */
  @Test
  public void testSaveStatusInfo() {
    List<ConfigurationElement> elements = configurationMapper.getConfigElements(1);
    ConfigurationElement element = elements.iterator().next();
    element.setStatus(Status.OK);
    element.setDaqStatus(Status.RESTART);
    configurationMapper.saveStatusInfo(element);
  }
  
  /**
   * Checks execution (no check if insertion was successful).
   */
  @Test
  public void testMarkAsApplied() {   
    configurationMapper.markAsApplied(1);
  }
  
}
