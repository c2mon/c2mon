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
package cern.c2mon.client.auth;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import cern.tim.shared.client.auth.SessionInfo;

public class LoginToolBar extends JToolBar implements ActionListener, SessionListener {
  /**
     * Serial Version UID identifier that allows to detect version changes
     */
  private static final long serialVersionUID = -3021285232405391572L;
    
  private JLabel loginName = new JLabel();
  private JButton logInButton = new JButton();
  private JButton logOutButton = new JButton();
  private Toolkit tk = Toolkit.getDefaultToolkit();
  private Image imageLogIn = tk.getImage(LoginToolBar.class.getResource("login.gif"));
  private Image imageLogOut = tk.getImage(LoginToolBar.class.getResource("logout.gif"));
  private String userName = null;
  private Frame parentFrame = null;

  public LoginToolBar(Frame owner)
  {
    super();
    setLayout(new FlowLayout(FlowLayout.LEFT));
    parentFrame = owner;
    SessionManager.getInstance().addSessionListener(this);
    loginName.setMaximumSize(new Dimension(150, 25));
    loginName.setMinimumSize(new Dimension(150, 25));
    loginName.setHorizontalAlignment(JLabel.LEFT);
    
    logInButton.setMaximumSize(new Dimension(25, 25));
    logInButton.setMinimumSize(new Dimension(25, 25));
    imageLogIn = imageLogIn.getScaledInstance(20,20, Image.SCALE_AREA_AVERAGING);
    ImageIcon imageIconLogIn = new ImageIcon(imageLogIn);
    logInButton.setIcon(imageIconLogIn);
    logInButton.setToolTipText("Log in");
    logInButton.setActionCommand("login");
    logInButton.addActionListener(this);
    logInButton.setEnabled(false);
        
    logOutButton.setMaximumSize(new Dimension(25, 25));
    logOutButton.setMinimumSize(new Dimension(25, 25));
    imageLogOut = imageLogOut.getScaledInstance(20,20, Image.SCALE_AREA_AVERAGING);
    ImageIcon imageIconLogOut = new ImageIcon(imageLogOut);    
    logOutButton.setIcon(imageIconLogOut);
    logOutButton.setToolTipText("Log out");
    logOutButton.setActionCommand("logout");
    logOutButton.addActionListener(this);
    logOutButton.setEnabled(false);
   
    add(loginName);
    add(logInButton);
    add(logOutButton);
    setUsername("guest");
    setFloatable(false);
    setVisible(true);
  }

  protected void setUsername(String user) {
    userName = user;
    loginName.setText("User: " + userName);
  }

  /**
   * Get the name of the currently logged in user.
   */
  public String getUsername() {
    return userName;
  }

  public void actionPerformed(ActionEvent e) {
    if (SessionManager.isInitialised()) {
      if ("login".equals(e.getActionCommand())) {    // login
        login();
      }
      else { // logout 
        SessionManager.getInstance().logout();
      }  
    }
  }

  public void login() {
    LoginDialog ldialog = new LoginDialog(parentFrame);
  }

  /**
   * onLogin() callback of the SessionListener interface.
   * This method is called by the SessionManager whenever a user successfully
   * logs in. A confirmation message dialog is displayed and the name of the 
   * user who logged in is displayed in the toolbar. Furthermore, the logout
   * button is enabled.
   * @param pInfo SessionInfo containing information about the new session
   */
  public void onLogin(final SessionInfo pInfo) {
    JOptionPane.showMessageDialog(parentFrame, "User " + pInfo.getUserName() + " logged in");
    setUsername(pInfo.getUserName());
    this.logOutButton.setEnabled(true);
    this.logInButton.setEnabled(false);
  }

  /**
   * onLogout() callback of the SessionListener interface.
   * This method is called by the SessionManager whenever a user has logged out.
   * The user name "guest" is displayed in the toolbar and the logout button is 
   * disabled.
   * @param pInfo SessionInfo containing information about the old session
   */
  public void onLogout(SessionInfo info) {
    //JOptionPane.showMessageDialog(null, "User " + info.getUserName() + " logged out");
    setUsername("guest");
    this.logOutButton.setEnabled(false);
    this.logInButton.setEnabled(true);
  }  

  /**
   * onSuspend() callback of the SessionListener interface.
   * This method is called by the SessionManager when it is unable to handle any
   * login/logout requests at the moment. When the session is suspended, the 
   * login/logout buttons are disabled.
   * @param isSuspended flag indicating whether the session is now suspended (true)
   * or active again (false).
   */
  public void onSuspend(SessionInfo info, boolean isSuspended) {
    this.logInButton.setEnabled(!isSuspended);
    this.logOutButton.setEnabled(!isSuspended && (info!=null));
  }
}