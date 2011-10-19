/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
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
 *****************************************************************************/
package cern.c2mon.client.history.gui.toolbars;

import java.awt.Dimension;

import javax.swing.AbstractButton;
import javax.swing.JButton;

/**
 * Factory for creating standardized buttons for the toolbar
 * 
 * @author vdeila
 *
 */
public final class ToolbarButtonFactory {

  /**
   * Creates a simple standardized button
   * 
   * @return A <code>JButton</code>
   */
  public static AbstractButton createButton() {
    final AbstractButton button = new JButton();
    button.setMinimumSize(new Dimension(26, 26));
    button.setMaximumSize(new Dimension(26, 26));
    button.setPreferredSize(new Dimension(26, 26));
    button.setHideActionText(true);
    button.setEnabled(true);

    return button;
  }
  
  /** Utility class */
  private ToolbarButtonFactory() {
    
  }
}
