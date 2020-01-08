/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.supervision;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.server.supervision.impl.SupervisionTagNotifier;
import cern.c2mon.server.test.CachePopulationRule;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * Integration test of supervision module with core cache modules.
 *
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheActionsModuleRef.class,
    CacheDbAccessModule.class,
    SupervisionModule.class
})
public class SupervisionManagerTest {

  @Rule
  @Autowired
  public CachePopulationRule supervisionCachePopulationRule;

  @Autowired
  private SupervisionManager supervisionManager;

  @Autowired
  private SupervisionFacade supervisionFacade;

  @Autowired
  private C2monCache<AliveTag> aliveTimerCache;

  @Autowired
  private C2monCache<Process> processCache;

  @Autowired
  private ProcessService processService;

  @Autowired
  private C2monCache<Equipment> equipmentCache;

  @Autowired
  private C2monCache<SubEquipment> subEquipmentCache;

  @Autowired
  private SupervisionNotifier supervisionNotifier;

  @Autowired
  private SupervisionTagNotifier supervisionTagNotifier;

  /**
   * Mock listeners registered for supervision events &
   * tag callbacks.
   */
  private SupervisionListener supervisionListener;

  private IMocksControl controller;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    controller = EasyMock.createNiceControl();
    supervisionListener = controller.createMock(SupervisionListener.class);
    supervisionNotifier.registerAsListener(supervisionListener);
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
    CountDownLatch latch = new CountDownLatch(6);
    supervisionListener.notifySupervisionEvent(EasyMock.isA(SupervisionEvent.class));
    EasyMock.expectLastCall().andAnswer(() -> { latch.countDown(); return null; }).times(6);

    controller.replay();

    //check initial status is correct
    AliveTag aliveTimer = aliveTimerCache.get(1221L);
    assertNotNull(aliveTimer);
    assertEquals(0, aliveTimer.getLastUpdate());
    Process process = processCache.get(aliveTimer.getRelatedId());
    assertEquals(SupervisionStatus.DOWN, process.getSupervisionStatus());
    assertEquals(null, process.getStatusTime());
    assertEquals(null, process.getStatusDescription());
//    Tag stateTag = controlTagCache.get(1220L);
//    assertEquals(null, stateTag.getValue());

    long updateTime = System.currentTimeMillis();
    //process control tag
    supervisionManager.processControlTag(new SourceDataTagValue(1221L,
        "test alive", true, 0L, new SourceDataTagQuality(), new Timestamp(updateTime), 4, false, "description", 10000));

    //check alive is updated
    aliveTimer = aliveTimerCache.get(1221L);
    assertNotNull(aliveTimer.getLastUpdate());
    assertTrue(aliveTimer.getLastUpdate() > System.currentTimeMillis() - 10000); //account for non-synchronized

    //check process status is changed
    process = processCache.get(aliveTimer.getRelatedId());
    assertEquals(SupervisionStatus.RUNNING, process.getSupervisionStatus());
    Timestamp processTime = process.getStatusTime();
    assertTrue(processTime.after(new Timestamp(updateTime - 1)));
    assertNotNull(process.getStatusDescription());

    //check tags are updated (note alive tag is not updated; this is done in SourceUpdateManager)
//    stateTag = controlTagCache.get(1220L);
//    assertEquals(SupervisionStatus.RUNNING.toString(), stateTag.getValue());
//    assertEquals(processTime, stateTag.getCacheTimestamp());

