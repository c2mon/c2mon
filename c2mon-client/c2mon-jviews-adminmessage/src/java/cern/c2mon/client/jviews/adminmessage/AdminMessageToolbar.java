/******************************************************************************
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
 *****************************************************************************/
package cern.c2mon.client.jviews.adminmessage;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.admin.AdminMessage;
import cern.c2mon.client.common.admin.AdminMessageDeliveryException;
import cern.c2mon.client.common.listener.SessionListener;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monSessionManager;
import cern.c2mon.client.jms.AdminMessageListener;
import cern.c2mon.client.module.C2monAdminMessageManager;

/**
 * This toolbar allows administrators to send a broadcast message
 * to all running TIM client instances. Furthermore keeps a list of
 * all received messages.
 */
public class AdminMessageToolbar extends JToolBar {
  /** Serial Version UID for the AdminMessageToolbar class */
  private static final long serialVersionUID = -5430124941232684390L;
  private static final Logger LOG = Logger.getLogger(AdminMessageToolbar.class);
  
  private Frame parent = null;
  
  private final AbstractButton sendButton;
  private final AbstractButton historyButton;
  
  private ArrayList<AdminMessage> messages = new ArrayList<AdminMessage>();
  
  /** The admin message manager */
  private final C2monAdminMessageManager adminMessageManager;
  
  /** The session manager */
  private final C2monSessionManager sessionManager;
  
  /**
   * Default Constructor
   * @param pParent The parent frame.
   */
  public AdminMessageToolbar(final Frame pParent) {
    super(HORIZONTAL);
    setFloatable(false);
    this.setBorder(BorderFactory.createEmptyBorder());
    setOpaque(false);
    
    this.adminMessageManager = C2monServiceGateway.getAdminMessageManager();
    this.sessionManager = C2monServiceGateway.getSessionManager();
    
    this.parent = pParent;
    
    setLayout(new FlowLayout(FlowLayout.LEFT));
    
    sendButton = createButton();
    historyButton = createButton();
    
    Image image = Toolkit.getDefaultToolkit().getImage(AdminMessageToolbar.class.getClassLoader().getResource("message.gif"));
    image = image.getScaledInstance(20, 20, Image.SCALE_AREA_AVERAGING);
    ImageIcon imgIcon = new ImageIcon(image);

    //sendButton.setText("Send message");
    sendButton.setIcon(imgIcon);
    sendButton.setToolTipText("Send administrator message");
    historyButton.setText("...");
    historyButton.setToolTipText("Read old administrator messages.");
      
    this.historyButton.addActionListener(new HistoryButtonEventHandler());
    this.sendButton.addActionListener(new SendButtonEventHandler());
    
    this.add(historyButton);
    this.add(sendButton);
    
    this.adminMessageManager.addAdminMessageListener(new AdminMessageEventHandler());
    this.sessionManager.addSessionListener(new SessionEventHandler());
    
    checkAccessRights(getFirstUsername());
  }
  
  /**
   * Creates a simple standardized button
   * 
   * @return A <code>JButton</code>
   */
  private static AbstractButton createButton() {
    final AbstractButton button = new JButton();
    button.setMinimumSize(new Dimension(26, 26));
    button.setMaximumSize(new Dimension(26, 26));
    button.setPreferredSize(new Dimension(26, 26));
    button.setHideActionText(true);
    button.setEnabled(true);
    return button;
  }
  
  /**
   * Checks the rights of the user and changes the gui 
   * 
   * @param userName the user name to check. Can be <code>null</code>.
   */
  private void checkAccessRights(final String userName) {
    if (userName != null
        && this.adminMessageManager.isUserAllowedToSend(userName)) {
      sendButton.setEnabled(true);
      sendButton.setToolTipText("Send an administrator message to all TIM users.");
      LOG.info(String.format("User %s is allowed to send admin messages", userName));
    }
    else {
      sendButton.setEnabled(false);
      sendButton.setToolTipText("You have to be an administrator to send messages to all users.");
      LOG.info(String.format("User %s is not allowed to send admin messages", userName));
    }
  }
  
  /**
   * @return the first username that is logged in, or <code>null</code> if none
   *         is logged in.
   */
  private String getFirstUsername() {
    final Set<String> loggedInUsers = sessionManager.getLoggedUserNames();
    if (loggedInUsers != null && loggedInUsers.size() > 0) {
      return loggedInUsers.iterator().next();
    }
    return null;
  }
  
  /** Handles new admin message events */
  class AdminMessageEventHandler implements AdminMessageListener {
    @Override
    public void onAdminMessageUpdate(final AdminMessage adminMessage) {
      messages.add(adminMessage);
      new Thread() {
        public void run() {
          AdminMessagePopup.showAdminMessage(parent, adminMessage);
        }
      }.start();
    }
  }
  
  /** Handles login and logout events */
  class SessionEventHandler implements SessionListener {
    @Override
    public void onLogin(final String userName) {
      checkAccessRights(userName);
    }

    @Override
    public void onLogout(final String userName) {
      checkAccessRights(null); 
    }
  }
  
  /** Handles events when the send button is pressed */
  class SendButtonEventHandler implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      final AdminMessageSenderDialog sender = new AdminMessageSenderDialog(parent);
      sender.setVisible(true);
      if (sender.sendMessage()) {
        try {
          adminMessageManager.sendAdminMessage(getFirstUsername(), sender.getMessageType(), sender.getMessageText());
        }
        catch (AdminMessageDeliveryException ae) {
          JOptionPane.showMessageDialog(
            parent,
            ae.getMessage(),
            "Error sending admin messagee",
            JOptionPane.ERROR_MESSAGE
          );
          LOG.error("Could not send the admin message", ae);
        }
      }
      else {
        LOG.info("actionPerformed() : Not sending message");
      }
    }
  }
  
  /** Handles events when the history button is pressed */
  class HistoryButtonEventHandler implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      JOptionPane.showMessageDialog(
          parent, 
          new AdminMessageHistoryPanel(messages),
          "TIM administrator message history:",
          JOptionPane.PLAIN_MESSAGE
        );
    }
  }
  
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setSize(300, 300);
    frame.getContentPane().add(new AdminMessageToolbar(frame));
    frame.setVisible(true);
  }
}