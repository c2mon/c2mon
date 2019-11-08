package cern.c2mon.cache.actions.command;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.shared.common.ConfigurationException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;

import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Szymon Halastra
 */
public class CommandCacheObjectFactoryTest {

  private C2monCache<Equipment> equipmentCache;

  private CommandTagCacheObjectFactory factory;

  @Before
  public void init() {
    equipmentCache = EasyMock.createNiceMock(C2monCache.class);

//    factory = new CommandCacheObjectFactory(equipmentCache);
  }

  @Test
  @Ignore
  public void createCommandCacheObject() throws IllegalAccessException {
    replay(equipmentCache);

    Properties properties = new Properties();
    properties.setProperty("name", "command");
    properties.setProperty("description", "command description");
    properties.setProperty("dataType", "long");
    properties.setProperty("sourceTimeout", "1000");
    properties.setProperty("clientTimeout", "10000");
    properties.setProperty("sourceRetries", "2");
    properties.setProperty("execTimeout", "21000");
    properties.setProperty("clientTimeout", "22001");

    //TODO: write missing test
    properties.setProperty("equipmentId", "1");

    CommandTagCacheObject command = (CommandTagCacheObject) factory.createCacheObject(1L, properties);

    assertEquals("Command should have a name", "command", command.getName());
    assertEquals("Command should have a description", "command description", command.getDescription());
    assertEquals("Command should have a dataType set", "long", command.getDataType());
    assertEquals("Command should have sourceTimeout set", 1000, command.getSourceTimeout());
    assertEquals("Command should have clientTimeout set", 10000, command.getClientTimeout());
    assertEquals("Command should have sourceRetries set", 2, command.getSourceRetries());
    assertEquals("Command should have execTimeout set", 21000, command.getExecTimeout());
    assertEquals("Command should have clientTimeout set", 22001, command.getClientTimeout());
  }

  @Test
  @Ignore
  public void throwExceptionWhenNameIsMissing() throws IllegalAccessException {
    Properties properties = new Properties();
    try {
      factory.createCacheObject(1L, properties);
      fail("Exception should be thrown");
    }
    catch (ConfigurationException e) {
      assertEquals("INVALID-PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }

  @Test
  @Ignore
  public void throwExceptionWhenDescriptionIsMissing() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.setProperty("name", "command");
    try {
      factory.createCacheObject(1L, properties);
      fail("Exception should be thrown");
    }
    catch (ConfigurationException e) {
      assertEquals("INVALID-PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }

  @Test
  @Ignore
  public void throwExceptionWhenDataTypeIsMissing() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.setProperty("name", "command");
    properties.setProperty("description", "command description");

    try {
      factory.createCacheObject(1L, properties);
      fail("Exception should be thrown");
    }
    catch (ConfigurationException e) {
      assertEquals("INVALID-PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }

  @Test
  @Ignore
  public void throwExceptionWhenSourceTimeoutIsUnder100() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.setProperty("name", "command");
    properties.setProperty("description", "command description");
    properties.setProperty("dataType", "long");
    properties.setProperty("sourceTimeout", "50");

    try {
      factory.createCacheObject(1L, properties);
      fail("Exception should be thrown");
    }
    catch (ConfigurationException e) {
      assertEquals("INVALID-PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }

  @Test
  @Ignore
  public void throwExceptionWhenClientTimeoutISUnder5000() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.setProperty("name", "command");
    properties.setProperty("description", "command description");
    properties.setProperty("dataType", "long");
    properties.setProperty("sourceTimeout", "1000");
    properties.setProperty("clientTimeout", "3000");

    try {
      factory.createCacheObject(1L, properties);
      fail("Exception should be thrown");
    }
    catch (ConfigurationException e) {
      assertEquals("INVALID-PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }

  @Test
  @Ignore
  public void throwExceptionWhenExecTimeoutIsGreaterThanClientTimeout() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.setProperty("name", "command");
    properties.setProperty("description", "command description");
    properties.setProperty("dataType", "long");
    properties.setProperty("sourceTimeout", "1000");
    properties.setProperty("clientTimeout", "10000");
    properties.setProperty("execTimeout", "12000");

    try {
      factory.createCacheObject(1L, properties);
      fail("Exception should be thrown");
    }
    catch (ConfigurationException e) {
      assertEquals("INVALID-PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }

  @Test
  @Ignore
  public void throwExceptionWhenExecTimeoutIsSmaller() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.setProperty("name", "command");
    properties.setProperty("description", "command description");
    properties.setProperty("dataType", "long");
    properties.setProperty("sourceTimeout", "1000");
    properties.setProperty("clientTimeout", "10000");
    properties.setProperty("execTimeout", "12000");
    properties.setProperty("sourceRetries", "3");

    try {
      factory.createCacheObject(1L, properties);
      fail("Exception should be thrown");
    }
    catch (ConfigurationException e) {
      assertEquals("INVALID-PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }

  @Test
  @Ignore
  public void throwExceptionWhenClientTimeoutIsSmallerThanExecTimeout() throws IllegalAccessException {
    Properties properties = new Properties();
    properties.setProperty("name", "command");
    properties.setProperty("description", "command description");
    properties.setProperty("dataType", "long");
    properties.setProperty("sourceTimeout", "1000");
    properties.setProperty("clientTimeout", "10000");
    properties.setProperty("execTimeout", "12000");
    properties.setProperty("sourceRetries", "3");
    properties.setProperty("clientTimeout", "12001");

    try {
      factory.createCacheObject(1L, properties);
      fail("Exception should be thrown");
    }
    catch (ConfigurationException e) {
      assertEquals("INVALID-PARAMETER should be thrown", 0, e.getErrorCode());
    }
  }
}
