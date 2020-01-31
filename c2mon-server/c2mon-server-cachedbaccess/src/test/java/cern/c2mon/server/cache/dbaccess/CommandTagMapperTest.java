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
package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.*;

public class CommandTagMapperTest extends AbstractMapperTest {

  @Inject
  private CommandTagMapper commandTagMapper;

  private CommandTagCacheObject commandTag;

  @Before
  public void setUp() {
//    testDataHelper.removeTestData(); //cleans DB after any previous non-finished tests
//    testDataHelper.createTestData();
    commandTag = (CommandTagCacheObject) commandTagMapper.getItem(11000L);
//    testDataHelper.insertTestDataIntoDB();
  }

  @Test
  public void testRetrieveOneFromDB() {
    //has already been inserted above
    CommandTagCacheObject retrievedTag = (CommandTagCacheObject) commandTagMapper.getItem(commandTag.getId());
    assertNotNull(retrievedTag);

    //includes check on process field
    CacheObjectComparison.equals(commandTag, retrievedTag);

  }

  @Test
  public void testGetAll() {
    List<CommandTag> commandList = commandTagMapper.getAll();
    assertNotNull(commandList);
    assertTrue(commandList.size() == 2);
  }

  @Test
  public void testGetCommandTag() {
    //construct fake DataTagCacheObject, setting all fields
    CommandTagCacheObject cacheObject = new CommandTagCacheObject(200_000L);
    cacheObject.setName("Junit_test_command_tag"); //non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Integer"); // non null
    cacheObject.setHardwareAddress(new SimpleHardwareAddressImpl("test hardware address"));
    cacheObject.setEquipmentId(150L); //need test equipment inserted - use EquipmentMapperTest
    cacheObject.setMaximum(23);
    cacheObject.setMinimum(10);
    Metadata metadata = new Metadata();
    metadata.addMetadata("String", 11);
    cacheObject.setMetadata(metadata);
    cacheObject.setProcessId(50L); //need test process also (P_JAPC01)
    cacheObject.setSourceTimeout(10);
    cacheObject.setSourceRetries(2);
    cacheObject.setExecTimeout(10);
    cacheObject.setClientTimeout(3);
    RbacAuthorizationDetails rbac = new RbacAuthorizationDetails();
    rbac.setRbacProperty("property");
    rbac.setRbacDevice("device");
    rbac.setRbacClass("class");
    cacheObject.setAuthorizationDetails(rbac);
    //put in database
    commandTagMapper.insertCommandTag(cacheObject);

    //retrieve and check successful
    CommandTagCacheObject retrievedCommand = (CommandTagCacheObject) commandTagMapper.getItem(cacheObject.getId());
    assertNotNull(retrievedCommand);
    CacheObjectComparison.equals(cacheObject, retrievedCommand);
  }

  @Test
  public void testUpdate() {
    CommandTagCacheObject modifiedCommand = new CommandTagCacheObject(commandTag);
    //below: must all be different then values set in create method above
    modifiedCommand.setName("new name");
    modifiedCommand.setDescription("new description");
    modifiedCommand.setDataType("Integer");
    modifiedCommand.setMode(DataTagConstants.MODE_TEST);
    modifiedCommand.setEquipmentId(160L);
    //must change process manually here for assertions work...
    modifiedCommand.setProcessId(50L);
    try {
      modifiedCommand.setHardwareAddress(new OPCHardwareAddressImpl("newAddress"));
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    modifiedCommand.setSourceTimeout(10);
    modifiedCommand.setSourceRetries(2);
    modifiedCommand.setExecTimeout(10);
    modifiedCommand.setClientTimeout(3);
    modifiedCommand.setMinimum(30);
    modifiedCommand.setMaximum(60);
    Metadata metadata = new Metadata();
    metadata.addMetadata("metadata", 11);
    modifiedCommand.setMetadata(metadata);

    //update
    commandTagMapper.updateCommandTag(modifiedCommand);

    //retrieve and check successful
    CommandTagCacheObject retrievedCommand = (CommandTagCacheObject) commandTagMapper.getItem(modifiedCommand.getId());
    assertNotNull(retrievedCommand);
    CacheObjectComparison.equals(modifiedCommand, retrievedCommand);
  }

  @Test
  public void testIsInDB() {
    assertTrue(commandTagMapper.isInDb(11000L));
  }

  @Test
  public void testNotInDB() {
    assertFalse(commandTagMapper.isInDb(1263L));
  }

}
