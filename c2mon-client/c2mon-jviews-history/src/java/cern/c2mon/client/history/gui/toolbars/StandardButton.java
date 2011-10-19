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
