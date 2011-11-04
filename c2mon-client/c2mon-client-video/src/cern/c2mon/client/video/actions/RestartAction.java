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
 * The RestartAction class initiates a restart of all running video connections <br>
 * This action is related to a bug in the JViewer from Netvu which appears from
 * time to time and freezes some of the running video streams.
 * 
 * @author Matthias Braeger
 */
public class RestartAction extends AbstractAction {

  /** Generated serial version UID */
  private static final long serialVersionUID = 4392399314184361733L;

  /** Log4J logger */
  protected static final Logger log = Logger.getLogger(RestartAction.class);
    
  /** Action command to restart the viewers in case one of the image freezes */
  public static final String RESTART_ACTION = "restart";
  
  /** The Singleton instance of that class */
  private static RestartAction _instance = null;
  
  /**
   * Default constructor that shall be used with this action
   */
  protected RestartAction() {
    super("Restart all Video Streams");
  }

  /**
   * @return The Singleton instance of that class
   */
  public static AbstractAction getInstance() {
    if ( _instance == null ) {
     _instance = new RestartAction(); 
    }
    
    return _instance;
  }
  
  /**
   * Gets the VideoPanelController from the TimVideoViewer
   * and forces a restart of the active video connections.
   */
  public void actionPerformed(ActionEvent e) {
    if ( log.isDebugEnabled() )
      log.debug("calling restart()...");
    
    TimVideoViewer viewer = TimVideoViewer.getInstance(); 
    viewer.getVideoPanelController().refresh();
    
    if ( log.isDebugEnabled() )
      log.debug("restart() end");
  }
}
