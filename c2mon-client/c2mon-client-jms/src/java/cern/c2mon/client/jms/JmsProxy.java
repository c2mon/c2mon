package cern.c2mon.client.jms;
/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
import java.util.Collection;

import javax.jms.JMSException;

import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.request.JsonRequest;

/**
 * Interface to singleton managing all JMS logic for the client.
 * 
 * <p>There are a number of restrictions on how listeners are registered.
 * In particular, there is a 1-1 correspondence between updated ClientDataTags
 * and ServerUpdateListeners (in the current implementation, ClientDataTags are
 * in fact the ServerUpdateListeners).
 * 
 * <ul>
 * <li>a ServerUpdateListener object can only register to receive updates
 * for a single Tag; if an attempt is made to register it to another one
 * without first unregistering, the new registration will be ignored
 * <li>only one ServerUpdateListener can be registered for updates to 
 * destined for a given ClientDataTag; any new registration will result
 * in the old one being overwritten
 * </ul>
 * 
 * <p>The JmsProxy will take care of reestablishing the connection
 * and subscriptions after any JMS connection problems. JmsExceptions
 * will still be thrown by the callers so that other actions can be
 * taken such as invalidating Tags. Once the connection is reestablished,
 * the registered {@link ConnectionListener}s will be passed
 * a list of Tags for which a listener is registered, so that a refresh
 * of the data can be requested. ConnectionListeners are also notified
 * when the connection is lost.
 * 
 * <p>The implementation is thread safe, meaning the singleton
 * should behave correctly without any external synchronization.
 * 
 * <p>In general, null parameters are not accepted by the methods 
 * and IllegalArgumentExceptions/NullPointerExceptions will be thrown
 * if a null is passed.
 * 
 * @author Mark Brightwell
 *
 */
public interface JmsProxy {
  
  /**
   * Register the listener to received update notifications destined for the
   * specified ClientDataTag. This method will in fact "register if not
   * already registered with some Tag", so can safely be called to
   * confirm registration.
   * 
   * <p>Returns even if JMS connection is down; subscriptions will be
   * done automatically on reconnection.
   * 
   * @param serverUpdateListener the listener that will be called on update
   * @param clientDataTag the tag for which the updates are destined (also
   *                    the listener in current C2MON client API implementation)
   * @throws JMSException if there is a JMS failure in subscribing; the JmsProxy will
   *                  subscribe this listener automatically once the connection is back,
   *                  but the caller may with to invalidate the Tag in the meantime
   * @throws NullPointerException if either argument is null
   */
  void registerUpdateListener(ServerUpdateListener serverUpdateListener, 
                                  TopicRegistrationDetails clientDataTag) throws JMSException;

  /**
   * Unregisters a listener from receiving updates destined for the specified
   * ClientDataTag. This method will in fact "unregister if currently registered"
   * so can safely be called without a call to the isRegistered method.
   * 
   * @param serverUpdateListener the listener to unregister
   * @throws IllegalArgumentException if argument is null
   */
  void unregisterUpdateListener(ServerUpdateListener serverUpdateListener);
  
  /**
   * Determines if the passed listener is currently registered
   * (including those awaiting initial subscription if the connection
   * is down).
   * 
   * <p>Notice there is not guarantee that the listener will still be registered once
   * this method returns if the API is being called on multiple threads! 
   * 
   * @param serverUpdateListener the listener to check
   * @return true if registered
   * @throws IllegalArgumentException if argument is null
   */  
  boolean isRegisteredListener(ServerUpdateListener serverUpdateListener);

  /**
   * Replace the current registered listener with another one.
   * 
   * <p>If the first listener passed is not currently registered, the call
   * will have no effect.
   * 
   * @param registeredListener the current registered listener
   * @param replacementListener the new listener to register
   * @throws IllegalArgumentException if either argument is null
   */
  void replaceListener(ServerUpdateListener registeredListener, ServerUpdateListener replacementListener);
  
  /**
   * Send a request to the server and wait "timeout" milliseconds for a response.
   * 
   * <p>Never returns null.
   * 
   * @param jsonRequest the request object, convertible to Json format
   * @param queueName the name of the queue on which to send this request
   * @param timeout the time to wait for a response (in milliseconds); 
   *                            if no response arrives in this time, a NullPointerException is thrown
   * @param <T> the type of the response expected (inside the collection)
   *                            
   * @return the response to the request (never null)
   * @throws JMSException if not currently connected or 
   *                      if a JMS problem occurs while making the request (reconnection is handled by the JmsProxy)
   * @throws RuntimeException if the response from the server is null (probable timeout)
   * @throws NullPointerException thrown if either argument is null
   */
  <T extends ClientRequestResult> Collection<T> sendRequest(JsonRequest<T> jsonRequest, String queueName,int timeout) throws JMSException;

  /**
   * Register a listener for connection/disconnection events.
   * 
   * @param connectionListener the listener to register
   * @throws IllegalArgumentException if argument is null
   */
  void registerConnectionListener(ConnectionListener connectionListener);
  
  /**
   * Register a listener to be notified of supervision events received
   * from the server.
   * 
   * @param supervisionListener the listener to register
   * @throws IllegalArgumentException if argument is null
   */
  void registerSupervisionListener(SupervisionListener supervisionListener);
  
  /**
   * Unregister the listener from receiving supervision updates.
   * 
   * @param supervisionListener the listener to remove
   * @throws IllegalArgumentException if argument is null
   */
  void unregisterSupervisionListener(SupervisionListener supervisionListener);
}
