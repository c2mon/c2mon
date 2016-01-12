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
import java.awt.Frame;
import java.awt.Dimension;
import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import java.awt.GridLayout;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class ConnectionStateDialog extends JDialog  {
  /**
     * Serial Version UID for the ConnectionStateDialog class 
     */
  private static final long serialVersionUID = -1639121545885872683L;
  private JButton closeButton = new JButton();
  private ConnectionState connectionState1 = new ConnectionState();
  private HeartbeatState heartbeatState1 = new HeartbeatState();
  private JPanel connStatePanel = new JPanel();
  private JPanel heartbeatPanel = new JPanel();
  private JPanel buttonPanel = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JLabel connStateLabel = new JLabel();
  private JLabel heartbeatLabel = new JLabel();
  private GridBagLayout gridBagLayout = new GridBagLayout();
  private JLabel connStateDesc = new JLabel();
  private JLabel heartbeatDesc = new JLabel();


  public ConnectionStateDialog() {
    this(null, new ConnectionState(), new HeartbeatState());
  }

  public ConnectionStateDialog(Frame parent, ConnectionState pConnState, HeartbeatState pHeartbeatState) {
    super(parent, "TIM Connection State", true);
    connectionState1 = pConnState;
    heartbeatState1 = pHeartbeatState;
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(500, 150));
    this.getContentPane().setLayout(gridBagLayout);

    closeButton.setText("Close");
    closeButton.setActionCommand("close");
    closeButton.setToolTipText("Close the connection state dialog");
    closeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          closeButton_actionPerformed(e);
        }
      });
    heartbeatState1.setMinimumSize(new Dimension(150, 10));
    gridLayout1.setColumns(1);
    gridLayout1.setRows(3);
    connStateLabel.setText("JMS connection:");
    connStateLabel.setMinimumSize(new Dimension(100, 25));
    connectionState1.setMinimumSize(new Dimension(100, 25));
    connectionState1.setMaximumSize(new Dimension(150, 25));
    connStateLabel.setFont(new Font("Arial", 0, 11));
    heartbeatLabel.setText("TIM server heartbeat:");
    heartbeatLabel.setMinimumSize(new Dimension(100, 25));
    heartbeatState1.setMinimumSize(new Dimension(150, 25));
    heartbeatState1.setMaximumSize(new Dimension(150, 25));
    heartbeatLabel.setFont(new Font("Arial", 0, 11));
    connStateDesc.setText(connectionState1.getDescription());
    connStateDesc.setMinimumSize(new Dimension(200, 25));
    connStateDesc.setFont(new Font("Arial", 0, 11));
    heartbeatDesc.setText(heartbeatState1.getDescription());
    heartbeatDesc.setMinimumSize(new Dimension(200, 25));
    heartbeatDesc.setFont(new Font("Arial", 0, 11));
    
    this.getContentPane().add(connStateLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(connectionState1, new GridBagConstraints(1, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(connStateDesc, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(heartbeatLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(heartbeatState1, new GridBagConstraints(1, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(heartbeatDesc, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(closeButton, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(connStatePanel, null);
    this.getContentPane().add(heartbeatPanel, null);
    this.getContentPane().add(buttonPanel, null);
  }

  private void closeButton_actionPerformed(ActionEvent e) {
    this.setVisible(false);
    this.dispose();
  }

  public static void main(String[] args) {
    new ConnectionStateDialog().setVisible(true);
  }
}
