package cern.c2mon.server.shorttermlog;

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

import cern.c2mon.server.shorttermlog.listener.LogCommandListener;
import cern.c2mon.server.shorttermlog.mapper.CommandTagLogMapper;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.client.command.CommandExecutionStatus;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandReportImpl;
import cern.c2mon.shared.client.command.CommandTagLog;
import cern.c2mon.shared.daq.command.CommandTag;

/**
 * Tests the module works correctly, mocking all other
 * server modules.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/shorttermlog/config/server-shorttermlog-module-test.xml"})
public class ShortTermLogModuleTest {

  @Autowired
  private LogCommandListener logCommandListener;
  
  @Autowired
  private CommandTagLogMapper commandTagMapper;
    
  @Before
  public void setUp() {
    commandTagMapper.deleteAllLogs(CacheObjectCreation.createTestCommandTag().getId());
  }
  
  @After
  public void cleanUp() {
    commandTagMapper.deleteAllLogs(CacheObjectCreation.createTestCommandTag().getId());
  }
  
  /**
   * Tests logging of commands works.
   */
  @Test
  public void testCommandLogging() {
    CommandTag commandTag = CacheObjectCreation.createTestCommandTag();
    CommandReport report = new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_AUTHORISATION_FAILED, "report text", new Timestamp(System.currentTimeMillis()), new Short((short) 2));
    logCommandListener.log(commandTag, report);
    
    List<CommandTagLog> retrievedLogList = commandTagMapper.getCommandTagLog(commandTag.getId());
    
    assertNotNull(retrievedLogList);
    assertEquals(1, retrievedLogList.size());
    
    CommandTagLog retrievedLog = retrievedLogList.get(0);
    
    assertEquals(commandTag.getId(), retrievedLog.getTagId());
    assertEquals(commandTag.getId().toString(), retrievedLog.getId());
    assertEquals(commandTag.getName(), retrievedLog.getName());
    assertEquals(commandTag.getDataType(), retrievedLog.getDataType());
    assertEquals(commandTag.getCommandExecutionDetails().getValue().toString(), retrievedLog.getValue());
    //assertEquals(commandTag.getHost(), retrievedLog.getHost());
    assertEquals(Short.valueOf(commandTag.getMode()), retrievedLog.getMode());
    //assertEquals(commandTag.getUser(), retrievedLog.getUser());
    assertEquals(report.getStatus(), retrievedLog.getReportStatus());
    assertEquals(report.getReportText(), retrievedLog.getReportDescription());
    
    //check time is logged in UTC format to DB
    int offset = TimeZone.getDefault().getOffset(commandTag.getCommandExecutionDetails().getExecutionStartTime().getTime());    
    assertEquals(commandTag.getCommandExecutionDetails().getExecutionStartTime().getTime(), retrievedLog.getExecutionTime().getTime() + offset);
    
    //check time is logged in UTC format to DB
    int offset2 = TimeZone.getDefault().getOffset(report.getTimestamp().getTime());    
    assertEquals(report.getTimestamp().getTime(), retrievedLog.getReportTime().getTime() + offset2);
  }
  
  
}
