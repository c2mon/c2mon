package cern.c2mon.cache;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cern.c2mon.cache.api.AbstractCache;
import cern.c2mon.cache.api.service.SupervisedService;
import cern.c2mon.server.cache.SupervisedServiceImpl;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

/**
 * For test purposes I will be using EquipmentCacheObject implementation or ProcessCacheObject
 *
 * @author Szymon Halastra
 */
public class SupervisedServiceTest {

  private AbstractCache c2monCache;
  private AliveTimerService aliveTimerService;

  private SupervisedService<Supervised> supervisedService;

  @Before
  public void init() {
    c2monCache = EasyMock.createNiceMock(AbstractCache.class);
    aliveTimerService = EasyMock.createNiceMock(AliveTimerService.class);

    supervisedService = new SupervisedServiceImpl<>(c2monCache, aliveTimerService);
  }

  @Test
  @Ignore
  public void getLastOccurredSupervisionEventWithStatusTimeAndStatusDescription() {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String statusDescription = "description";
    String name = "test";

    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);
    equipment.setName(name);
    equipment.setStatusTime(timestamp);
    equipment.setStatusDescription(statusDescription);
    equipment.setSupervisionStatus(SupervisionConstants.SupervisionStatus.RUNNING);

    prepareMock(equipment);

    SupervisionEvent supervisionEvent = supervisedService.getSupervisionStatus(equipment.getId());

