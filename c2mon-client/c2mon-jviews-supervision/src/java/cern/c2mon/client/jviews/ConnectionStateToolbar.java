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
package cern.c2mon.client.jviews;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JToolBar;

public class ConnectionStateToolbar extends JToolBar {

  /**
   * Serial Version UID for the ConnectionStateToolbar class
   */
  private static final long serialVersionUID = 3263184429192034551L;
  private ConnectionState connectionState = new ConnectionState();
  private HeartbeatState heartbeatState = new HeartbeatState();

  /**
   * Default Constructor
   * 
   * @param pParent The parent frame
   */
  public ConnectionStateToolbar(Frame pParent) {
    super(HORIZONTAL);
    this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
    this.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
//    this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3, 0, 0, 0)));
    this.setOpaque(false);
    this.setMinimumSize(new Dimension(95, 26));
    this.setMaximumSize(new Dimension(95, 26));
    this.setPreferredSize(new Dimension(95, 26));

    add(connectionState);
    add(heartbeatState);

    setFloatable(false);
    setVisible(true);

  }

  public ConnectionStateToolbar() {
    this(null);
  }
}
