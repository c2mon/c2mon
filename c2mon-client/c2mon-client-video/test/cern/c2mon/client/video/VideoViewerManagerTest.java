/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2008  CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.client.video;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.video.IVideoViewerManager;
import cern.c2mon.client.video.viewers.NetvuViewer;

/**
 * JUnit test for ch.cern.tim.client.video.VideoViewerManager
 * 
 * @author Matthias Braeger
 */
public class VideoViewerManagerTest {
  
  private static IVideoViewerManager manager = null;
  
  private static VideoConnectionProperties props1 = null;
  private static VideoConnectionProperties props2 = null;
  
  /**
   * Is called before the tests
   * 
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // Creates a single view video viewer
    manager = new NetvuViewer(true).getVideoViewerManager();
    props1 = new VideoConnectionProperties("test", 1);
    props2 = new VideoConnectionProperties("test", 2);
    
    manager.connectCamera(props1);
  }

  /**
   * Is called after the tests
   * 
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    manager.closeAllConnections();
  }
  
  @Test
  public void testConnectCamera() {
    manager.connectCamera(props1);
    assertTrue(manager.isRunning(props1));
    
    manager.connectCamera(props2);
    assertFalse(manager.isRunning(props1));
    assertTrue(manager.isRunning(props2));
  }
  
  @Test
  public void testGetActiveConnectionProperties() {
    manager.connectCamera(props1);
    
    assertNotNull(manager.getActiveConnectionProperties());
    assertEquals(props1, manager.getActiveConnectionProperties());
  }
  
  @Test
  public void testIsActivlyConnected() {
    assertTrue(manager.isShowingVideo());
  }
  
  @Test
  public void testIsConnectionAvailable() {
    manager.connectCamera(props1);
    assertTrue(manager.isRunning(props1));
    assertFalse(manager.isRunning(props2));
  }
  
  @Test
  public void testCloseCameraConn() {
    manager.connectCamera(props1);
    assertTrue(manager.closeCameraConn(props1));
    assertFalse(manager.closeCameraConn(props2));
  }
  
  @Test
  public void testCloseAllConn() {
    manager.connectCamera(props1);
    manager.connectCamera(props2);
    manager.closeAllConnections();
    
    assertFalse(manager.isShowingVideo());
    assertFalse(manager.isRunning(props1));
    assertFalse(manager.isRunning(props2));
  }

  /**
   * This method is required to run the JUnit 4 tests with the old Eclipse
   * JUnit-runner
   * 
   * @return A Test adapter for the JUnit-runner of Eclipse
   */
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(VideoViewerManagerTest.class);
  }
}
