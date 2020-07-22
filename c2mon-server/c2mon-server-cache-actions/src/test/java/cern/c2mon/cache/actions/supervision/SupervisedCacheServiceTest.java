package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.factory.SupervisionStateTagFactory;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.sql.Timestamp;

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
  C2monIgniteConfiguration.class,
  CacheActionsModuleRef.class
})
public class SupervisedCacheServiceTest {

  @Inject private EquipmentService equipmentService;
  @Inject private C2monCache<Equipment> equipmentCache;

  @Inject private ProcessService processService;
  @Inject private C2monCache<Process> processCache;

  @Inject private SupervisionStateTagService stateTagService;

  private SupervisionStateTag stateTag;

  @Before
  public void before() {
    stateTag = new SupervisionStateTagFactory().ofProcess();
    stateTagService.getCache().put(stateTag.getId(), stateTag);
  }

  @Test
  public void getLastSupervisionEvent() {
    EquipmentCacheObject equipment = apply(getEquipment(SupervisionStatus.RUNNING), e -> {
      e.setName("test");
      e.setStatusTime(new Timestamp(currentTimeMillis()));
      e.setStatusDescription("description");
    });
    equipmentCache.put(equipment.getId(), equipment);

    SupervisionEvent supervisionEvent = stateTagService.getSupervisionEvent(equipment.getStateTagId());
    assertEquals(supervisionEvent.getEntityId(), equipment.getId());
    assertEquals(supervisionEvent.getName(), equipment.getName());
    assertEquals(supervisionEvent.getStatus(), equipment.getSupervisionStatus());
    assertEquals(supervisionEvent.getEventTime(), equipment.getStatusTime());
    assertEquals(supervisionEvent.getMessage(), equipment.getStatusDescription());
  }

  @Test
  @Ignore
  public void getLastSupervisionEventNullValues() {
    EquipmentCacheObject equipment = apply(getEquipment(SupervisionStatus.RUNNING), e -> {
      e.setName("test");
      e.setStatusTime(null);
      e.setStatusDescription(null);
    });
    equipmentCache.put(equipment.getId(), equipment);

    SupervisionEvent supervisionEvent = stateTagService.getSupervisionEvent(equipment.getStateTagId());
    assertEquals(supervisionEvent.getEntityId(), equipment.getId());
    assertEquals(supervisionEvent.getName(), equipment.getName());
    assertEquals(supervisionEvent.getStatus(), equipment.getSupervisionStatus());
    assertNotNull(supervisionEvent.getEventTime());
    assertNotNull(supervisionEvent.getMessage());
  }

  @Test
  public void startDownEquipment() {
    EquipmentCacheObject equipment = getEquipment(SupervisionStatus.DOWN);
    equipmentCache.put(equipment.getId(), equipment);

    long timestamp = currentTimeMillis();
    equipmentService.start(equipment.getId(), timestamp);

    assertEquals(SupervisionStatus.STARTUP, getSupervisionStatus(equipment));
    assertEquals(SupervisionStatus.STARTUP, equipment.getSupervisionStatus());
    assertNotNull(equipment.getStatusTime());
    assertTrue(equipment.getStatusDescription().contains("was started"));
  }

  @Test
  public void stopDownEquipment() {
    EquipmentCacheObject equipment = getEquipment(SupervisionStatus.DOWN);
    equipmentCache.put(equipment.getId(), equipment);

    long timestamp = currentTimeMillis();
    equipmentService.stop(equipment.getId(), timestamp);

    assertEquals(SupervisionStatus.DOWN, getSupervisionStatus(equipment));
    assertEquals(SupervisionStatus.DOWN, equipment.getSupervisionStatus());
    assertNotNull(equipment.getStatusTime());
    assertTrue(equipment.getStatusDescription().contains("was stopped"));
  }

