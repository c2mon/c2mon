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

import cern.c2mon.cache.actions.state.SupervisionStateTagUpdateCascader;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cern.c2mon.server.cache.test.factory.SourceDataTagValueFactory.sampleCommFault;
import static cern.c2mon.shared.common.CacheEvent.UPDATE_ACCEPTED;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.DOWN;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.RUNNING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration test of supervision module with core cache modules.
 *
 * @author Alexandros Papageorgiou, Mark Brightwell
 */
@ContextConfiguration(classes = SupervisionStateTagUpdateCascader.class)
public class CommFaultTagSupervisionTest extends SupervisionCacheTest {

  public static final long EQ_ID = 150L;
  public static final long SUBEQ_ID = 250L;

  @Inject
  private C2monCache<Equipment> equipmentCache;

  @Inject
  private C2monCache<CommFaultTag> commFaultTagCache;

  @Inject
  private C2monCache<SubEquipment> subEquipmentCache;

  @Inject
  private C2monCache<SupervisionStateTag> stateTagCache;

  @Test
  public void initCorrectly() {
    assertEquals(stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus(), DOWN);
    assertEquals(stateTagCache.get(subEquipmentCache.get(SUBEQ_ID).getStateTagId()).getSupervisionStatus(), DOWN);
  }

  /**
   * Tests the processing of a commfault and its consequences on the
   * Equipment status and registered listeners.
   */
  @Test
  public void processCommFaultTagStatus() {
    supervisionManager.processControlTag(sampleCommFault(System.currentTimeMillis()));

    assertEquals(RUNNING, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus());
  }

  @Test
  public void processCommFaultTagStatusSubEqCascade() throws InterruptedException {
    CountDownLatch expectingOneUpdate = new CountDownLatch(1);
    stateTagCache.getCacheListenerManager().registerListener(subEq -> {
      if (SUBEQ_ID == subEq.getSupervisedId())
        expectingOneUpdate.countDown();
    }, UPDATE_ACCEPTED);

    supervisionManager.processControlTag(sampleCommFault(System.currentTimeMillis()));

    assertTrue("Subequipment should be updated", expectingOneUpdate.await(100, TimeUnit.MILLISECONDS));
    assertEquals(RUNNING, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus());
    assertEquals(RUNNING, stateTagCache.get(subEquipmentCache.get(SUBEQ_ID).getStateTagId()).getSupervisionStatus());
  }

  @Test
  public void processCommFaultTagTime() {
    // Choose an earlier time to avoid accidentally matching any other System.currentTimeMillis()
    long time = System.currentTimeMillis() - 1;
    supervisionManager.processControlTag(sampleCommFault(time));

    assertEquals(time, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getStatusTime().getTime());
  }

  @Test
  public void processCommFaultTagListeners() throws InterruptedException {
    CountDownLatch expectingOneUpdate = new CountDownLatch(1);
    commFaultTagCache.getCacheListenerManager().registerListener(eq -> {
      if (EQ_ID == eq.getSupervisedId())
        expectingOneUpdate.countDown();
    }, UPDATE_ACCEPTED);

    supervisionManager.processControlTag(sampleCommFault(System.currentTimeMillis()));

    assertTrue("Comm Fault tag should be updated", expectingOneUpdate.await(100, TimeUnit.MILLISECONDS));
  }

  @Test
  public void processCommFaultTagFiresStateListeners() throws InterruptedException {
    CountDownLatch expectingOneUpdate = new CountDownLatch(1);
    stateTagCache.getCacheListenerManager().registerListener(eq -> {
      if (EQ_ID == eq.getSupervisedId())
        expectingOneUpdate.countDown();
    }, UPDATE_ACCEPTED);

    supervisionManager.processControlTag(sampleCommFault(System.currentTimeMillis()));

    assertTrue("State tag should be updated", expectingOneUpdate.await(100, TimeUnit.MILLISECONDS));
  }

  @Test
  public void wrongOrderEvents() {
    // Equipment down initially
    assertEquals(DOWN, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus());

    // Switch to running
    long initialUpdateTime = System.currentTimeMillis();
    supervisionManager.processControlTag(sampleCommFault(initialUpdateTime));
    assertEquals(RUNNING, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus());

    // Switch to stopped, but with older timestamp
    SourceDataTagValue commFaultDown = sampleCommFault(initialUpdateTime - 1);
    commFaultDown.setValue(Boolean.FALSE);
    supervisionManager.processControlTag(commFaultDown);

    assertEquals(RUNNING, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus());
  }
}
