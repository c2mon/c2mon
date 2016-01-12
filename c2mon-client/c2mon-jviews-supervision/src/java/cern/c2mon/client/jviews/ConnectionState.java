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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.jms.ConnectionListener;


public class ConnectionState extends JPanel implements ConnectionListener {

  /**
     * Serial Version UID for the ConnectionState class 
     */
  private static final long serialVersionUID = 4752494453968802899L;
  public  static final int CONNECTION_UNINITIALISED = 0;
  public  static final int CONNECTION_OK = 1;
  public static final int CONNECTION_LOST = 2;

  private static ImageIcon[] ICONS={null, null, null};
  private static final String[] ICON_NAMES = {
    "connection_uninitialised.gif", 
    "connection_ok.gif", 
    "connection_lost.gif"
  };
  
  private static final String[] STATE_DESCS = {
    "Trying to connect ...", 
    "<HTML><BODY><P>JMS connection <B>OK</B></P></BODY></HTML>", 
    "<HTML><BODY><P>Connection to JMS broker <B>lost</B>!</P><P>Trying to reconnect...</P></BODY></HTML>"
  };

  private int state = CONNECTION_UNINITIALISED;
  
  private String description = null;
  private ImageIcon icon = null;
  private JLabel labelIcon = new JLabel();

  public  ConnectionState() {
    this(CONNECTION_UNINITIALISED);
  }
  
  public ConnectionState(int pState) {
    super(new BorderLayout());
    labelIcon = new JLabel();
    labelIcon.setMinimumSize(new Dimension(16, 16));
    labelIcon.setMaximumSize(new Dimension(16, 16));
    labelIcon.setPreferredSize(new Dimension(16, 16));
    labelIcon.setHorizontalAlignment(JLabel.CENTER);
    this.add(labelIcon, BorderLayout.NORTH);
    setState(pState);
    C2monServiceGateway.getSupervisionManager().addConnectionListener(this);
  }

  public void setState(int pState) {
    this.state = pState;
    this.description = getDescription(pState);
    this.icon = getIcon(pState);
    labelIcon.setIcon(this.icon);
    labelIcon.setToolTipText(this.description);
  }
  
  public ImageIcon getIcon() {
    return this.icon;
  }

  public String getDescription() {
    return this.description;
  }

  public int getState() {
    return this.state;
  }

  private static ImageIcon getIcon(int pState) {
    if (ICONS[pState] == null) {
      Image image = Toolkit.getDefaultToolkit().getImage(ConnectionState.class.getClassLoader().getResource(ICON_NAMES[pState]));
      image = image.getScaledInstance(16,16, Image.SCALE_AREA_AVERAGING);
      ICONS[pState] = new ImageIcon(image);
    }
    return ICONS[pState];
  }

  private static String getDescription(int pState) {
    return STATE_DESCS[pState];
  }

  @Override
  public void onConnection() {
    setState(ConnectionState.CONNECTION_OK);
  }

  @Override
  public void onDisconnection() {
    setState(ConnectionState.CONNECTION_LOST);
  }


}
