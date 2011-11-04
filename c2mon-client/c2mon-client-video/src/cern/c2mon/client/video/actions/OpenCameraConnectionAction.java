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

import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.video.TimVideoViewer;

/**
 * Opens a video connection on the main view for the video connection properties
 * that were provided during the instantiation of that class.
 *
 * @author Matthias Braeger
 */
public class OpenCameraConnectionAction extends AbstractAction {
  
  /** Serial version UID */
  private static final long serialVersionUID = 6405913899164286945L;

  final VideoConnectionProperties connectionProperties;
  
  /**
   * Default constructor
   */
  public OpenCameraConnectionAction(final VideoConnectionProperties vcp) {
    super(vcp.getDescription());
    this.connectionProperties = vcp;
  }

  
  @Override
  public void actionPerformed(ActionEvent arg0) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        TimVideoViewer.getInstance().getVideoMainHandler().setActiveVideoConncetion(connectionProperties);
      }
    }).start();
  }
}
