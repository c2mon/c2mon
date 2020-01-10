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
package cern.c2mon.server.supervision.notifier;

import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.server.supervision.impl.SupervisionNotifierImpl;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Unit test of the SupervisionNotifier implementation.
 *
 * @author Mark Brightwell
 *
 */
public class SupervisionNotifierTest {

  /**
   * Class to test.
   */
  private static SupervisionNotifier supervisionNotifier = new SupervisionNotifierImpl(null, null, null);

  /**
   * Test event.
   */
  private static final SupervisionEntity ENTITY = SupervisionEntity.PROCESS;
  private static final Long ID = 1L;
  private static final String NAME = "P_TEST";
  private static final SupervisionStatus STATUS = SupervisionStatus.RUNNING;
  private static final java.sql.Timestamp DATE = new java.sql.Timestamp(System.currentTimeMillis());
  private static final String MESSAGE = null;

  /**
   * Test registration.
   */
  @Test
  public void testRegisterListener() {
    SupervisionListener supervisionListener = EasyMock.createMock(SupervisionListener.class);
    supervisionNotifier.registerAsListener(supervisionListener);
  }

  @Test(timeout = 500)
  public void testNotifications() throws InterruptedException {
    SupervisionListener supervisionListener = EasyMock.createMock(SupervisionListener.class);
    SupervisionEvent event = new SupervisionEventImpl(ENTITY, ID, NAME, STATUS, DATE, MESSAGE);
    CountDownLatch latch = new CountDownLatch(1);

    supervisionListener.notifySupervisionEvent(event);
    EasyMock.expectLastCall().andAnswer(() -> { latch.countDown(); return null; });

    EasyMock.replay(supervisionListener);
    supervisionNotifier.registerAsListener(supervisionListener);
    supervisionNotifier.notifySupervisionEvent(event);
    //wait for notification to occur
    latch.await();
    EasyMock.verify(supervisionListener);
  }

}
