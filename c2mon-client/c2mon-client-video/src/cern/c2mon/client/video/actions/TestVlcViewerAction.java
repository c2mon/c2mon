/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.video.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.video.TimVideoViewer;

public class TestVlcViewerAction extends AbstractAction {
  
  /** Serial version UID */
  private static final long serialVersionUID = 1790257300641162284L;

  /** Singelton instance */
  private static TestVlcViewerAction instance = null;
  
  private final VideoConnectionProperties[] openViews = new VideoConnectionProperties[1];
  
  /** Put here the right odserver */
  private final static String odserver = "";

  /**
   * Hidden default constructor
   */
  protected TestVlcViewerAction() {
    super("Test Viewer connection");
    
    /** Put here your test properties */
    openViews[0] = new VideoConnectionProperties(odserver, 1234);
    openViews[0].setDescription(odserver +":1234");

  }
  
  public final VideoConnectionProperties[] getOpenViews() {
    return openViews;
  }
  
  /**
   * Returns the singleton instance of this class
   * @return The singleton instance
   */
  public static TestVlcViewerAction getInstance() {
    if (instance == null)
      instance = new TestVlcViewerAction();
    
    return instance;
  }


  @Override
  public void actionPerformed(ActionEvent arg0) {
    
    new Thread(new Runnable() {
      @Override
      public void run() {
        int count = 0;
        
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {}
        while (true) {
          System.out.println("Start " + openViews[0].getCamera());
          TimVideoViewer.getInstance().getVideoMainHandler().setActiveVideoConncetion(openViews[(0 + count) % openViews.length]);
        
          for (int i = 1; i < openViews.length; i++) {
            final int camera = (i + count) % openViews.length; 
            System.out.println("Start " + openViews[i].getCamera());
            TimVideoViewer.getInstance().getVideoMainHandler().addVideoRequest(openViews[camera]);
          }
          
          try {Thread.sleep(5000);} catch (InterruptedException e) {}
          TimVideoViewer.getInstance().getVideoMainHandler().closeAllConnections();
          count++;
          System.out.println("*******************************************");
          System.out.println("Survived " + count + " connection restarts!");
          System.out.println("*******************************************");
        }
      }
    }).start();
  }
}
