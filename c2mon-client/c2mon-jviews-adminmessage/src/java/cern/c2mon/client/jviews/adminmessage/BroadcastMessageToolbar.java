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

import cern.c2mon.client.common.admin.BroadcastMessage;
import cern.c2mon.client.common.admin.BroadcastMessageDeliveryException;
import cern.c2mon.client.common.service.SessionService;
import cern.c2mon.client.ext.messenger.BroadcastMessageService;
import cern.c2mon.client.ext.messenger.C2monMessengerGateway;
import cern.c2mon.client.ext.rbac.C2monSessionGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.common.listener.SessionListener;
import cern.c2mon.client.jms.BroadcastMessageListener;

/**
 * This toolbar allows administrators to send a broadcast message
 * to all running TIM client instances. Furthermore keeps a list of
 * all received messages.
 */
public class BroadcastMessageToolbar extends JToolBar {
  /** Serial Version UID for the BroadcastMessageToolbar class */
  private static final long serialVersionUID = -5430124941232684390L;
  private static final Logger LOG = LoggerFactory.getLogger(BroadcastMessageToolbar.class);
  
  private Frame parent = null;
  
  private final AbstractButton sendButton;
  private final AbstractButton historyButton;
  
  private ArrayList<BroadcastMessage> messages = new ArrayList<BroadcastMessage>();
  
  /** The admin message manager */
  private final BroadcastMessageService broadcastMessageService;
  
  /** The session manager */
  private final SessionService sessionManager;
  
  /**
   * Default Constructor
   * @param pParent The parent frame.
   */
  public BroadcastMessageToolbar(final Frame pParent) {
    super(HORIZONTAL);
    setFloatable(false);
    this.setBorder(BorderFactory.createEmptyBorder());
    setOpaque(false);
    
    this.broadcastMessageService = C2monMessengerGateway.getBroadcastMessageService();
    this.sessionManager = C2monSessionGateway.getSessionService();
    
    this.parent = pParent;
    
    setLayout(new FlowLayout(FlowLayout.LEFT));
    
    sendButton = createButton();
    historyButton = createButton();
    
    sendButton.setIcon(loadImage("Message_write.gif"));
    sendButton.setToolTipText("Send administrator message");
    historyButton.setIcon(loadImage("Message_receieved.gif"));
    historyButton.setToolTipText("Read old administrator messages.");
      
    this.historyButton.addActionListener(new HistoryButtonEventHandler());
    this.sendButton.addActionListener(new SendButtonEventHandler());
    
    this.add(historyButton);
    this.add(sendButton);
    
    this.broadcastMessageService.addMessageListener(new BroadcastMessageEventHandler());
    this.sessionManager.addSessionListener(new SessionEventHandler());
    
    checkAccessRights(getFirstUsername());
  }
  
  /**
   * @param name the file name of the icon to load
   * @return the image
   */
  private static ImageIcon loadImage(final String name) {
    try {
      Image image = Toolkit.getDefaultToolkit().getImage(BroadcastMessageToolbar.class.getClassLoader().getResource(name));
      image = image.getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING);
      return new ImageIcon(image);
    }
    catch (Exception e) {
      LOG.error(String.format(
          "Unable to load image '%s'", name
          ), e);
      return null;
    }
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
        && this.broadcastMessageService.isUserAllowedToSend(userName)) {
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
  class BroadcastMessageEventHandler implements BroadcastMessageListener {
    @Override
    public void onBroadcastMessageReceived(final BroadcastMessage broadcastMessage) {
      messages.add(broadcastMessage);
      new Thread() {
        public void run() {
          BroadcastMessagePopup.showAdminMessage(parent, broadcastMessage);
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
      final BroadcastMessageSenderDialog sender = new BroadcastMessageSenderDialog(parent);
      sender.setVisible(true);
      if (sender.sendMessage()) {
        try {
          broadcastMessageService.sendMessage(getFirstUsername(), sender.getMessageType(), sender.getMessageText());
        }
        catch (BroadcastMessageDeliveryException ae) {
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
          new BroadcastMessageHistoryPanel(messages),
          "TIM administrator message history:",
          JOptionPane.PLAIN_MESSAGE
        );
    }
  }
  
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setSize(300, 300);
    frame.getContentPane().add(new BroadcastMessageToolbar(frame));
    frame.setVisible(true);
  }
}
