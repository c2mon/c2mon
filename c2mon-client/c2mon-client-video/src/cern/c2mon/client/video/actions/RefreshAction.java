/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2008 CERN This program is free software; you can redistribute
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

import org.apache.log4j.Logger;

import cern.c2mon.client.video.TimVideoViewer;

/**
 * The RepaintAction class takes care of refreshing the video images. <br>
 * This action is related to a bug in the JViewer from Netvu which appears from
 * time to time during the connection to a new video stream. Without the refresh
 * it could be that the image does not appear on the screen.
 * 
 * @author Matthias Braeger
 */
public class RefreshAction extends AbstractAction {

  /** Generated serial version UID */
  private static final long serialVersionUID = 4392399314184361733L;

  /** Log4J logger */
  protected static final Logger log = Logger.getLogger(RefreshAction.class);
  
  /** Flag to decide whether to enlarge or to decrease the frame by one pixel */
  private boolean flag = false;
  
  /** Action command to repaint the video viewers */
  public static final String REFRESH_ACTION = "refresh";
  
  /** Singleton instance */
  private static RefreshAction _instance = null;
  
  /**
   * Default constructor that shall be used with this action
   */
  protected RefreshAction() {
    super("Refresh Video Images");
  }
  
  /**
   * @return The Singleton instance of that class
   */
  public static RefreshAction getInstance() {
    if ( _instance == null ) {
      _instance = new RefreshAction(); 
    }
    
    return _instance;
  }
  
  /**
   * Resizes the main JFrame by one pixel to force a refresh of
   * the video viewers. This is of course not very elegant but
   * I could not find another way to do it. All standard methods like
   * revalidate(), repaint, update(), etc. were not helping.
   */
  public synchronized void refresh() {
    if ( log.isDebugEnabled() )
      log.debug("calling refresh()...");
    
    TimVideoViewer viewer = TimVideoViewer.getInstance(); 
    
    if ( flag = !flag )
      viewer.setSize(
        viewer.getWidth() + 1, 
        viewer.getHeight());
    else
      viewer.setSize(
          viewer.getWidth() - 1, 
          viewer.getHeight());
    
    if ( log.isDebugEnabled() )
      log.debug("refresh() end");
  }

  public void actionPerformed(ActionEvent e) {
    refresh();
  }
}
