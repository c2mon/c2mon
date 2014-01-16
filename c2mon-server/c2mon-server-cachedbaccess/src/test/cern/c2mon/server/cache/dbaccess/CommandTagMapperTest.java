package cern.c2mon.server.cache.dbaccess;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.daq.command.CommandTag;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml"})
@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
@Transactional
public class CommandTagMapperTest {

  @Autowired
  private CommandTagMapper commandTagMapper;
  
  @Autowired
  private TestDataHelper testDataHelper;
  
  private CommandTagCacheObject commandTag;
  
  @Before
  public void setUp() {  
    testDataHelper.removeTestData(); //cleans DB after any previous non-finished tests
    testDataHelper.createTestData();
    commandTag = testDataHelper.getCommandTag();
    testDataHelper.insertTestDataIntoDB();
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
    assertTrue(commandList.size() == 3);
  }
  
  @Test
  public void testUpdate() {
    CommandTagCacheObject modifiedCommand = new CommandTagCacheObject(commandTag);
    //below: must all be different then values set in create method above
    modifiedCommand.setName("new name");
    modifiedCommand.setDescription("new description");
    modifiedCommand.setDataType("Integer");
    modifiedCommand.setMode(DataTagConstants.MODE_TEST);
    modifiedCommand.setEquipmentId(Long.valueOf(160));
    //must change process manually here for assertions work...
    modifiedCommand.setProcessId(Long.valueOf(50));
    try {
      modifiedCommand.setHardwareAddress(new OPCHardwareAddressImpl("newAddress"));
    } catch (ConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    modifiedCommand.setSourceTimeout(10);
    modifiedCommand.setSourceRetries(2);
    modifiedCommand.setExecTimeout(10);
    modifiedCommand.setClientTimeout(3);
    modifiedCommand.setMinimum(Float.valueOf(30));
    modifiedCommand.setMaximum(Float.valueOf(60)); 
    
    //update
    commandTagMapper.updateCommandTag(modifiedCommand);
    
    //retrieve and check successful
    CommandTagCacheObject retrievedCommand = (CommandTagCacheObject) commandTagMapper.getItem(modifiedCommand.getId());
    assertNotNull(retrievedCommand);
    CacheObjectComparison.equals(modifiedCommand, retrievedCommand);
  }
  
  @After
  public void removeTestData() {
    testDataHelper.removeTestData();
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
