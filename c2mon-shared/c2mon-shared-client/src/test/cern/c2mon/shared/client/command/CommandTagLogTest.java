package cern.c2mon.shared.client.command;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.junit.Test;

import cern.c2mon.pmanager.fallback.exception.DataFallbackException;

/**
 * Unit test of Command object for logging to STL.
 * 
 * @author Mark Brightwell
 *
 */
public class CommandTagLogTest {

  /**
   * @return a test object
   */
  public static CommandTagLog createLogObject() {
    CommandTagLog commandTagLog = new CommandTagLog();
    commandTagLog.setDataType("String");
    commandTagLog.setExecutionTime(new Timestamp(System.currentTimeMillis()));
    commandTagLog.setHost("host");
    commandTagLog.setUser("user");
    commandTagLog.setId(10L);
    commandTagLog.setMode(Short.valueOf("2"));
    commandTagLog.setName("name");
    commandTagLog.setValue("value");
    commandTagLog.setReportDescription("report-desc");
    commandTagLog.setReportStatus(CommandExecutionStatus.STATUS_AUTHORISATION_FAILED);
    commandTagLog.setReportTime(new Timestamp(System.currentTimeMillis() + 1000));
    commandTagLog.setTagId(20L);    
    return commandTagLog;
  }
  
  @Test
  public void testFallbackEncoding() throws DataFallbackException {
    CommandTagLog commandLog = createLogObject();
    String encoded = commandLog.toString();
    CommandTagLog decoded = (CommandTagLog) commandLog.getObject(encoded);
    assertEquals(commandLog, decoded);
  }
  
}