    assertEquals("SupervisionEvent should have the same id", supervisionEvent.getEntityId(), equipment.getId());
    assertEquals("SupervisionEvent should have the same name", supervisionEvent.getName(), equipment.getName());
    assertEquals("SupervisionEvent should have the same supervision status", supervisionEvent.getStatus(), equipment.getSupervisionStatus());
    assertEquals("SupervisionEvent should have the same supervision time", supervisionEvent.getEventTime(), equipment.getStatusTime());
    assertEquals("SupervisionEvent should have the same supervision message", supervisionEvent.getMessage(), equipment.getStatusDescription());
  }

  @Test
  @Ignore
  public void getLastOccurredSupervisionEventWithNullStatusTimeAndNullStatusDescription() {
    String name = "test";

    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);
    equipment.setName(name);
    equipment.setStatusTime(null);
    equipment.setStatusDescription(null);
    equipment.setSupervisionStatus(SupervisionConstants.SupervisionStatus.RUNNING);

    prepareMock(equipment);

    SupervisionEvent supervisionEvent = supervisedService.getSupervisionStatus(equipment.getId());

    assertEquals("SupervisionEvent should have the same id", supervisionEvent.getEntityId(), equipment.getId());
    assertEquals("SupervisionEvent should have the same name", supervisionEvent.getName(), equipment.getName());
    assertEquals("SupervisionEvent should have the same supervision status", supervisionEvent.getStatus(), equipment.getSupervisionStatus());
    assertNotNull("SupervisionEvent should have not null supervision time", supervisionEvent.getEventTime());
    assertNotNull("SupervisionEvent should have not null supervision message", supervisionEvent.getMessage());
  }

  @Test
  @Ignore
  public void startSupervisionWithSupervisedId() {
    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    prepareMock(equipment);

    supervisedService.start(equipment.getId(), timestamp);

    assertEquals("Supervised should have STARTUP status", equipment.getSupervisionStatus(), SupervisionConstants.SupervisionStatus.STARTUP);
    assertTrue("Supervised should have information about start in status description", equipment.getStatusDescription().contains("was started"));
    assertTrue("Supervised should have timestamp set", equipment.getStatusTime() != null);
  }

  @Test
  @Ignore
  public void stopSupervisionWithSupervisedId() {
    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    prepareMock(equipment);

    supervisedService.stop(equipment.getId(), timestamp);

    assertEquals("Supervised should stop with DOWN status", equipment.getSupervisionStatus(), SupervisionConstants.SupervisionStatus.DOWN);
    assertTrue("Supervised should have timestamp set", equipment.getStatusTime() != null);
    assertTrue("Supervised should have information about stop in status description", equipment.getStatusDescription().contains("was stopped"));
  }

  @Test
  public void resumeSupervisedWhenRunning() {
    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);

    equipment.setSupervisionStatus(SupervisionConstants.SupervisionStatus.RUNNING);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String message = "test-message";

    prepareMock(equipment);

    supervisedService.resume(equipment.getId(), timestamp, message);

    assertTrue("Supervised should not have status time set", equipment.getStatusTime() == null);
    assertTrue("Supervised should not have status description set", equipment.getStatusDescription() == null);
    assertTrue("Supervised should have RUNNING status set", equipment.getSupervisionStatus() == SupervisionConstants.SupervisionStatus.RUNNING);
  }

  @Test
  @Ignore
  public void resumeSupervisedWhenNotRunningAndNotProcess() {
    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);

    equipment.setSupervisionStatus(SupervisionConstants.SupervisionStatus.DOWN);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String message = "test-message";

    prepareMock(equipment);

    supervisedService.resume(equipment.getId(), timestamp, message);

    assertEquals("Supervised should have status time set", equipment.getStatusTime(), timestamp);
    assertEquals("Supervised should have status description set", equipment.getStatusDescription(), message);
    assertTrue("Supervised should have RUNNING status set", equipment.getSupervisionStatus() == SupervisionConstants.SupervisionStatus.RUNNING);
  }

  @Test
  @Ignore
  public void resumeSupervisedWhenNotRunningAndProcess() {
    ProcessCacheObject process = new ProcessCacheObject(1L);

    process.setSupervisionStatus(SupervisionConstants.SupervisionStatus.DOWN);
    process.setLocalConfig(ProcessCacheObject.LocalConfig.Y);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String message = "test-message";

    prepareMock(process);

    supervisedService.resume(process.getId(), timestamp, message);

    assertEquals("Supervised should not have status time set", process.getStatusTime(), timestamp);
    assertEquals("Supervised should not have status description set", process.getStatusDescription(), message);
    assertTrue("Supervised should have RUNNING_LOCAL status set", process.getSupervisionStatus() == SupervisionConstants.SupervisionStatus.RUNNING_LOCAL);
  }

  @Test
  @Ignore
  public void suspendWhenIsRunning() {
    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);
    equipment.setSupervisionStatus(SupervisionConstants.SupervisionStatus.RUNNING);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String message = "test-message";

    prepareMock(equipment);

    supervisedService.suspend(equipment.getId(), timestamp, message);

    assertEquals("Supervised should have DOWN status", equipment.getSupervisionStatus(), SupervisionConstants.SupervisionStatus.DOWN);
    assertEquals("Supervised should have status time set", equipment.getStatusTime(), timestamp);
    assertEquals("Supervised should have status description set", equipment.getStatusDescription(), message);
  }

  @Test
  @Ignore
  public void suspendWhenIsUncertain() {
    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);
    equipment.setSupervisionStatus(SupervisionConstants.SupervisionStatus.UNCERTAIN);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String message = "test-message";

    prepareMock(equipment);

    supervisedService.suspend(equipment.getId(), timestamp, message);

    assertEquals("Supervised should have DOWN status", equipment.getSupervisionStatus(), SupervisionConstants.SupervisionStatus.DOWN);
    assertEquals("Supervised should have status time set", equipment.getStatusTime(), timestamp);
    assertEquals("Supervised should have status description set", equipment.getStatusDescription(), message);
  }

  @Test
  public void suspendWhenIsNotRunning() {
    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String message = "test-message";

    prepareMock(equipment);

    supervisedService.suspend(equipment.getId(), timestamp, message);

    assertEquals("Supervised should have DOWN status", equipment.getSupervisionStatus(), SupervisionConstants.SupervisionStatus.DOWN);
    assertTrue("Supervised should not have status time set", equipment.getStatusTime() == null);
    assertTrue("Supervised should not have status description set", equipment.getStatusDescription() == null);
  }

  private void prepareMock(Supervised equipment) {
    expect(c2monCache.get(equipment.getId())).andReturn(equipment);

    replay(c2monCache);
  }
}
