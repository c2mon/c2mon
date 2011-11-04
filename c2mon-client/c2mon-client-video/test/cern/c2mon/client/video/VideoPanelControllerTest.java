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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.video.NetvuMainPanel;
import cern.c2mon.client.video.VideoPanelController;


/**
 * JUnit test class for ch.cern.tim.client.video.VideoPanelController.
 * 
 * @author Matthias Braeger
 */
public class VideoPanelControllerTest {

  private static VideoPanelController controller = null;
  
  /**
   * Is called before the tests
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // Creates one main viewer and a queue of length 4
    controller = new NetvuMainPanel("title", true, 4);
  }
  
  /**
   * Is called after the tests
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    controller.closeAllConnections();
  }
  
  @Test
  public void testAddVideoToQueue() {
    assertTrue(controller.addVideoToQueue(new VideoConnectionProperties("test", 1)));
    assertTrue(controller.addVideoToQueue(new VideoConnectionProperties("test", 2)));
    assertTrue(controller.addVideoToQueue(new VideoConnectionProperties("test", 3)));
    assertTrue(controller.addVideoToQueue(new VideoConnectionProperties("test", 4)));
    assertTrue(controller.addVideoToQueue(new VideoConnectionProperties("test", 5)));
    assertFalse("Since the queue is full we expect false", 
                controller.addVideoToQueue(new VideoConnectionProperties("test", 6)));
  }
  
  @Test
  public void testGetViewersQueueSize() {
    assertEquals(4, controller.getViewersQueueSize()); 
  }
  
  @Test
  public void testIsVideoInQueue() {
    assertTrue(controller.isVideoInQueue(new VideoConnectionProperties("test", 1)));
    assertTrue(controller.isVideoInQueue(new VideoConnectionProperties("test", 2)));
    assertTrue(controller.isVideoInQueue(new VideoConnectionProperties("test", 3)));
    assertTrue(controller.isVideoInQueue(new VideoConnectionProperties("test", 4)));
    assertTrue(controller.isVideoInQueue(new VideoConnectionProperties("test", 5)));
    
    assertFalse(controller.isVideoInQueue(new VideoConnectionProperties("test", 6)));
    assertFalse(controller.isVideoInQueue(new VideoConnectionProperties("test2", 1)));
    assertFalse(controller.isVideoInQueue(new VideoConnectionProperties("test2", 2)));
  }
  
  @Test
  public void testSetMainVideo() {
    assertEquals(
        "We expect that the last element of the queue is being returned, since it will be kicked out of the queue", 
        new VideoConnectionProperties("test", 5), 
        controller.setMainVideo(new VideoConnectionProperties("test", 6)));
    
    assertNull(
        "We expect nothing back, since this element is still in the queue", 
        controller.setMainVideo(new VideoConnectionProperties("test", 1)));
    
    assertNull(
        "We expect nothing back, since this element is still in the queue",
        controller.setMainVideo(new VideoConnectionProperties("test", 2)));
  }
  
  @Test
  public void testCloseVideoConnection() {
    assertTrue(controller.closeVideoConnection(new VideoConnectionProperties("test", 1)));
    assertFalse(controller.closeVideoConnection(new VideoConnectionProperties("test", 1)));
    
    assertTrue(controller.closeVideoConnection(new VideoConnectionProperties("test", 2)));
    assertFalse(controller.closeVideoConnection(new VideoConnectionProperties("test", 2)));
    
    assertTrue(controller.closeVideoConnection(new VideoConnectionProperties("test", 3)));
    assertFalse(controller.closeVideoConnection(new VideoConnectionProperties("test", 3)));
    
    assertTrue(controller.closeVideoConnection(new VideoConnectionProperties("test", 4)));
    assertFalse(controller.closeVideoConnection(new VideoConnectionProperties("test", 4)));
    
    assertFalse(controller.closeVideoConnection(new VideoConnectionProperties("test", 5)));
    
    assertTrue(controller.closeVideoConnection(new VideoConnectionProperties("test", 6)));
    assertFalse(controller.closeVideoConnection(new VideoConnectionProperties("test", 6)));
    
    
    assertFalse(controller.closeVideoConnection(new VideoConnectionProperties("test2", 1)));
  }
  
  @Test
  public void testCloseAllConnections() {
    testAddVideoToQueue();
    controller.closeAllConnections();
    
    assertFalse(controller.isVideoInQueue(new VideoConnectionProperties("test", 1)));
    assertFalse(controller.isVideoInQueue(new VideoConnectionProperties("test", 2)));
    assertFalse(controller.isVideoInQueue(new VideoConnectionProperties("test", 3)));
    assertFalse(controller.isVideoInQueue(new VideoConnectionProperties("test", 4)));
    assertFalse(controller.isVideoInQueue(new VideoConnectionProperties("test", 5)));
  }
  
  /**
   * This method is required to run the JUnit 4 tests with the old
   * Eclipse JUnit-runner
   * @return A Test adapter for the JUnit-runner of Eclipse
   */
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(VideoPanelControllerTest.class);
  }
}
