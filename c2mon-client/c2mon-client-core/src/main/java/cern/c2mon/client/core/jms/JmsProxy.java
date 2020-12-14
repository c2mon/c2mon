/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.client.core.jms;

import java.util.Collection;

import javax.jms.JMSException;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.listener.TagUpdateListener;
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
   * @param topicRegistrationDetails the details need to register to updates
   *                                  for a given Tag
   * @throws JMSException if there is a JMS failure in subscribing; the JmsProxy will
   *                  subscribe this listener automatically once the connection is back,
   *                  but the caller may wish to invalidate the Tag in the meantime
   * @throws NullPointerException if either argument is null
   */
  void registerUpdateListener(TagUpdateListener serverUpdateListener,
                                  TopicRegistrationDetails topicRegistrationDetails) throws JMSException;

  /**
   * Unregisters a listener from receiving updates destined for the specified
   * ClientDataTag. This method will in fact "unregister if currently registered"
   * so can safely be called without a call to the isRegistered method.
   * 
   * @param serverUpdateListener the listener to unregister
   * @throws NullPointerException if argument is null
   * @throws IllegalStateException if trying to unregister an unrecognized ServerUpdateListener
   */
  void unregisterUpdateListener(TagUpdateListener serverUpdateListener);
  
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
   * @throws NullPointerException if argument is null
   */  
  boolean isRegisteredListener(TagUpdateListener serverUpdateListener);

  /**
   * Replace the current registered listener with another one.
   * 
   * <p>If the first listener passed is not currently registered, the call
   * will have no effect.
   * 
   * @param registeredListener the current registered listener
   * @param replacementListener the new listener to register
   * @throws NullPointerException if either argument is null
   */
  void replaceListener(TagUpdateListener registeredListener, TagUpdateListener replacementListener);
  
  /**
   * Sends a message to the given topic.
   * 
   * @param message the message to send
   * @param queueName the name of the queue on which to send this request
   * @param timeToLive how long the message will live on the broker !!This now overridden with default 10 minutes!!
   * @throws JMSException if not currently connected or 
   *                      if a JMS problem occurs while making the request (reconnection is handled by the JmsProxy)
   * @throws NullPointerException thrown if either argument is null
   */
  void publish(final String message, final String queueName, final long timeToLive) throws JMSException;
  
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
  <T extends ClientRequestResult> Collection<T> sendRequest(JsonRequest<T> jsonRequest, String queueName, int timeout) throws JMSException;
  
  /**
   * Send a request to the server and wait "timeout" milliseconds for a response.
   * 
   * <p>Never returns null.
   * 
   * @param jsonRequest the request object, convertible to Json format
   * @param queueName the name of the queue on which to send this request
   * @param timeout the time to wait for a response (in milliseconds); 
   *                            if no response arrives in this time, a NullPointerException is thrown
   * @param reportListener Receives updates for <code>ClientRequestProgressReport</code> and <code>ClientRequestErrorReport</code>
   * @param <T> the type of the response expected (inside the collection)
   *                            
   * @return the response to the request (never null)
   * @throws JMSException if not currently connected or 
   *                      if a JMS problem occurs while making the request (reconnection is handled by the JmsProxy)
   * @throws RuntimeException if the response from the server is null (probable timeout)
   * @throws NullPointerException thrown if either argument is null
   */
  <T extends ClientRequestResult> Collection<T> sendRequest(JsonRequest<T> jsonRequest, String queueName, int timeout,
      ClientRequestReportListener reportListener) throws JMSException;

  /**
   * Register a listener for connection/disconnection events.
   * 
   * @param connectionListener the listener to register
   * @throws NullPointerException if argument is null
   */
  void registerConnectionListener(ConnectionListener connectionListener);
  
  /**
   * Register a listener to be notified of supervision events received
   * from the server.
   * 
   * @param supervisionListener the listener to register
   * @throws NullPointerException if argument is null
   */
  void registerSupervisionListener(SupervisionListener supervisionListener);
  
  /**
   * Unregister the listener from receiving supervision updates.
   * 
   * @param supervisionListener the listener to remove
   */
  void unregisterSupervisionListener(SupervisionListener supervisionListener);
  
  /**
   * Register a listener to be notified of alarm messages received
   * from the server.
   * 
   * @param alarmListener the listener to register
   * @throws JMSException In case of JMS problems 
   */
  void registerAlarmListener(final AlarmListener alarmListener) throws JMSException;
   
  /**
   * Unregister the listener from receiving alarm updates.
   * 
   * @param alarmListener the listener to remove
   */
  void unregisterAlarmListener(final AlarmListener alarmListener);
  
  /**
   * Register a listener to be notified of BroadcastMessage events received
   * from the server.
   * 
   * @param broadcastMessageListener the listener to register
   * @throws NullPointerException if argument is null
   * @throws IllegalStateException if 
   */
  void registerBroadcastMessageListener(BroadcastMessageListener broadcastMessageListener);
  
  /**
   * Unregister the listener from receiving BroadcastMessage updates.
   * 
   * @param broadcastMessageListener the listener to remove
   */
  void unregisterBroadcastMessageListener(BroadcastMessageListener broadcastMessageListener);

  
  /**
   * Register a listener to be notified of heartbeat events incoming
   * from the server.
   * 
   * @param heartbeatListener the listener to register
   * @throws NullPointerException if argument is null
   */
  void registerHeartbeatListener(HeartbeatListener heartbeatListener);
  
  /**
   * Unregister the listener from receiving heartbeat updates.
   * 
   * @param heartbeatListener the listener to remove
   */
  void unregisterHeartbeatListener(HeartbeatListener heartbeatListener);
}
