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
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cern.c2mon.shared.common.supervision.SupervisionStatus.DOWN;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.RUNNING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration test of supervision module with core cache modules.
 *
 * @author Alexandros Papageorgiou, Mark Brightwell
 */
public class CommFaultTagSupervisionTest extends SupervisionCacheTest {

  public static final long EQ_ID = 150L;
  public static final long SUBEQ_ID = 250L;
  @Autowired
  private C2monCache<Equipment> equipmentCache;

  @Autowired
  private C2monCache<CommFaultTag> commFaultTagCache;

  @Autowired
  private C2monCache<SubEquipment> subEquipmentCache;

  @Inject
  private C2monCache<SupervisionStateTag> stateTagCache;

  private IMocksControl controller;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    controller = EasyMock.createNiceControl();
    assertEquals(stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus(), DOWN);
    assertEquals(stateTagCache.get(subEquipmentCache.get(SUBEQ_ID).getStateTagId()).getSupervisionStatus(), DOWN);
  }

  /**
   * Tests the processing of a commfault and its consequences on the
   * Equipment status and registered listeners.
   */
  @Test
  @DirtiesContext
  public void processCommFaultTagStatus() {
    supervisionManager.processControlTag(createSampleCommFaultTag(System.currentTimeMillis()));

    assertEquals(RUNNING, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus());
  }

  @Test
  @DirtiesContext
  public void processCommFaultTagStatusSubEqCascade() throws InterruptedException {
    CountDownLatch expectingOneUpdate = new CountDownLatch(1);
    subEquipmentCache.getCacheListenerManager().registerListener(subEq -> {
      if (SUBEQ_ID == subEq.getId())
        expectingOneUpdate.countDown();
    });

    supervisionManager.processControlTag(createSampleCommFaultTag(System.currentTimeMillis()));

    assertTrue("Subequipment should be updated", expectingOneUpdate.await(100, TimeUnit.MILLISECONDS));
    assertEquals(RUNNING, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus());
  }

  @Test
  @DirtiesContext
  public void processCommFaultTagTime() {
    long time = System.currentTimeMillis();
    supervisionManager.processControlTag(createSampleCommFaultTag(time));

    assertEquals(time, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getStatusTime().getTime());
  }

  @Test
  @DirtiesContext
  public void processCommFaultTagDescription() {
    SourceDataTagValue commFaultTag = createSampleCommFaultTag(System.currentTimeMillis());
    String exampleValue = "exampleValue";
    commFaultTag.setValueDescription(exampleValue);
    supervisionManager.processControlTag(commFaultTag);

    assertEquals(exampleValue, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getStatusDescription());
  }

  @Test
  @DirtiesContext
  public void processCommFaultTagListeners() throws InterruptedException {
    CountDownLatch expectingOneUpdate = new CountDownLatch(1);
    commFaultTagCache.getCacheListenerManager().registerListener(eq -> {
      if (EQ_ID == eq.getSupervisedId())
        expectingOneUpdate.countDown();
    }, CacheEvent.UPDATE_ACCEPTED);

    supervisionManager.processControlTag(createSampleCommFaultTag(System.currentTimeMillis()));

    assertTrue("Comm Fault tag should be updated", expectingOneUpdate.await(100, TimeUnit.MILLISECONDS));
  }

  @Test
  public void wrongOrderEvents() {
    // Equipment down initially
    assertEquals(DOWN, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus());

    // CFTag1, switch to running
    long initialUpdateTime = System.currentTimeMillis();
    supervisionManager.processControlTag(createSampleCommFaultTag(initialUpdateTime));
    assertEquals(RUNNING, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus());

    // CFTag2, switch to stopped, but with older timestamp
    SourceDataTagValue commFaultDown = createSampleCommFaultTag(initialUpdateTime - 1);
    commFaultDown.setValue(Boolean.FALSE);
    supervisionManager.processControlTag(commFaultDown);

    assertEquals(RUNNING, stateTagCache.get(equipmentCache.get(EQ_ID).getStateTagId()).getSupervisionStatus());
  }

  private SourceDataTagValue createSampleCommFaultTag(long updateTime) {
    return new SourceDataTagValue(
      1223L,
      "test commfault",
      true,
      Boolean.TRUE,
      new SourceDataTagQuality(),
      new Timestamp(updateTime),
      4,
      false,
      "description",
      10000);
  }
}
