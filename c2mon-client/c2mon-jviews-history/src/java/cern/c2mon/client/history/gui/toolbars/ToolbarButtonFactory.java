/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
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
