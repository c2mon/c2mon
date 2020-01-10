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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Integration test of supervision module with core cache modules.
 *
 * @author Alexandros Papageorgiou, Mark Brightwell
 *
 */
public class CommFaultTagSupervisionTest extends SupervisionCacheTest {

  @Rule
  @Autowired
  public CachePopulationRule supervisionCachePopulationRule;

  @Autowired
  private SupervisionManager supervisionManager;

  @Autowired
  private C2monCache<Equipment> equipmentCache;

  @Autowired
  private C2monCache<SubEquipment> subEquipmentCache;

  @Autowired
  private SupervisionNotifier supervisionNotifier;

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
