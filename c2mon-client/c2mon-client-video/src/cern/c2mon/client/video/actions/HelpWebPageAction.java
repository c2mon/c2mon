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

package cern.c2mon.client.video.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import cern.accsoft.gui.beans.BrowserLauncher;
import cern.c2mon.client.video.TimVideoViewer;
import cern.c2mon.client.video.VideoPropertyNames;
import cern.c2mon.client.video.VideoViewerConfigLoader;


/**
 * This action class is used within the VideoViewer to provide via a 
 * MenuItem a link to the TIM Video Viewer help page. 
 * 
 * @author Matthias Braeger
 */
public class HelpWebPageAction extends AbstractAction {

  /** Generated serial version UID */
  private static final long serialVersionUID = -9092170387776987053L;
  
  /** Log4J logger */
  protected static final Logger log = Logger.getLogger(HelpWebPageAction.class);
  
  /** Action command to display the help */
  public static final String HELP_ACTION = "help";
  
  /** The Singleton instance of that class */
  private static HelpWebPageAction _instance = null;
  
  /**
   * Default constructor
   */
  protected HelpWebPageAction() {
    super("Help on TIM Video Viewer");
  }

  public static HelpWebPageAction getInstance() {
    if ( _instance == null ) {
      _instance = new HelpWebPageAction();
    }
    
    return _instance;
  }
  
  /**
   * The action method will try to open the help page of the TIM Video Viewer. The URL
   * is provided via the user properties. In case that it can't open the URL in a browser
   * it displays an error message box to the user.
   */
  public void actionPerformed(ActionEvent arg0) {
    final String helpPage = 
      (String)VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.HELP_WEB_PAGE);
    
    if (helpPage != null) {
      try {
        BrowserLauncher.openURL(helpPage);
      }
      catch (java.io.IOException ex) {
        JOptionPane.showMessageDialog(TimVideoViewer.getInstance(),
          "Error starting external viewer:\n " + ex.getMessage(),
          "Display error",
          JOptionPane.ERROR_MESSAGE);
      }   
      catch (Exception ex) {
        log.error("a problem encountered",ex);
      }
    }
    else {
      javax.swing.JOptionPane.showMessageDialog(TimVideoViewer.getInstance(),
          "Property viewerhelpdir not defined in the configuration file",
          "Help error",
          javax.swing.JOptionPane.ERROR_MESSAGE);      
    }
  }

}