  @Test
  public void resumeRunningEquipment() {
    EquipmentCacheObject equipment = getEquipment(SupervisionStatus.RUNNING);
    equipmentCache.put(equipment.getId(), equipment);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    equipmentService.resume(equipment.getId(), timestamp, message);

    assertSame(SupervisionStatus.RUNNING, getSupervisionStatus(equipment));
    assertSame(SupervisionStatus.RUNNING, equipment.getSupervisionStatus());
    assertNull(equipment.getStatusTime());
    assertNull(equipment.getStatusDescription());
  }

  @Test
  public void resumeDownEquipment() {
    EquipmentCacheObject equipment = getEquipment(SupervisionStatus.DOWN);
    equipmentCache.put(equipment.getId(), equipment);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    equipmentService.resume(equipment.getId(), timestamp, message);

    assertSame(SupervisionStatus.RUNNING, getSupervisionStatus(equipment));
    assertSame(SupervisionStatus.RUNNING, equipment.getSupervisionStatus());
    assertEquals(timestamp, equipment.getStatusTime().getTime());
    assertEquals(message, equipment.getStatusDescription());
  }

  @Test
  public void suspendRunningEquipment() {
    EquipmentCacheObject equipment = getEquipment(SupervisionStatus.RUNNING);
    equipmentCache.put(equipment.getId(), equipment);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    equipmentService.suspend(equipment.getId(), timestamp, message);

    assertEquals(SupervisionStatus.DOWN, getSupervisionStatus(equipment));
    assertEquals(SupervisionStatus.DOWN, equipment.getSupervisionStatus());
    assertEquals(timestamp, equipment.getStatusTime().getTime());
    assertEquals(message, equipment.getStatusDescription());
  }

  @Test
  public void suspendUncertainEquipment() {
    EquipmentCacheObject equipment = getEquipment(SupervisionStatus.UNCERTAIN);
    equipmentCache.put(equipment.getId(), equipment);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    equipmentService.suspend(equipment.getId(), timestamp, message);

    assertEquals(SupervisionStatus.DOWN, getSupervisionStatus(equipment));
    assertEquals(SupervisionStatus.DOWN, equipment.getSupervisionStatus());
    assertEquals(timestamp, equipment.getStatusTime().getTime());
    assertEquals(message, equipment.getStatusDescription());
  }

  @Test
  public void suspendDownEquipment() {
    EquipmentCacheObject equipment = getEquipment(SupervisionStatus.DOWN);
    equipmentCache.put(equipment.getId(), equipment);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    equipmentService.suspend(equipment.getId(), timestamp, message);

    assertEquals(SupervisionStatus.DOWN, getSupervisionStatus(equipment));
    assertEquals(SupervisionStatus.DOWN, equipment.getSupervisionStatus());
    assertNull(equipment.getStatusTime());
    assertNull(equipment.getStatusDescription());
  }

  @Test
  public void resumeDownProcess() {
    ProcessCacheObject process = apply(getProcess(SupervisionStatus.DOWN), p -> {
      p.setLocalConfig(ProcessCacheObject.LocalConfig.Y);
    });
    processCache.put(process.getId(), process);

    long timestamp = currentTimeMillis();
    String message = "test-message";
    processService.resume(process.getId(), timestamp, message);

    assertSame(SupervisionStatus.RUNNING_LOCAL, getSupervisionStatus(process));
    assertSame(SupervisionStatus.RUNNING_LOCAL, process.getSupervisionStatus());
    assertEquals(timestamp, process.getStatusTime().getTime());
    assertEquals(message, process.getStatusDescription());
  }

  private EquipmentCacheObject getEquipment(SupervisionStatus supervisionStatus) {
    EquipmentCacheObject e = new EquipmentCacheObject(101010L);
    e.setSupervisionStatus(supervisionStatus);
    e.setStateTagId(stateTag.getId());
    return e;
  }

  private ProcessCacheObject getProcess(SupervisionStatus supervisionStatus) {
    ProcessCacheObject p = new ProcessCacheObject(202020L);
    p.setSupervisionStatus(supervisionStatus);
    p.setStateTagId(stateTag.getId());
    return p;
  }

  private SupervisionStatus getSupervisionStatus(Supervised supervised) {
    return stateTagService.getSupervisionEvent(supervised.getStateTagId()).getStatus();
  }
}
