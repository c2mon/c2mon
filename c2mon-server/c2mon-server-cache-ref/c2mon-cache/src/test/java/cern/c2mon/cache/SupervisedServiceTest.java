package cern.c2mon.cache;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.SupervisedService;
import cern.c2mon.server.cache.SupervisedServiceImpl;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class SupervisedServiceTest {

  private C2monCache cache;
  private AliveTimerService aliveTimerService;

  private SupervisedService<Supervised> supervisedService;

  @Before
  public void init() {
    cache = EasyMock.createNiceMock(C2monCache.class);
    aliveTimerService = EasyMock.createNiceMock(AliveTimerService.class);

    supervisedService = new SupervisedServiceImpl<>(cache, aliveTimerService);
  }

  @Test
  public void getLastOccurredSupervisionEventWithStatusTimeAndStatusDescription() {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String statusDescription = "description";
    String name = "test";

    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);
    equipment.setName(name);
    equipment.setStatusTime(timestamp);
    equipment.setStatusDescription(statusDescription);
    equipment.setSupervisionStatus(SupervisionConstants.SupervisionStatus.RUNNING);

    expect(cache.get(equipment.getId())).andReturn(equipment);

    replay(cache);

    SupervisionEvent supervisionEvent = supervisedService.getSupervisionStatus(equipment.getId());

    assertEquals("SupervisionEvent should have the same id", supervisionEvent.getEntityId(), equipment.getId());
    assertEquals("SupervisionEvent should have the same name", supervisionEvent.getName(), equipment.getName());
    assertEquals("SupervisionEvent should have the same supervision status", supervisionEvent.getStatus(), equipment.getSupervisionStatus());
    assertEquals("SupervisionEvent should have the same supervision time", supervisionEvent.getEventTime(), equipment.getStatusTime());
    assertEquals("SupervisionEvent should have the same supervision message", supervisionEvent.getMessage(), equipment.getStatusDescription());
  }

  @Test
  public void getLastOccurredSupervisionEventWithNullStatusTimeAndNullStatusDescription() {
    String name = "test";

    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);
    equipment.setName(name);
    equipment.setStatusTime(null);
    equipment.setStatusDescription(null);
    equipment.setSupervisionStatus(SupervisionConstants.SupervisionStatus.RUNNING);

    expect(cache.get(equipment.getId())).andReturn(equipment);

    replay(cache);

    SupervisionEvent supervisionEvent = supervisedService.getSupervisionStatus(equipment.getId());

    assertEquals("SupervisionEvent should have the same id", supervisionEvent.getEntityId(), equipment.getId());
    assertEquals("SupervisionEvent should have the same name", supervisionEvent.getName(), equipment.getName());
    assertEquals("SupervisionEvent should have the same supervision status", supervisionEvent.getStatus(), equipment.getSupervisionStatus());
    assertNotNull("SupervisionEvent should have not null supervision time", supervisionEvent.getEventTime());
    assertNotNull("SupervisionEvent should have not null supervision message", supervisionEvent.getMessage());
  }

  @Test
  public void startSupervisionWithSupervisedId() {
    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    expect(cache.get(equipment.getId())).andReturn(equipment);

    replay(cache);

    supervisedService.start(equipment.getId(), timestamp);

    assertEquals("Supervised should have STARTUP status", equipment.getSupervisionStatus(), SupervisionConstants.SupervisionStatus.STARTUP);
    assertTrue("Supervised should have information about start in status description", equipment.getStatusDescription().contains("was started"));
    assertTrue("Supervised should have timestamp set", equipment.getStatusTime() != null);
  }

  @Test
  public void stopSupervisionWithSupervisedId() {
    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    expect(cache.get(equipment.getId())).andReturn(equipment);

    replay(cache);

    supervisedService.stop(equipment.getId(), timestamp);

    assertEquals("Supervised should stop with DOWN status", equipment.getSupervisionStatus(), SupervisionConstants.SupervisionStatus.DOWN);
    assertTrue("Supervised should have timestamp set", equipment.getStatusTime() != null);
    assertTrue("Supervised should have information about stop in status description", equipment.getStatusDescription().contains("was stopped"));
  }

  

  /** TODO: write tests for:
   * 3. resume
   * 4. suspend
   * 5. isRunning
   * 6. isUncertain
   */
}
