package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.IgniteModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.factory.SupervisionStateTagFactory;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

/**
 * For test purposes, EquipmentCacheObject or ProcessCacheObject will be used.
 *
 * @author Szymon Halastra
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  CacheLoadingModuleRef.class,
  IgniteModule.class,
  CacheActionsModuleRef.class
})
public class SupervisedCacheServiceTest {

  @Inject private EquipmentService equipmentService;
  @Inject private C2monCache<Equipment> equipmentCache;

  @Inject private ProcessService processService;
  @Inject private C2monCache<Process> processCache;

  @Inject private SupervisionStateTagService stateTagService;

  @Before
  public void before() {
    equipmentCache.clear();
    processCache.clear();
    stateTagService.getCache().clear();
  }

  @Test
  public void startDownEquipment() {
    EquipmentCacheObject equipment = getCachedEquipment(SupervisionStatus.DOWN);

    long timestamp = currentTimeMillis();
    equipmentService.start(equipment.getId(), timestamp);

    SupervisionEvent event = stateTagService.getSupervisionEvent(equipment.getStateTagId());
    assertEquals(SupervisionStatus.STARTUP, event.getStatus());
    assertNotNull(event.getEventTime());
    assertEquals(timestamp, event.getEventTime().getTime());
    assertTrue(event.getMessage().contains("was started"));
  }

  @Test
  public void stopDownEquipment() {
    EquipmentCacheObject equipment = getCachedEquipment(SupervisionStatus.DOWN);

    long timestamp = currentTimeMillis();
    equipmentService.stop(equipment.getId(), timestamp);

    SupervisionEvent event = stateTagService.getSupervisionEvent(equipment.getStateTagId());
    assertEquals(SupervisionStatus.DOWN, event.getStatus());
    assertEquals(0, event.getEventTime().getTime());
    assertEquals("", event.getMessage());
  }

  @Test
  public void resumeRunningEquipment() {
    EquipmentCacheObject equipment = getCachedEquipment(SupervisionStatus.RUNNING);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    equipmentService.resume(equipment.getId(), timestamp, message);

    SupervisionEvent event = stateTagService.getSupervisionEvent(equipment.getStateTagId());
    assertSame(SupervisionStatus.RUNNING, event.getStatus());
    assertEquals(0, event.getEventTime().getTime());
    assertEquals("", event.getMessage());
  }

  @Test
  public void resumeDownEquipment() {
    EquipmentCacheObject equipment = getCachedEquipment(SupervisionStatus.DOWN);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    equipmentService.resume(equipment.getId(), timestamp, message);

    SupervisionEvent event = stateTagService.getSupervisionEvent(equipment.getStateTagId());
    assertSame(SupervisionStatus.RUNNING, event.getStatus());
    assertNotNull(event.getEventTime());
    assertEquals(timestamp, event.getEventTime().getTime());
    assertEquals(message, event.getMessage());
  }

  @Test
  public void suspendRunningEquipment() {
    EquipmentCacheObject equipment = getCachedEquipment(SupervisionStatus.RUNNING);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    equipmentService.suspend(equipment.getId(), timestamp, message);

    SupervisionEvent event = stateTagService.getSupervisionEvent(equipment.getStateTagId());
    assertEquals(SupervisionStatus.DOWN, event.getStatus());
    assertNotNull(event.getEventTime());
    assertEquals(timestamp, event.getEventTime().getTime());
    assertEquals(message, event.getMessage());
  }

  @Test
  public void suspendUncertainEquipment() {
    EquipmentCacheObject equipment = getCachedEquipment(SupervisionStatus.UNCERTAIN);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    equipmentService.suspend(equipment.getId(), timestamp, message);

    SupervisionEvent event = stateTagService.getSupervisionEvent(equipment.getStateTagId());
    assertEquals(SupervisionStatus.DOWN, event.getStatus());
    assertNotNull(event.getEventTime());
    assertEquals(timestamp, event.getEventTime().getTime());
    assertEquals(message, event.getMessage());
  }

  @Test
  public void suspendDownEquipment() {
    EquipmentCacheObject equipment = getCachedEquipment(SupervisionStatus.DOWN);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    equipmentService.suspend(equipment.getId(), timestamp, message);

    SupervisionEvent event = stateTagService.getSupervisionEvent(equipment.getStateTagId());
    assertEquals(SupervisionStatus.DOWN, event.getStatus());
    assertEquals(0, event.getEventTime().getTime());
    assertEquals("", event.getMessage());
  }

  @Test
  public void resumeDownProcess() {
    ProcessCacheObject process = apply(getCachedProcess(SupervisionStatus.DOWN), p -> {
      processService.setLocalConfig(p.getId(), ProcessCacheObject.LocalConfig.Y);
    });

    long timestamp = currentTimeMillis();
    String message = "test-message";
    processService.resume(process.getId(), timestamp, message);

    SupervisionEvent event = stateTagService.getSupervisionEvent(process.getStateTagId());
    assertSame(SupervisionStatus.RUNNING_LOCAL, event.getStatus());
    assertNotNull(event.getEventTime());
    assertEquals(timestamp, event.getEventTime().getTime());
    assertEquals(message, event.getMessage());
  }

  private EquipmentCacheObject getCachedEquipment(SupervisionStatus supervisionStatus) {
    SupervisionStateTag stateTag = new SupervisionStateTagFactory().sampleBase();
    stateTag.setSupervisionStatus(supervisionStatus);
    stateTagService.getCache().put(stateTag.getId(), stateTag);

    EquipmentCacheObject equipment = new EquipmentCacheObject(101010L);
    equipment.setStateTagId(stateTag.getId());
    equipmentCache.put(equipment.getId(), equipment);

    return equipment;
  }

  private ProcessCacheObject getCachedProcess(SupervisionStatus supervisionStatus) {
    SupervisionStateTag stateTag = new SupervisionStateTagFactory().ofProcess();
    stateTag.setSupervisionStatus(supervisionStatus);
    stateTagService.getCache().put(stateTag.getId(), stateTag);

    ProcessCacheObject process = new ProcessCacheObject(202020L);
    process.setStateTagId(stateTag.getId());
    processCache.put(process.getId(), process);

    return process;
  }
}
