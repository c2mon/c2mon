package cern.c2mon.server.shorttermlog.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import java.util.List;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.tim.shared.client.command.CommandExecutionStatus;
import cern.tim.shared.client.command.CommandTagLog;

/**
 * Test of Mybatis Mapper implementation.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/shorttermlog/config/server-shorttermlog-mapper-test.xml"})
public class CommandTagMapperTest {

  private static final Long COMMAND_ID = 1111L;
  
  /**
   * To test.
   */
  @Autowired
  private CommandTagLogMapper commandTagLogMapper;    
  
  /**
   * Removes test values from previous tests in case clean up failed.
   */
  @Before
  public void beforeTest() {
    removeTestData();
  }
  
  /**
   * Removes test values after test.
   */
  @After
  public void afterTest() {
    removeTestData();
  }

  private void removeTestData() {
    commandTagLogMapper.deleteAllLogs(COMMAND_ID);
  }
  
  /**
   * Assumes no logs in test table for this command.
   */
  @Test
  public void testLogCommand() {
    CommandTagLog commandLog = new CommandTagLog();
    commandLog.setId(COMMAND_ID);
    commandLog.setName("command name");
    commandLog.setDataType("datatype");
    commandLog.setMode(Short.valueOf((short) 2));
    commandLog.setExecutionTime(new Timestamp(System.currentTimeMillis()));
    commandLog.setValue("value");
    commandLog.setHost("host");
    commandLog.setUser("user");   
    commandLog.setReportStatus(CommandExecutionStatus.STATUS_CMD_UNKNOWN);
    commandLog.setReportTime(new Timestamp(System.currentTimeMillis() + 1000));
    commandLog.setReportDescription("report text");
    
    commandTagLogMapper.insertLog(commandLog);
    
    List<CommandTagLog> retrievedLogList = commandTagLogMapper.getCommandTagLog(COMMAND_ID);
    
    assertNotNull(retrievedLogList);
    assertEquals(1, retrievedLogList.size());
    
    CommandTagLog retrievedLog = retrievedLogList.get(0);
    
    assertEquals(commandLog.getId(), retrievedLog.getId());
    assertEquals(commandLog.getName(), retrievedLog.getName());
    assertEquals(commandLog.getDataType(), retrievedLog.getDataType());
    assertEquals(commandLog.getValue(), retrievedLog.getValue());
    assertEquals(commandLog.getHost(), retrievedLog.getHost());
    assertEquals(commandLog.getMode(), retrievedLog.getMode());
    assertEquals(commandLog.getUser(), retrievedLog.getUser());
    assertEquals(commandLog.getReportStatus(), retrievedLog.getReportStatus());
    assertEquals(commandLog.getReportDescription(), retrievedLog.getReportDescription());
    
    //check time is logged in UTC format to DB
    int offset = TimeZone.getDefault().getOffset(commandLog.getReportTime().getTime());    
    assertEquals(commandLog.getReportTime().getTime(), retrievedLog.getReportTime().getTime() + offset);
    
  //check time is logged in UTC format to DB
    int offset2 = TimeZone.getDefault().getOffset(commandLog.getExecutionTime().getTime());    
    assertEquals(commandLog.getExecutionTime().getTime(), retrievedLog.getExecutionTime().getTime() + offset2);
        
  }
  
}
