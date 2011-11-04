/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2010 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.video.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import cern.c2mon.client.video.TimVideoViewer;


/**
 * This action class is used within the Video Viewer to display via a 
 * MenuItem the About dialog box.
 * 
 * @author Matthias Braeger
 *
 */
public class SystemExitAction extends AbstractAction {

  /** Generated serial version UID */
  private static final long serialVersionUID = -1995305001740034293L;
  
  /** Log4J logger */
  protected static final Logger log = Logger.getLogger(HelpWebPageAction.class);
  
  /** Singleton instance */
  private static SystemExitAction instance = null;
  
  /**
   * Default constructor
   * @param parent The parent component that owns this instance
   */
  private SystemExitAction() {
    super("Exit");
  }
  
  /**
   * @return The singleton instance of this class
   */
  public static SystemExitAction getInstance() {
    if (instance == null) {
      instance = new SystemExitAction();
    }
    
    return instance;
  }
  
  /**
   * Opens the TIM About dialog
   */
  public void actionPerformed(ActionEvent arg0) {
    TimVideoViewer.getInstance().getVideoMainHandler().closeAllConnections();
    System.exit(0);
  }

}
