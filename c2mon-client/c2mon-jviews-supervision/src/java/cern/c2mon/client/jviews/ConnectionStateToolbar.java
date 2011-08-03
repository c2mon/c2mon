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