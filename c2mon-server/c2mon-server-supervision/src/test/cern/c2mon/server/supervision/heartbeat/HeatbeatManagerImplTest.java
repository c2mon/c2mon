/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.supervision.heartbeat;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.supervision.HeartbeatListener;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.util.jms.JmsSender;

/**
 * Tests the HeartbeatManager implementation correctly sends the
 * heartbeat in the correct format.
 * 
 * @author Mark Brightwell
 *
 */
public class HeatbeatManagerImplTest {

  /**
   * Class to test.
   */
  private HeartbeatManagerImpl heartbeatManagerImpl;
  
  /**
   * Heartbeat interval in millis
   */
  private static final long INTERVAL = 5000;
  
  /**
   * Mocked JmsSender.
   */
  private JmsSender mockJmsSender;
  
  @Before
  public void init() {
    mockJmsSender = EasyMock.createMock(JmsSender.class);
    ClusterCache clusterCache = EasyMock.createControl().createMock(ClusterCache.class);
    heartbeatManagerImpl = new HeartbeatManagerImpl(mockJmsSender, clusterCache);
    heartbeatManagerImpl.setHeartbeatInterval(INTERVAL);
  }
  
  @After
  public void after() {
    heartbeatManagerImpl.stop();   
  }
  
  /**
   * Tests the heartbeat is sent at every tick.
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testHeartbeat() throws InterruptedException {
    mockJmsSender.send(EasyMock.isA(String.class)); 
    mockJmsSender.send(EasyMock.isA(String.class));    
    EasyMock.replay(mockJmsSender);
    
    //start heartbeat
    heartbeatManagerImpl.start();    
    //wait long enough to get 2 notifications!
    Thread.sleep(2 * INTERVAL - 2000);
    EasyMock.verify(mockJmsSender);
  }
  
  /**
   * Tests listeners can correctly register and receive Heartbeat notifications.
   * @throws InterruptedException if sleep interrupted
   */
  @Test
  public void testListenerNotified() throws InterruptedException {
    HeartbeatListener mockListener = EasyMock.createMock(HeartbeatListener.class);       
    mockListener.notifyHeartbeat(EasyMock.isA(Heartbeat.class));
    mockListener.notifyHeartbeat(EasyMock.isA(Heartbeat.class));
    mockListener.notifyHeartbeat(EasyMock.isA(Heartbeat.class));    
    EasyMock.replay(mockListener);
        
    //register listener
    heartbeatManagerImpl.registerToHeartbeat(mockListener);
    //start heartbeat
    heartbeatManagerImpl.start();
    //wait for 3 notifications
    Thread.sleep(3 * INTERVAL - 2000);
    EasyMock.verify(mockListener);
  }
  
  
}
