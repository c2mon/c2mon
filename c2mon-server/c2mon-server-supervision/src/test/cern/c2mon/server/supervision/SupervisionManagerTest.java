package cern.c2mon.server.supervision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.cache.CacheSupervisionListener;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.supervision.impl.SupervisionTagNotifier;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

/**
 * Integration test of supervision module with core cache modules.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/supervision/config/server-supervision-integration.xml" })
public class SupervisionManagerTest {

  
  @Autowired
  private SupervisionManager supervisionManager;
  
  @Autowired
  private ControlTagCache controlTagCache;
  
  @Autowired
  private AliveTimerCache aliveTimerCache;
  
  @Autowired
  private ProcessCache processCache;
  
  @Autowired
  private EquipmentCache equipmentCache;
  
  @Autowired
  private SupervisionNotifier supervisionNotifier;
  
  @Autowired
  private SupervisionTagNotifier supervisionTagNotifier;
  
  @Autowired
  private CacheRegistrationService cacheRegistrationService;
  
  /**
   * Mock listeners registered for supervision events &
   * tag callbacks.
   */
  private SupervisionListener supervisionListener;
  private CacheSupervisionListener<Tag> cacheSupervisionListener;
  
  private IMocksControl controller;
  
  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    controller = EasyMock.createControl();
    supervisionListener = controller.createMock(SupervisionListener.class);
    supervisionNotifier.registerAsListener(supervisionListener);
    cacheSupervisionListener = controller.createMock(CacheSupervisionListener.class);
    cacheRegistrationService.registerForSupervisionChanges(cacheSupervisionListener);       
  }
  
  /**
   * Tests a process alive tag is correctly processed by the SupervisionManager
   * (alive timer updated; supervision listeners notified, etc).
   * 
   * Process is down at start of test, then alive is received.
   * 
   * @throws InterruptedException 
   */
  @Test
  public void testProcessAliveTag() throws InterruptedException {    
    supervisionListener.notifySupervisionEvent(EasyMock.isA(SupervisionEvent.class));
    cacheSupervisionListener.onSupervisionChange(EasyMock.isA(Tag.class));
    EasyMock.expectLastCall().times(6);
    
    controller.replay();
    
    //check initial status is correct
    AliveTimer aliveTimer = aliveTimerCache.getCopy(1221L);
    assertNotNull(aliveTimer);
    assertEquals(0, aliveTimer.getLastUpdate());
    Process process = processCache.getCopy(aliveTimer.getRelatedId());
    assertEquals(SupervisionStatus.DOWN, process.getSupervisionStatus());
    assertEquals(null, process.getStatusTime());
    assertEquals(null, process.getStatusDescription());
    Tag stateTag = controlTagCache.getCopy(1220L);
    assertEquals(null, stateTag.getValue());
    
    long updateTime = System.currentTimeMillis(); 
    //process control tag
    supervisionManager.processControlTag(new SourceDataTagValue(1221L, 
        "test alive", true, 0L, new SourceDataQuality(), new Timestamp(updateTime), 4, false, "description", 10000));
    
    //check alive is updated
    aliveTimer = aliveTimerCache.getCopy(1221L);
    assertNotNull(aliveTimer.getLastUpdate());
    assertTrue(aliveTimer.getLastUpdate() > System.currentTimeMillis() - 10000); //account for non-synchronized
    
    //check process status is changed
    process = processCache.getCopy(aliveTimer.getRelatedId());
    assertEquals(SupervisionStatus.RUNNING, process.getSupervisionStatus());
    Timestamp processTime = process.getStatusTime(); 
    assertTrue(processTime.after(new Timestamp(updateTime - 1)));
    assertNotNull(process.getStatusDescription());
    
    //check tags are updated (note alive tag is not updated; this is done in SourceUpdateManager)
    stateTag = controlTagCache.getCopy(1220L);
    assertEquals(SupervisionStatus.RUNNING.toString(), stateTag.getValue());
    assertEquals(processTime, stateTag.getCacheTimestamp());
    
    Thread.sleep(2000); //wait for notification on listener thread
    controller.verify();    
  }
  
  /**
   * Alives older than 2 minutes are rejected.
   */
  @Test
  public void testRejectOldAlive() {
    //check alive timer is defined & set last update
    AliveTimer aliveTimer = aliveTimerCache.getCopy(1221L);
    assertNotNull(aliveTimer);
    aliveTimer.setLastUpdate(System.currentTimeMillis()-1000);    
    long aliveTime = aliveTimer.getLastUpdate();
    //send alive 2 minutes old (should be rejected)
    SourceDataTagValue value = new SourceDataTagValue(1221L, 
        "test alive", true, 0L, new SourceDataQuality(), new Timestamp(System.currentTimeMillis()), 4, false, "description", 10000);
    value.setDaqTimestamp(new Timestamp(System.currentTimeMillis() - 130000));
    supervisionManager.processControlTag(value);
    
    //no update
    assertEquals(aliveTime, aliveTimer.getLastUpdate());        
  }
  
  /**
   * Checks a new process alive has no affect on the state tag or on the process
   * status, since it is already down as running. Only the alive is updated.
   * @throws InterruptedException 
   */
  @Test
  @DirtiesContext
  public void testProcessAliveNoAffect() throws InterruptedException {
    controller.reset();    
    controller.replay(); //no listener call this time
    
    //check initial status is correct
    AliveTimer aliveTimer = aliveTimerCache.getCopy(1221L);
    assertNotNull(aliveTimer);
    long aliveTime = aliveTimer.getLastUpdate();
    Process process = processCache.getCopy(aliveTimer.getRelatedId());
    assertEquals(SupervisionStatus.RUNNING, process.getSupervisionStatus());
    Timestamp originalProcessTime = process.getStatusTime();
    assertNotNull(originalProcessTime);
    assertNotNull(process.getStatusDescription());
    Tag stateTag = controlTagCache.getCopy(1220L);
    assertEquals(SupervisionStatus.RUNNING.toString(), stateTag.getValue());
    
    long updateTime = System.currentTimeMillis(); 
    //process control tag
    supervisionManager.processControlTag(new SourceDataTagValue(1221L, 
        "test alive", true, 0L, new SourceDataQuality(), new Timestamp(updateTime), 4, false, "description", 10000));
    
    //check alive is updated
    assertNotNull(aliveTimer.getLastUpdate());
    assertTrue(aliveTimer.getLastUpdate() > aliveTime - 1);
    
    //check process status is not changed & time also
    assertEquals(SupervisionStatus.RUNNING, process.getSupervisionStatus());
    Timestamp processTime = process.getStatusTime(); 
    assertEquals(originalProcessTime, processTime);
    assertNotNull(process.getStatusDescription());
    
    //check tags are updated (note alive tag is not updated; this is done in SourceUpdateManager)
    assertEquals(SupervisionStatus.RUNNING.toString(), stateTag.getValue());
    assertEquals(originalProcessTime, stateTag.getCacheTimestamp());
    
    Thread.sleep(2000); //wait for notification on listener thread
    controller.verify(); //expect one call on the supervision listener 
  }
  
  /**
   * Tests the processing of a commfault and its consequences on the
   * Equipment status and registered listeners.
   * 
   * <p>Send 2 commfault tags, one FALSE indicating Equipment DOWN then
   * one TRUE indicating Equipment UP.
   * 
   * @throws InterruptedException 
   */
  @Test
  @DirtiesContext  
  public void testCommFaultTag() throws InterruptedException {
    //(1) Send CommFaultTag TRUE
    supervisionListener.notifySupervisionEvent(EasyMock.isA(SupervisionEvent.class));
    cacheSupervisionListener.onSupervisionChange(EasyMock.isA(Tag.class));
    EasyMock.expectLastCall().times(6);
    
    Equipment equipment = equipmentCache.getCopy(150L);
    assertEquals(equipment.getSupervisionStatus(), SupervisionStatus.DOWN);
    Timestamp originalTime = new Timestamp(System.currentTimeMillis() - 1000);
    equipment.setStatusTime(originalTime);
    String originalDescription = "initial description";
    equipment.setStatusDescription(originalDescription);   
    assertTrue(controlTagCache.getCopy(equipment.getStateTagId()).getValue() == null);
    
    controller.replay();
   
    long updateTime = System.currentTimeMillis();
    supervisionManager.processControlTag(new SourceDataTagValue(1223L, 
        "test commfault", true, Boolean.TRUE, new SourceDataQuality(), new Timestamp(updateTime), 4, false, "description", 10000));
    //wait for Tag callback thread    
    Thread.sleep(2000);
    
    controller.verify();
    
    //check equipment status & state tag have changed
    equipment = equipmentCache.getCopy(150L);
    assertEquals(equipment.getSupervisionStatus(), SupervisionStatus.RUNNING);
    Timestamp secondTime = equipment.getStatusTime();
    assertFalse(originalTime.equals(secondTime));
    String secondDescription = equipment.getStatusDescription();
    assertFalse(originalDescription.equals(secondDescription));
    assertEquals(SupervisionStatus.RUNNING.toString(), controlTagCache.getCopy(equipment.getStateTagId()).getValue());
    
    //(2) Send CommFaultTag FALSE
    controller.reset();
    supervisionListener.notifySupervisionEvent(EasyMock.isA(SupervisionEvent.class));
    cacheSupervisionListener.onSupervisionChange(EasyMock.isA(Tag.class));
    EasyMock.expectLastCall().times(6);
    
    controller.replay();
    
    long updateTime2 = System.currentTimeMillis();
    supervisionManager.processControlTag(new SourceDataTagValue(1223L, 
        "test commfault", true, Boolean.FALSE, new SourceDataQuality(), new Timestamp(updateTime2), 4, false, "description", 10000));
    Thread.sleep(2000);
    
    controller.verify();
    equipment = equipmentCache.getCopy(150L);
    //check equipment status & state tag have changed
    assertEquals(equipment.getSupervisionStatus(), SupervisionStatus.DOWN);
    assertFalse(secondTime.equals(equipment.getStatusTime()));
    assertFalse(secondDescription.equals(equipment.getStatusDescription()));
    assertEquals(SupervisionStatus.DOWN.toString(), controlTagCache.getCopy(equipment.getStateTagId()).getValue());
  }
  
}
