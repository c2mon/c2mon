package cern.c2mon.server.cache.process;

import static org.junit.Assert.*;

import java.util.Properties;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.common.process.Process;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * Unit test of ProcessFacade implementation
 * 
 * @author Mark Brightwell
 *
 */
public class ProcessFacadeImplTest {
  
  private ProcessFacade processFacade;
  
  private IMocksControl control;
  
  //mocks
  private EquipmentFacade equipmentFacade;
  private ProcessCache processCache;
  private SubEquipmentFacade subEquipmentFacade;
  private AliveTimerCache aliveTimerCache;
  private AliveTimerFacade aliveTimerFacade;
  
  @Before
  public void setUp() {
    control = EasyMock.createControl();
    equipmentFacade = control.createMock(EquipmentFacade.class);
    processCache = control.createMock(ProcessCache.class);
    subEquipmentFacade = control.createMock(SubEquipmentFacade.class);
    aliveTimerFacade = control.createMock(AliveTimerFacade.class);
    aliveTimerCache = control.createMock(AliveTimerCache.class);
    processFacade = new ProcessFacadeImpl(equipmentFacade, processCache, subEquipmentFacade, aliveTimerCache, aliveTimerFacade);
  }
  
  @Test(expected=ConfigurationException.class)
  public void testFailCreateCacheObject() throws IllegalAccessException {
    Properties properties = new Properties();    
    processFacade.createCacheObject(1L, properties);
  }
  
  @Test
  public void testCreateCacheObject() throws IllegalAccessException {
    Properties properties = new Properties();    
    properties.put("name", "P_NAME");
    properties.put("description", "description");
    properties.put("maxMessageSize", 3);
    properties.put("maxMessageDelay", 500);
    properties.put("stateTagId", "4");
    properties.put("aliveTagId", "3");
    properties.put("aliveInterval", 20000);
    
    processCache.acquireReadLockOnKey(1L);
    processCache.releaseReadLockOnKey(1L);
        
    control.replay();
    Process process = processFacade.createCacheObject(1L, properties);    
    control.verify();
    assertEquals("c2mon.jms.process.listener.trunk.default" + ".NOHOST" + "." + "P_NAME" + ".NOTIME", process.getJmsListenerTopic());
  }
}
