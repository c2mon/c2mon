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

import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import cern.c2mon.client.video.TimVideoViewer;
import ch.cern.tim.gui.shared.aboutbox.AboutDialog;

/**
 * This action class is used within the Video Viewer to display via a 
 * MenuItem the About dialog box.
 * 
 * @author Matthias Braeger
 *
 */
public class AboutTimDialogAction extends AbstractAction {

  /** Generated serial version UID */
  private static final long serialVersionUID = -1995305001740033693L;
  
  /** The name of the application */
  private static final String APP_NAME = "TIM Video Viewer";
  
  /** The current version */
  private static final String TIM_VIDEO_VERSION = "JVLC 1.1.4";
  
  /** The logo image of the TIM Video Viewer */
  private static final Image TIM_VIDEO_LOGO = 
    Toolkit.getDefaultToolkit().getImage(TimVideoViewer.class.getResource("tim-video-splash.gif")); 

  /** The parent component that owns this instance */
  private final Frame parent;
  
  /** Log4J logger */
  protected static final Logger log = Logger.getLogger(HelpWebPageAction.class);
  
  /**
   * Default constructor
   * @param parent The parent component that owns this instance
   */
  public AboutTimDialogAction(final Frame parent) {
    super("About...");
    this.parent = parent;
  }
  
  /**
   * Opens the TIM About dialog
   */
  public void actionPerformed(ActionEvent arg0) {
    log.debug(parent);
   
    AboutDialog aboutDlg = new AboutDialog(parent, APP_NAME, TIM_VIDEO_VERSION, TIM_VIDEO_LOGO);
    aboutDlg.setVisible(true);
  }
}
