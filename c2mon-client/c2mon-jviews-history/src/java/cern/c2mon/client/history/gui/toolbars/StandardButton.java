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

import javax.swing.JButton;

/**
 * Standard toolbar button
 * 
 * @author vdeila
 */
public class StandardButton extends JButton {

  /** serialVersionUID */
  private static final long serialVersionUID = -1380182063662989467L;

  /**
   * Creates a standard button
   */
  public StandardButton() {
    super();

    setMinimumSize(new Dimension(26, 26));
    setMaximumSize(new Dimension(26, 26));
    setPreferredSize(new Dimension(26, 26));
    setHideActionText(true);
  }
  

  
}
