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
package cern.c2mon.client.video;

import java.awt.Component;

/**
 * This interface defines the functionalities that are needed by the
 * <code>VideoMainPanel</code> in order to control the different video viewers 
 * and to forward the video request to the appropriate video managers.
 * @author Matthias Braeger
 */
public interface IVideoViewer {
  
  /**
   * Returns the current height of the title label 
   * This method is preferable to writing component.getBounds().height, or component.getSize().height 
   * because it doesn't cause any heap allocations.
   * 
   * @return the current height of the title label
   */
  int getTitleLabelHeight();


  /**
   * Sets the information text
   * @param text The information text
   */
  void setTitle(final String text);


  /**
   * @return the manager of this viewer.
   */
  IVideoViewerManager getVideoViewerManager();


  /** 
   * Returns the Video Viewer Component that shall be displayed to the user
   * in the <code>VideoMainPanel</code>. 
   * 
   * @return The graphical component to be displayed.
   */
  Component getVideoViewerComponent();
}
