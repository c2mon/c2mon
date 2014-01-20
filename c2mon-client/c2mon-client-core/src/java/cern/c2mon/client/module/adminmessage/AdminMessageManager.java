/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
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
 ******************************************************************************/
package cern.c2mon.client.module.adminmessage;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.admin.AdminMessage;
import cern.c2mon.client.common.admin.AdminMessageDeliveryException;
import cern.c2mon.client.common.admin.AdminMessageImpl;
import cern.c2mon.client.common.util.ConcurrentSet;
import cern.c2mon.client.common.util.RbacAuthorizationDetailsParser;
import cern.c2mon.client.core.C2monSessionManager;
import cern.c2mon.client.jms.AdminMessageListener;
import cern.c2mon.client.module.C2monAdminMessageManager;
import cern.c2mon.client.module.adminmessage.handler.AdminMessageHandler;
import cern.c2mon.shared.common.command.AuthorizationDetails;

/**
 * The admin message manager allows registering
 * of listeners so they get informed about new administrator messages. It does also
 * have a method for sending admin messages.
 * 
 * @author vdeila
 */
@Service
public class AdminMessageManager implements C2monAdminMessageManager, AdminMessageListener {

  /** Log4j logger instance */
  private static final Logger LOG = Logger.getLogger(AdminMessageManager.class);
  
  /** The rbac class which will be used for the authorization details */
  private static final String RBAC_ADMIN_MESSAGE_CLASS = "TIM_APPLICATIONS";
  
  /** The rbac device which will be used for the authorization details */
  private static final String RBAC_ADMIN_MESSAGE_DEVICE = "TIM_WEBREFADMIN";
  
  /** The rbac property which will be used for the authorization details */
  private static final String RBAC_ADMIN_MESSAGE_PROPERTY = "RUN";
  
  /** A set of listeners */
  private final Set<AdminMessageListener> adminMessageListeners;
  
  /** Instance of the admin message handler */
  private final AdminMessageHandler adminMessageHandler;
  
  /** Instance of the session manager */
  private final C2monSessionManager sessionManager;
  
  /** 
   * The rbac authorization details that will be used to authorise every person who
   * tries to send admin messages.
   */
  private String rbacAdminAuthorizationDetails;
  
  /**
   * Constructor
   * 
   * @param adminMessageHandler the admin message handler
   * @param sessionManager the session manager
   */
  @Autowired
  protected AdminMessageManager(final AdminMessageHandler adminMessageHandler
      , final C2monSessionManager sessionManager) {
    
    this.adminMessageHandler = adminMessageHandler;
    this.sessionManager = sessionManager;
    this.adminMessageListeners = new ConcurrentSet<AdminMessageListener>();
  }
  
  /**
   * Sets the RBAC authorization details String 
   * @param authDetails The RBAC authorization details given as RBAC class/device/property tuple
   */
  @Autowired
  public void setAuthDetails(@Value("${c2mon.client.rbac.admin}") final String authDetails) {
    rbacAdminAuthorizationDetails = authDetails;
  }
  
  /**
   * Called by Spring to initialize this service.
   */
  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    adminMessageHandler.registerAdminMessageListener(this);
  }

  @Override
  public void addAdminMessageListener(final AdminMessageListener listener) {
    if (listener != null) {
      adminMessageListeners.add(listener);
    }
  }

  @Override
  public void removeAdminMessageListener(final AdminMessageListener listener) {
    adminMessageListeners.remove(listener);
  }

  @Override
  public void sendAdminMessage(final String userName, final AdminMessage.AdminMessageType type, final String message) throws AdminMessageDeliveryException {
    if (isUserAllowedToSend(userName)) {
      final AdminMessage adminMessage = new AdminMessageImpl(type, userName, message, new Timestamp(System.currentTimeMillis()));
      adminMessageHandler.publishAdminMessage(adminMessage);
    }
    else {
      throw new AdminMessageDeliveryException(String.format(
          "'%s' does not have the access to send admin messages, or is not logged in.",
          userName));
    }
  }
  
  @Override
  public boolean isUserAllowedToSend(final String userName) {
    
    try {
      return userName != null
          && sessionManager.isUserLogged(userName) 
          && sessionManager.isAuthorized(userName, getAdminAuthorizationDetails());
    } catch (IOException e) {
      LOG.error("RbacAuthDetails not found! " + e.getMessage());
      return false;
    }
  }

  @Override
  public void onAdminMessageUpdate(final AdminMessage adminMessage) {
    final Iterator<AdminMessageListener> iterator = adminMessageListeners.iterator();
    while (iterator.hasNext()) {
      final AdminMessageListener listener = iterator.next();
      try {
        listener.onAdminMessageUpdate(adminMessage);
      }
      catch (Exception e) {
        LOG.error(String.format(
            "Failed to notify listener '%s' about admin message.", listener.toString()
            ), e);
      }
    }
  }
  
  /**
   * @return the authorization details for a admin that can send messages
   * @throws IOException In case the authDetails could not be found
   */
  private AuthorizationDetails getAdminAuthorizationDetails() throws IOException {
    return RbacAuthorizationDetailsParser.parseRbacDetails(rbacAdminAuthorizationDetails);
  }
}
