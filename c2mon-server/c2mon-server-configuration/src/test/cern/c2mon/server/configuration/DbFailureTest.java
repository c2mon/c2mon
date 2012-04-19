package cern.c2mon.server.configuration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.configuration.handler.impl.ProcessConfigHandlerImpl;
import cern.c2mon.server.configuration.handler.transacted.ProcessConfigTransacted;
import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.CommandTagCache;
import cern.tim.server.cache.ControlTagCache;
import cern.tim.server.cache.DataTagCache;
import cern.tim.server.cache.EquipmentCache;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.ProcessFacade;
import cern.tim.server.cache.RuleTagCache;
import cern.tim.server.cache.SubEquipmentCache;
import cern.tim.server.cache.dbaccess.AlarmMapper;
import cern.tim.server.cache.dbaccess.CommandTagMapper;
import cern.tim.server.cache.dbaccess.ControlTagMapper;
import cern.tim.server.cache.dbaccess.DataTagMapper;
import cern.tim.server.cache.dbaccess.EquipmentMapper;
import cern.tim.server.cache.dbaccess.ProcessMapper;
import cern.tim.server.cache.dbaccess.RuleTagMapper;
import cern.tim.server.cache.dbaccess.SubEquipmentMapper;
import cern.tim.server.common.process.Process;
import cern.tim.server.test.TestDataInserter;
import cern.tim.shared.client.configuration.ConfigConstants;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigurationReport;

/**
 * Tests the ConfigHandler's when DB persitence fails, for instance when a constraint
 * if violated. Checks the cache is left in a consistent state.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/configuration/config/server-configuration-oracle-test.xml" })
public class DbFailureTest implements ApplicationContextAware {

  private IMocksControl mockControl = EasyMock.createNiceControl();
  
  @Autowired
  private ProcessConfigHandlerImpl processConfigHandler;
  
  @Autowired
  private ConfigurationLoader configurationLoader;
  
  private ApplicationContext context;
  
  @Autowired
  private DataTagCache dataTagCache;
  
  @Autowired
  private DataTagMapper dataTagMapper;

  @Autowired
  private ControlTagCache controlTagCache;
  
  @Autowired
  private ControlTagMapper controlTagMapper;
  
  @Autowired
  private CommandTagCache commandTagCache;
  
  @Autowired
  private CommandTagMapper commandTagMapper;
  
  @Autowired
  private RuleTagCache ruleTagCache;
  
  @Autowired
  private RuleTagMapper ruleTagMapper;
  
  @Autowired
  private EquipmentCache equipmentCache;
  
  @Autowired
  private EquipmentMapper equipmentMapper;
  
  @Autowired
  private SubEquipmentCache subEquipmentCache;
  
  @Autowired
  private SubEquipmentMapper subEquipmentMapper;  
  
  @Autowired
  private ProcessCache processCache;
  
  @Autowired
  private ProcessMapper processMapper;
  
  @Autowired
  private AliveTimerCache aliveTimerCache;
  
  @Autowired
  private CommFaultTagCache commFaultTagCache;
  
  @Autowired
  private AlarmCache alarmCache;
    
  @Autowired
  private AlarmMapper alarmMapper;
  
  @Autowired
  private ProcessFacade processFacade;
  
  @Autowired
  private TestDataInserter testDataInserter;
  
  @Before
  public void init() {
    ((AbstractApplicationContext) context).start();
    mockControl.reset();
  }
  
  @After
  public void after() throws IOException{
    testDataInserter.removeTestData();
    testDataInserter.insertTestData();
  }
  
  /**
   * Tests the system is left in the correct consistent state if the removal of
   * the Process from the DB fails.
   */
  @Test
  @DirtiesContext
  public void testDBPersistenceFailure() {
    //reset ProcessConfigTransacted to mock
    ProcessConfigTransacted processConfigTransacted = mockControl.createMock(ProcessConfigTransacted.class);
    processConfigHandler.setProcessConfigTransacted(processConfigTransacted);
    processConfigTransacted.doRemoveProcess(EasyMock.isA(Process.class), EasyMock.isA(ConfigurationElementReport.class));
    EasyMock.expectLastCall().andThrow(new RuntimeException("fake exception thrown"));        
    
    mockControl.replay();
    
    ConfigurationReport report = configurationLoader.applyConfiguration(28);
    assertTrue(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    //check all is removed except Process & its Control tags
    //check process is still here
    assertTrue(processCache.hasKey(50L));
    assertNotNull(processMapper.getItem(50L));
    //equipment is gone
    assertFalse(equipmentCache.hasKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    //rules gone
    assertFalse(ruleTagCache.hasKey(60010L));
    assertNull(ruleTagMapper.getItem(60010L));
    assertFalse(ruleTagCache.hasKey(60002L));
    assertNull(ruleTagMapper.getItem(60002L));
    //tags
    assertFalse(dataTagCache.hasKey(200002L));
    assertNull(dataTagMapper.getItem(200002L));
    assertFalse(dataTagCache.hasKey(200003L));
    assertNull(dataTagMapper.getItem(200003L));
    //process control tags are still here!
    assertTrue(controlTagCache.hasKey(1220L));
    assertNotNull(controlTagMapper.getItem(1220L));
    assertTrue(controlTagCache.hasKey(1221L));
    assertNotNull(controlTagMapper.getItem(1221L)); 
    //equipment control tags are gone
    assertFalse(controlTagCache.hasKey(1222L));
    assertNull(controlTagMapper.getItem(1222L));
    assertFalse(controlTagCache.hasKey(1223L));
    assertNull(controlTagMapper.getItem(1223L)); 
    //equipment commfault is gone
    assertFalse(commFaultTagCache.hasKey(1223L));
    //process alive is still here (may be active or not, depending on status when removed)
    assertTrue(aliveTimerCache.hasKey(1221L));
    //alarms all gone
    assertFalse(alarmCache.hasKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(alarmCache.hasKey(350001L));
    assertNull(alarmMapper.getItem(350001L));
    
    mockControl.verify();
  }
  
  @Override
  public void setApplicationContext(ApplicationContext arg0) throws BeansException {
    context = arg0;
  }
}
