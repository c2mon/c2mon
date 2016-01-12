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
package cern.c2mon.client.jviews.adminmessage;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import cern.c2mon.client.common.admin.BroadcastMessage;
import cern.c2mon.client.common.admin.BroadcastMessageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcastMessageSenderDialog extends JDialog {

  /**
     * Serial Version UID for the BroadcastMessageSenderDialog class
     */
    private static final long serialVersionUID = 4356945784884369516L;

  private static final Logger LOG = LoggerFactory.getLogger(BroadcastMessageSenderDialog.class);
  
  private boolean sendMessage = false;
  
  private String messageText = null;
  
  private JButton sendButton = new JButton();
  private JButton previewButton = new JButton();
  private JButton cancelButton = new JButton();
  private JLabel labelHeader = new JLabel();
  private JTextPane messagePane = new JTextPane();
  private JScrollPane jScrollPane1 = new JScrollPane();
  
  
  public BroadcastMessageSenderDialog(Frame parent) {
    super(parent, "Send an administrator message to all users", true);
    try {
      jbInit();
    }
    catch (Exception e) {
      LOG.error("error occured while initialising AdminSenderDialog",e);             
    }
  }
  
  public String getMessageText() {
    return this.messageText;
  }
  
  /**
   * @return the message type
   */
  public BroadcastMessage.BroadcastMessageType getMessageType() {
    return BroadcastMessage.BroadcastMessageType.INFO.INFO;
  }

  public BroadcastMessageSenderDialog() {
    try {
      jbInit();
    } catch(Exception e) {
      LOG.error("error occured while initialising AdminSenderDialog",e);         
    }
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(null);
    setSize(400, 300);
  
    //set the default location (as setting the parent frame not always work in a multiscreen environment)
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();
    this.setLocation((screenSize.width/ 2 - frameSize.width/2), 
                     (screenSize.height/ 2 - frameSize.height/2));

    sendButton.setText("Send");
    sendButton.setBounds(new Rectangle(60, 230, 87, 25));
    sendButton.setActionCommand("send");
    sendButton.setToolTipText("Send the message");
    sendButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          send(e);
        }
      });
    previewButton.setText("Preview");
    previewButton.setBounds(new Rectangle(150, 230, 87, 25));
    previewButton.setActionCommand("preview");
    previewButton.setToolTipText("Preview the message before sending it");
    previewButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          previewMessage(e);
        }
      });
    cancelButton.setText("Cancel");
    cancelButton.setBounds(new Rectangle(240, 230, 87, 25));
    cancelButton.setActionCommand("cancel");
    cancelButton.setToolTipText("Close this dialog without sending the message.");
    cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cancel(e);
        }
      });
    labelHeader.setText("<HTML><BODY><P>Please enter the message you would like to send to " + "all users.</P></BODY></HTML>");
    labelHeader.setBounds(new Rectangle(5, 5, 380, 15));
    labelHeader.setToolTipText("null");
    labelHeader.setMinimumSize(new Dimension(380, 15));
    labelHeader.setMaximumSize(new Dimension(380, 15));
    labelHeader.setPreferredSize(new Dimension(380, 15));
    jScrollPane1.setBounds(new Rectangle(5, 25, 380, 200));
    messagePane.setBounds(new Rectangle(5, 25, 380, 200));
    messagePane.setToolTipText("null");
    messagePane.setMinimumSize(new Dimension(380, 200));
    messagePane.setMaximumSize(new Dimension(380, 200));
    messagePane.setPreferredSize(new Dimension(380, 200));
    jScrollPane1.getViewport().add(messagePane,null);
    this.getContentPane().add(jScrollPane1, null);
    //this.getContentPane().add(messagePane, null);
    this.getContentPane().add(labelHeader, null);
    this.getContentPane().add(cancelButton, null);
    this.getContentPane().add(previewButton, null);
    this.getContentPane().add(sendButton, null);
    
  }

  private void previewMessage(ActionEvent e) {
    BroadcastMessagePopup.showAdminMessage(
        getParent(), 
        new BroadcastMessageImpl(
            getMessageType(), 
            "<User Name>", 
            this.messagePane.getText(), 
            new Timestamp(System.currentTimeMillis())), 
        "Admin message PREVIEW");
  }
  
  private void cancel(ActionEvent e) {
    sendMessage = false;
    messageText = null;
    dispose();
  }

  private void send(ActionEvent e) {
    sendMessage = true;
    messageText = messagePane.getText();
    dispose();
  }
  
  public boolean sendMessage() {
    return this.sendMessage;
  }

}
