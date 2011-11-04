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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.video.NetvuMainPanel;
import cern.c2mon.client.video.VideoMainHandler;


/**
 * JUnit test for ch.cern.tim.client.video.VideoMainHandler
 * 
 * @author Matthias Braeger
 */
public class VideoMainHandlerTest {

  private static VideoMainHandler handler = null;
  
  /**
   * Is called before the tests
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // Creates a handler for one main viewer and a queue of length 4
    handler = new VideoMainHandler(new NetvuMainPanel("test", true, 4));
    
    // Add 10 video requests
    for (int i = 0; i < 10; i++) {
      handler.addVideoRequest(new VideoConnectionProperties("test", i));
    }
  }
  
  /**
   * Is called after the tests
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    handler.closeAllConnections();
  }
  
  @Test
  public void testCloseVideoConnection() {
    int randomCam = (int)Math.round(Math.random() * 9) + 1;
    assertTrue(handler.closeVideoConnection(new VideoConnectionProperties("test", randomCam)));
    assertFalse(handler.closeVideoConnection(new VideoConnectionProperties("test", randomCam)));
    
    assertFalse(handler.closeVideoConnection(new VideoConnectionProperties("test1", 1)));
  }
  
  @Test
  public void testCloseAllConnections() {
    handler.closeAllConnections();
    
    for (int i = 0; i < 10; i++) {
      assertFalse(handler.closeVideoConnection(new VideoConnectionProperties("test", i)));
    }
  }
  
  
  /**
   * This method is required to run the JUnit 4 tests with the old
   * Eclipse JUnit-runner
   * @return A Test adapter for the JUnit-runner of Eclipse
   */
  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(VideoMainHandlerTest.class);
  }
}
