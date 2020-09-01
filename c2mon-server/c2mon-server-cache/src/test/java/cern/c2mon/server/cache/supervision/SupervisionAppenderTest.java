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
package cern.c2mon.server.cache.supervision;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

import static org.junit.Assert.assertTrue;

/**
 * Junit test of supervision appender.
 * @author Mark Brightwell
 *
 */
public class SupervisionAppenderTest {

  /**
   * Class to test.
   */
  private SupervisionAppenderImpl supervisionAppender;

  /**
   * Mocks
   */
  IMocksControl mockControl = EasyMock.createControl();
  private ProcessCache processCache;
  private ProcessFacade processFacade;
  private EquipmentCache equipmentCache;
  private EquipmentFacade equipmentFacade;

  @Before
  public void setUp() {
    processCache = mockControl.createMock(ProcessCache.class);
    processFacade = mockControl.createMock(ProcessFacade.class);
    equipmentCache = mockControl.createMock(EquipmentCache.class);
    equipmentFacade = mockControl.createMock(EquipmentFacade.class);
    supervisionAppender = new SupervisionAppenderImpl(processFacade, processCache, equipmentFacade, equipmentCache);
  }

  @Test
  public void testAddSupervisionQuality() {
    RuleTagCacheObject tag = new RuleTagCacheObject(1L);
    SupervisionEvent event = new SupervisionEventImpl(SupervisionEntity.PROCESS, 10L, "P_TEST", SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()), "test message");

    mockControl.replay();

    supervisionAppender.addSupervisionQuality(tag, event);

    mockControl.verify();

    assertTrue(!tag.isValid());
    assertTrue(tag.getDataTagQuality().isInvalidStatusSet(TagQualityStatus.PROCESS_DOWN));
    assertTrue(!tag.getDataTagQuality().isInvalidStatusSet(TagQualityStatus.EQUIPMENT_DOWN));
    assertTrue(tag.getDataTagQuality().getDescription().equals("test message"));
  }


}
