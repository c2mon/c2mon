package cern.c2mon.server.supervision.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.server.cache.CacheProvider;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.supervision.SupervisionAppender;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * Unit test of SupervisionTagNotifier class.
 * 
 * @author Mark Brightwell
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/supervision/config/server-supervision-integration.xml" })
public class SupervisionTagNotifierTest {

  /**
   * Class to test.
   */
  private SupervisionTagNotifier supervisionTagNotifier;
 
  /**
   * Mocks
   */
  private IMocksControl mockControl = EasyMock.createControl();
  private SupervisionNotifier supervisionNotifier;
  private CacheProvider cacheProvider;
  private ProcessCache processCache;
  private EquipmentCache equipmentCache;
  private TagLocationService tagLocationService;
  private DataTagCache dataTagCache;
  private RuleTagCache ruleTagCache;
  private EquipmentFacade equipmentFacade;
  private SupervisionAppender supervisionAppender;
  private ProcessFacade processFacade;
  @Autowired
  private ClusterCache clusterCache;
  
  @Autowired
  @Qualifier("processEventCache")
  private C2monCache<Long, SupervisionEvent> processEventCache;
  @Autowired
  @Qualifier("equipmentEventCache")
  private C2monCache<Long, SupervisionEvent> equipmentEventCache;
  
  /**
   * Data objects.
   */
  private ProcessCacheObject process;
  private EquipmentCacheObject equipment;
  private DataTagCacheObject dataTag;
  private DataTagCacheObject dataTag2;
  private RuleTagCacheObject ruleTag;
  private RuleTagCacheObject ruleTag2;
  private RuleTagCacheObject ruleTag3;
  
  @Before
  public void setUp() {
    supervisionNotifier = mockControl.createMock(SupervisionNotifier.class);
    cacheProvider = mockControl.createMock(CacheProvider.class);
    processCache = mockControl.createMock(ProcessCache.class);
    equipmentCache = mockControl.createMock(EquipmentCache.class);
    tagLocationService = mockControl.createMock(TagLocationService.class);
    dataTagCache = mockControl.createMock(DataTagCache.class);
    ruleTagCache = mockControl.createMock(RuleTagCache.class);    
    equipmentFacade = mockControl.createMock(EquipmentFacade.class);
    supervisionAppender = mockControl.createMock(SupervisionAppender.class);
    processFacade = mockControl.createMock(ProcessFacade.class);
    
    EasyMock.expect(cacheProvider.getProcessCache()).andReturn(processCache);
    EasyMock.expect(cacheProvider.getEquipmentCache()).andReturn(equipmentCache);
    EasyMock.expect(cacheProvider.getDataTagCache()).andReturn(dataTagCache);
    EasyMock.expect(cacheProvider.getRuleTagCache()).andReturn(ruleTagCache);
    EasyMock.expect(cacheProvider.getClusterCache()).andReturn(clusterCache);
    
    EasyMock.replay(cacheProvider);
    
    supervisionTagNotifier = new SupervisionTagNotifier(supervisionNotifier, cacheProvider,
                                                   tagLocationService, supervisionAppender, processFacade,
                                                   equipmentFacade, processEventCache, equipmentEventCache);
    
    EasyMock.reset(cacheProvider);
    process = new ProcessCacheObject(10L);    
    process.setEquipmentIds(new ArrayList<Long>(Arrays.asList(30L)));
    equipment = new EquipmentCacheObject(30L);
    equipment.setDataTagIds(new LinkedList<Long>(Arrays.asList(100L, 101L)));
    dataTag = new DataTagCacheObject(100L);
    dataTag.setRuleIds(new ArrayList<Long>(Arrays.asList(200L, 201L)));
    dataTag.setEquipmentId(30L);
    dataTag.setProcessId(10L);
    dataTag2 = new DataTagCacheObject(101L);
    dataTag2.setRuleIds(new ArrayList<Long>(Arrays.asList(200L, 202L)));
    dataTag2.setEquipmentId(30L);
    dataTag2.setProcessId(10L);
    ruleTag = new RuleTagCacheObject(200L);
    Set<Long> eqIds = new HashSet<Long>();
    eqIds.add(30L);
    Set<Long> procIds = new HashSet<Long>();
    procIds.add(10L);
    ruleTag.setEquipmentIds(eqIds);
    ruleTag.setProcessIds(procIds);
    ruleTag2 = new RuleTagCacheObject(201L); 
    ruleTag2.setEquipmentIds(eqIds);
    ruleTag2.setProcessIds(procIds);    
    ruleTag3 = new RuleTagCacheObject(202L);
    ruleTag3.setEquipmentIds(eqIds);
    ruleTag3.setProcessIds(procIds);    
  }