    latch.await(); //wait for notification on listener thread
    controller.verify();
  }

  /**
   * Alives older than 2 minutes are rejected.
   */
  @Test
  public void testRejectOldAlive() {
    //check alive timer is defined & set last update
    AliveTag aliveTimer = aliveTimerCache.get(1221L);
    assertNotNull(aliveTimer);
    aliveTimer.setLastUpdate(System.currentTimeMillis()-1000);
    long aliveTime = aliveTimer.getLastUpdate();
    //send alive 2 minutes old (should be rejected)
    SourceDataTagValue value = new SourceDataTagValue(1221L,
        "test alive", true, 0L, new SourceDataTagQuality(), new Timestamp(System.currentTimeMillis()), 4, false, "description", 10000);
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
  public void testProcessAliveNoAffect() throws InterruptedException {
    controller.reset();
    controller.replay(); //no listener call this time

    //check initial status is correct
    AliveTag aliveTimer = aliveTimerCache.get(1221L);
    assertNotNull(aliveTimer);
    long aliveTime = aliveTimer.getLastUpdate();
    Process process = processCache.get(aliveTimer.getRelatedId());

    processService.start(process.getId(), new Timestamp(System.currentTimeMillis()));
    processService.resume(process.getId(), new Timestamp(System.currentTimeMillis()), "");

    process = processCache.get(aliveTimer.getRelatedId());

    assertEquals(SupervisionStatus.RUNNING, process.getSupervisionStatus());
    Timestamp originalProcessTime = process.getStatusTime();
    assertNotNull(originalProcessTime);
    assertNotNull(process.getStatusDescription());
//    Tag stateTag = controlTagCache.get(1220L);
//    assertEquals(SupervisionStatus.RUNNING.toString(), stateTag.getValue());

    long updateTime = System.currentTimeMillis();
    //process control tag
    supervisionManager.processControlTag(new SourceDataTagValue(1221L,
        "test alive", true, 0L, new SourceDataTagQuality(), new Timestamp(updateTime), 4, false, "description", 10000));

    //check alive is updated
    assertNotNull(aliveTimer.getLastUpdate());
    assertTrue(aliveTimer.getLastUpdate() > aliveTime - 1);

    //check process status is not changed & time also
    assertEquals(SupervisionStatus.RUNNING, process.getSupervisionStatus());
    Timestamp processTime = process.getStatusTime();
    assertEquals(originalProcessTime, processTime);
    assertNotNull(process.getStatusDescription());

    //check tags are updated (note alive tag is not updated; this is done in SourceUpdateManager)
//    assertEquals(SupervisionStatus.RUNNING.toString(), stateTag.getValue());
//    assertEquals(originalProcessTime, stateTag.getCacheTimestamp());

//    Thread.sleep(2000); //wait for notification on listener thread
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
  @Ignore("This test is flaky")
  public void testCommFaultTag() throws InterruptedException {
    CountDownLatch latch1 = new CountDownLatch(6);
    //(1) Send CommFaultTag TRUE
    supervisionListener.notifySupervisionEvent(EasyMock.isA(SupervisionEvent.class));
    EasyMock.expectLastCall().andAnswer(() -> { latch1.countDown(); return null; }).times(6);

    Equipment equipment = equipmentCache.get(150L);
    assertEquals(equipment.getSupervisionStatus(), SupervisionStatus.DOWN);
    Timestamp originalTime = new Timestamp(System.currentTimeMillis() - 1000);
    equipment.setStatusTime(originalTime);
    String originalDescription = "initial description";
    equipment.setStatusDescription(originalDescription);
//    assertTrue(controlTagCache.get(equipment.getStateTagId()).getValue() == null);

    controller.replay();

    long updateTime = System.currentTimeMillis();
    supervisionManager.processControlTag(new SourceDataTagValue(1223L,
        "test commfault", true, Boolean.TRUE, new SourceDataTagQuality(), new Timestamp(updateTime), 4, false, "description", 10000));
    //wait for Tag callback thread
    latch1.await();

    controller.verify();

    //check equipment status & state tag have changed
    equipment = equipmentCache.get(150L);
    assertEquals(equipment.getSupervisionStatus(), SupervisionStatus.RUNNING);
    Timestamp secondTime = equipment.getStatusTime();
    assertFalse(originalTime.equals(secondTime));
    String secondDescription = equipment.getStatusDescription();
    assertFalse(originalDescription.equals(secondDescription));
//    assertEquals(SupervisionStatus.RUNNING.toString(), controlTagCache.get(equipment.getStateTagId()).getValue());

    //(2) Send CommFaultTag FALSE
    controller.reset();
    CountDownLatch latch2 = new CountDownLatch(6);

    supervisionListener.notifySupervisionEvent(EasyMock.isA(SupervisionEvent.class));
//    cacheSupervisionListener.onSupervisionChange(EasyMock.isA(Tag.class));
    EasyMock.expectLastCall().andAnswer(() -> { latch2.countDown(); return null; }).times(6);

    controller.replay();

    long updateTime2 = System.currentTimeMillis();
    supervisionManager.processControlTag(new SourceDataTagValue(1223L,
        "test commfault", true, Boolean.FALSE, new SourceDataTagQuality(), new Timestamp(updateTime2), 4, false, "description", 10000));
    latch2.await();

    controller.verify();
    equipment = equipmentCache.get(150L);
    //check equipment status & state tag have changed
    assertEquals(equipment.getSupervisionStatus(), SupervisionStatus.DOWN);
    assertFalse(secondTime.equals(equipment.getStatusTime()));
    assertFalse(secondDescription.equals(equipment.getStatusDescription()));
//    assertEquals(SupervisionStatus.DOWN.toString(), controlTagCache.get(equipment.getStateTagId()).getValue());
  }

  /**
   * Tests the processing of a commfault and its consequences on the
   * SubEquipment status and registered listeners.
   *
   * <p>Send 2 commfault tags, one FALSE indicating SubEquipment DOWN then
   * one TRUE indicating SubEquipment UP.
   *
   * @throws InterruptedException
   */
  @Test
  @Ignore("This test is flaky")
  public void testSubEquipmentCommFaultTag() throws InterruptedException {
    CountDownLatch latch1 = new CountDownLatch(2);
    // (1) Send CommFaultTag TRUE
    supervisionListener.notifySupervisionEvent(EasyMock.isA(SupervisionEvent.class));
//    cacheSupervisionListener.onSupervisionChange(EasyMock.isA(Tag.class));

    EasyMock.expectLastCall().andAnswer(() -> { latch1.countDown(); return null; }).times(2);

    SubEquipment subEquipment = subEquipmentCache.get(250L);
    assertEquals(subEquipment.getSupervisionStatus(), SupervisionStatus.DOWN);
    Timestamp originalTime = new Timestamp(System.currentTimeMillis() - 1000);
    subEquipment.setStatusTime(originalTime);
    String originalDescription = "initial description";
    subEquipment.setStatusDescription(originalDescription);
//    assertTrue(controlTagCache.get(subEquipment.getStateTagId()).getValue() == null);

    controller.replay();

    long updateTime = System.currentTimeMillis();
    supervisionManager.processControlTag(new SourceDataTagValue(1232L, "test commfault", true, Boolean.TRUE, new SourceDataTagQuality(),
        new Timestamp(updateTime), 4, false, "description", 10000));
    // wait for Tag callback thread
    latch1.await();

    controller.verify();

    // check equipment status & state tag have changed
    subEquipment = subEquipmentCache.get(250L);
    assertEquals(subEquipment.getSupervisionStatus(), SupervisionStatus.RUNNING);
    Timestamp secondTime = subEquipment.getStatusTime();
    assertFalse(originalTime.equals(secondTime));
    String secondDescription = subEquipment.getStatusDescription();
    assertFalse(originalDescription.equals(secondDescription));
//    assertEquals(SupervisionStatus.RUNNING.toString(), controlTagCache.get(subEquipment.getStateTagId()).getValue());

    //(2) Send CommFaultTag FALSE
    controller.reset();
    CountDownLatch latch2 = new CountDownLatch(2);

    supervisionListener.notifySupervisionEvent(EasyMock.isA(SupervisionEvent.class));
    EasyMock.expectLastCall().andAnswer(() -> { latch2.countDown(); return null; }).times(2);

    controller.replay();

    long updateTime2 = System.currentTimeMillis();
    supervisionManager.processControlTag(new SourceDataTagValue(1232L,
        "test commfault", true, Boolean.FALSE, new SourceDataTagQuality(), new Timestamp(updateTime2), 4, false, "description", 10000));
    latch2.await();

    controller.verify();
    subEquipment = subEquipmentCache.get(250L);
    //check equipment status & state tag have changed
    assertEquals(subEquipment.getSupervisionStatus(), SupervisionStatus.DOWN);
    assertFalse(secondTime.equals(subEquipment.getStatusTime()));
    assertFalse(secondDescription.equals(subEquipment.getStatusDescription()));
//    assertEquals(SupervisionStatus.DOWN.toString(), controlTagCache.get(subEquipment.getStateTagId()).getValue());
  }
}
