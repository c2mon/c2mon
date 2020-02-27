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
package cern.c2mon.server.history.logger;

import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.history.mapper.ServerLifecycleEventMapper;
import cern.c2mon.shared.client.lifecycle.ServerLifecycleEvent;
import org.apache.ibatis.exceptions.PersistenceException;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Unit test of ServerLifecycleLogger.
 *
 * @author Mark Brightwell
 *
 */
public class ServerLifecycleLoggerTest {

  private ServerLifecycleLogger serverLifecycleLogger;

  private ServerLifecycleEventMapper lifecycleMapper;

  private IMocksControl control;

  @Before
  public void init() {
    control = EasyMock.createNiceControl();
    lifecycleMapper = control.createMock(ServerLifecycleEventMapper.class);
    serverLifecycleLogger = new ServerLifecycleLogger(lifecycleMapper, new ServerProperties());
    serverLifecycleLogger.setTimeBetweenRelogs(100);
  }

  /**
   * Tests normal start/stop logging by calling directly the lifecycle
   * methods.
   */
  @Test
  public void testLogStartStop() {
    lifecycleMapper.logEvent(EasyMock.isA(ServerLifecycleEvent.class));
    EasyMock.expectLastCall().times(2);
    control.replay();
    serverLifecycleLogger.start();
    serverLifecycleLogger.stop();
    control.verify();
  }

  /**
   * Test the start log is repeated if logging fails.
   * Expect at least 3 tries in 5s. Also checks stop returns.
   * @throws InterruptedException
   */
  @Test
  public void testRepeatStartLog() throws InterruptedException {
    lifecycleMapper.logEvent(EasyMock.isA(ServerLifecycleEvent.class));
    CountDownLatch latch = new CountDownLatch(3);
    EasyMock.expectLastCall().andAnswer(() -> { latch.countDown(); throw new PersistenceException(""); }).times(3,10);
    control.replay();
    serverLifecycleLogger.start();
    latch.await();
    serverLifecycleLogger.stop();
    control.verify();
  }
}