  /**
   * Test init call.
   */
  public void testRegistration() {
    supervisionNotifier.registerAsListener(supervisionTagNotifier);
    
    mockControl.replay();
    
    supervisionTagNotifier.init();
    
    mockControl.verify();
  }
  
  /**
   * Tests notifySupervisionEvent for a process event.
   */
  @Test
  @DirtiesContext
  public void testNotifyProcessEvent() {
    SupervisionEvent event = new SupervisionEventImpl(SupervisionEntity.PROCESS, 10L, SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()), "test message");

    EasyMock.expect(processCache.getCopy(10L)).andReturn(process);
    //EasyMock.expect(equipmentFacade.getProcessForAbstractEquipment(30L)).andReturn(process);
    EasyMock.expect(equipmentCache.getCopy(30L)).andReturn(equipment); 
    EasyMock.expect(tagLocationService.getCopy(100L)).andReturn(dataTag);
    EasyMock.expect(tagLocationService.getCopy(101L)).andReturn(dataTag2);
    EasyMock.expect(tagLocationService.getCopy(200L)).andReturn(ruleTag).times(2);
    EasyMock.expect(tagLocationService.getCopy(201L)).andReturn(ruleTag2);
    EasyMock.expect(tagLocationService.getCopy(202L)).andReturn(ruleTag3);
    supervisionAppender.addSupervisionQuality(dataTag, event);
    dataTagCache.notifyListenersOfSupervisionChange(dataTag);
    supervisionAppender.addSupervisionQuality(dataTag2, event);
    dataTagCache.notifyListenersOfSupervisionChange(dataTag2);
    supervisionAppender.addSupervisionQuality(ruleTag,event);
    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag); //only once although uses triggered by 2 different tags
    supervisionAppender.addSupervisionQuality(ruleTag2,event);
    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag2);
    supervisionAppender.addSupervisionQuality(ruleTag3,event);
    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag3);
        
    mockControl.replay();
    
    supervisionTagNotifier.notifySupervisionEvent(event);
     
    mockControl.verify();
  }
  
  /**
   * Tests notifySupervisionEvent for and equipment event.
   */
  @Test
  @DirtiesContext
  public void testNotifyEquipmentEvent() { 
    SupervisionEvent event = new SupervisionEventImpl(SupervisionEntity.EQUIPMENT, 30L, SupervisionStatus.RUNNING, new Timestamp(System.currentTimeMillis()), "test message");
    mockControl.reset();
    //EasyMock.expect(equipmentFacade.getProcessForAbstractEquipment(30L)).andReturn(process);
    EasyMock.expect(equipmentCache.getCopy(30L)).andReturn(equipment);
    EasyMock.expect(tagLocationService.getCopy(100L)).andReturn(dataTag);
    EasyMock.expect(tagLocationService.getCopy(101L)).andReturn(dataTag2);
    EasyMock.expect(tagLocationService.getCopy(200L)).andReturn(ruleTag).times(2);
    EasyMock.expect(tagLocationService.getCopy(201L)).andReturn(ruleTag2);
    EasyMock.expect(tagLocationService.getCopy(202L)).andReturn(ruleTag3);
    supervisionAppender.addSupervisionQuality(dataTag,event);
    dataTagCache.notifyListenersOfSupervisionChange(dataTag);
    supervisionAppender.addSupervisionQuality(dataTag2,event);
    dataTagCache.notifyListenersOfSupervisionChange(dataTag2);
    supervisionAppender.addSupervisionQuality(ruleTag,event);
    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag); //only once although uses triggered by 2 different tags
    supervisionAppender.addSupervisionQuality(ruleTag2,event);
    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag2);
    supervisionAppender.addSupervisionQuality(ruleTag3,event);
    ruleTagCache.notifyListenersOfSupervisionChange(ruleTag3);
        
    mockControl.replay();
    
    supervisionTagNotifier.notifySupervisionEvent(event);
     
    mockControl.verify();
  }
}
