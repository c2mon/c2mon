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
package cern.c2mon.client.ext.messenger.impl;

import cern.c2mon.client.common.admin.BroadcastMessage;
import cern.c2mon.client.common.admin.BroadcastMessageDeliveryException;
import cern.c2mon.client.common.admin.BroadcastMessageImpl;
import cern.c2mon.client.common.service.SessionService;
import cern.c2mon.client.common.util.ConcurrentSet;
import cern.c2mon.client.common.util.RbacAuthorizationDetailsParser;
import cern.c2mon.client.ext.messenger.BroadcastMessageHandler;
import cern.c2mon.client.ext.messenger.BroadcastMessageService;
import cern.c2mon.client.jms.BroadcastMessageListener;
import cern.c2mon.shared.common.command.AuthorizationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;

/**
 * The {@link BroadcastMessageServiceImpl} is responsible for sending broadcast messages to all clients which have registered
 * to receive them.
 *
 * @author vdeila
 * @author Franz Ritter
 */
@Service
public class BroadcastMessageServiceImpl implements BroadcastMessageService, BroadcastMessageListener {

  /** Log4j logger instance */
  private static final Logger LOG = LoggerFactory.getLogger(BroadcastMessageServiceImpl.class);

  /** The rbac class which will be used for the authorization details */
  private static final String RBAC_ADMIN_MESSAGE_CLASS = "TIM_APPLICATIONS";

  /** The rbac device which will be used for the authorization details */
  private static final String RBAC_ADMIN_MESSAGE_DEVICE = "TIM_WEBREFADMIN";

  /** The rbac property which will be used for the authorization details */
  private static final String RBAC_ADMIN_MESSAGE_PROPERTY = "RUN";

  private final Set<BroadcastMessageListener> broadcastMessageListeners;

  private final BroadcastMessageHandler broadcastMessageHandler;

  private SessionService sessionService;

  /**
   * The rbac authorization details that will be used to authorise every person who
   * tries to send messages.
   */
  private String rbacAdminAuthorizationDetails;

  /**
   * Constructor
   *
   * @param broadcastMessageHandler the message handler
   * @param sessionManager the session manager
   */
  @Autowired
  protected BroadcastMessageServiceImpl(final BroadcastMessageHandler broadcastMessageHandler) {

    this.broadcastMessageHandler = broadcastMessageHandler;
    this.broadcastMessageListeners = new ConcurrentSet<>();
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
    broadcastMessageHandler.registerMessageListener(this);
  }

  @Override
  public void addMessageListener(final BroadcastMessageListener listener) {
    if (listener != null) {
      broadcastMessageListeners.add(listener);
    }
  }

  @Override
  public void removeMessageListener(final BroadcastMessageListener listener) {
    broadcastMessageListeners.remove(listener);
  }

  @Override
  public void sendMessage(final String userName, final BroadcastMessage.BroadcastMessageType type, final String message) throws BroadcastMessageDeliveryException {
    if (isUserAllowedToSend(userName)) {
      final BroadcastMessage broadcastMessage = new BroadcastMessageImpl(type, userName, message, new Timestamp(System.currentTimeMillis()));
      broadcastMessageHandler.publishMessage(broadcastMessage);
    } else {
      throw new BroadcastMessageDeliveryException(String.format(
          "'%s' does not have the access to send broadcast messages, or is not logged in.",
          userName));
    }
  }

  @Override
  public boolean isUserAllowedToSend(final String userName) {

    try {

      if(sessionService == null){
        return true;

      } else {
        return userName != null
            && sessionService.isUserLogged(userName)
            && sessionService.isAuthorized(userName, getAdminAuthorizationDetails());

      }
    } catch (IOException e) {
      LOG.error("RbacAuthDetails not found! " + e.getMessage());
      return false;
    }
  }

  @Override
  public void onBroadcastMessageReceived(final BroadcastMessage broadcastMessage) {
    final Iterator<BroadcastMessageListener> iterator = broadcastMessageListeners.iterator();
    while (iterator.hasNext()) {
      final BroadcastMessageListener listener = iterator.next();
      try {
        listener.onBroadcastMessageReceived(broadcastMessage);
      } catch (Exception e) {
        LOG.error(String.format(
            "Failed to notify listener '%s' about admin message.", listener.toString()
        ), e);
      }
    }
  }

  @Override
  public void registerSessionService(SessionService sessionService) {
    if (sessionService == null) {
      LOG.warn("No SessionService to to the BroadcastMessageManager set. Service is null.");
    }

    if (this.sessionService != null) {
      LOG.warn("SessionService were already set. Overriding of the service!");
    }

    this.sessionService = sessionService;
  }

  /**
   * @return the authorization details for a admin that can send messages
   * @throws IOException In case the authDetails could not be found
   */
  private AuthorizationDetails getAdminAuthorizationDetails() throws IOException {
    return RbacAuthorizationDetailsParser.parseRbacDetails(rbacAdminAuthorizationDetails);
  }
}
