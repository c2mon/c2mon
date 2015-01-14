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
package cern.c2mon.client.core;

import java.util.Collection;
import java.util.Set;

import javax.jms.JMSException;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.common.listener.DataTagListener;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.cache.CacheSynchronizationException;
import cern.c2mon.client.core.manager.TagManager;
import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.shared.client.tag.TagConfig;

/**
 * This interface describes the methods which are provided by
 * the C2MON tag manager singleton. The tag manager allows
 * subscribing listeners to the <code>ClientDataTag</code>'s
 * and to get informed when a new update has been sent.
 *
 * @author Matthias Braeger
 */
public interface C2monTagManager {

  /**
   * Use this method for registering a listener and to receive the current (initial) values and updates
   * for the list of specified data tags.<p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before. <p>
   * You will be informed about new updates via the <code>onUpdate(ClientDataTagValue)</code>
   * method.
   *
   * @param dataTagIds A collection of data tag IDs
   * @param listener the listener which shall be registered
   * @return A collection with initial values for all tags to which the listener got subscribed.
   *         Please note, that also the listener is receiving the initial values.
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tags. In that case the {@link TagManager} will
   *         rollback the subscription.
   * @see #subscribeDataTags(Set, DataTagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking()
   */
  void subscribeDataTags(final Set<Long> dataTagIds, final DataTagUpdateListener listener) throws CacheSynchronizationException;
  
  /**
   * Registers a listener to receive the current (initial) value and updates for one specific data tag.<p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before. <p>
   * You will be informed about new updates via the <code>onUpdate(ClientDataTagValue)</code>
   * method.
   *
   * @param dataTagId The unique identifier of the data tag you want to subscribe to
   * @param listener the listener which shall be registered
   * @return The initial value of the subscribed tag. Please note, that also the listener is
   *         receiving the initial value!
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tag. In that case the {@link TagManager} will
   *         rollback the subscription.
   * @see #subscribeDataTag(Long, DataTagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking();
   */
  void subscribeDataTag(final Long dataTagId, final DataTagUpdateListener listener) throws CacheSynchronizationException;
  
  /**
   * Registers a listener to receive updates for specific data tags. 
   * The method will return the initial values of the subscribed tags to {@link DataTagListener#onInitialUpdate(Collection)}.
   * <b>Please note</b> that the {@link DataTagListener#onUpdate(ClientDataTagValue)} method will then not
   * receive the initial values.<p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before. <p>
   * You will be informed about new updates via the <code>onUpdate(ClientDataTagValue)</code>
   * method.
   *
   * @param dataTagIds A collection of data tag IDs
   * @param listener the listener which shall be registered
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tags. In that case the {@link TagManager} will
   *         rollback the subscription.
   * @see #subscribeDataTags(Set, DataTagUpdateListener)
   * @see C2monSupervisionManager#isServerConnectionWorking()
   */
  void subscribeDataTags(final Set<Long> dataTagIds, final DataTagListener listener) throws CacheSynchronizationException;
  
  /**
   * Registers a listener to receive updates for a specific data tag. 
   * The method will return the initial value of the subscribed tag to {@link DataTagListener#onInitialUpdate(Collection)}. <b>Please note</b> 
   * that the {@link DataTagListener#onUpdate(ClientDataTagValue)} method will then not receive the initial value.<p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before. <p>
   * You will be informed about new updates via the <code>onUpdate(ClientDataTagValue)</code>
   * method.
   *
   * @param dataTagId The unique identifier of the data tag you want to subscribe to
   * @param listener the listener which shall be registered
   * @return The initial value of the subscribed tag. Please note, that the listener is not
   *         receiving the initial value!
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tag. In that case the {@link TagManager} will
   *         rollback the subscription.
   * @see #subscribeDataTag(Long, DataTagUpdateListener)
   * @see C2monSupervisionManager#isServerConnectionWorking()
   */
  void subscribeDataTag(final Long dataTagId, final DataTagListener listener) throws CacheSynchronizationException;

  /**
   * Use this method for unregistering a listener from receiving updates for specific data tags.
   *
   * @param dataTagIds A collection of data tag id's
   * @param listener the listener which shall be registered
   */
  void unsubscribeDataTags(final Set<Long> dataTagIds, final DataTagUpdateListener listener);
  
  /**
   * Unregisters a listener from receiving updates for specific data tag.
   *
   * @param dataTagId The unique identifier of the data tag from which we want to unsubscribe
   * @param listener the listener which shall be registered
   */
  void unsubscribeDataTag(final Long dataTagId, final DataTagUpdateListener listener);


  /**
   * Use this method to unsubscribe from all previously registered data tags.
   * @param listener the listener which shall be registered
   */
  void unsubscribeAllDataTags(final DataTagUpdateListener listener);


  /**
   * Returns for a given listener a copy of all subscribed data tags with
   * their current state as <code>ClientDataTagValue</code> instances.
   *
   * @param listener The listener for which we want to get the data tags
   *        subscriptions
   * @return A collection of <code>ClientDataTag</code> objects
   */
  Collection<ClientDataTagValue> getAllSubscribedDataTags(final DataTagUpdateListener listener);

  /**
   * Returns for a given listener a list of all subscribed data tags ids.
   *
   * @param listener The listener for which we want to get the data tags
   *        subscriptions
   * @return A collection of tag ids
   */
  Set<Long> getAllSubscribedDataTagIds(final DataTagUpdateListener listener);

  /**
   * Returns for the given id a copy of the cached data tag.
   * If the tag is not in the local cache it will try to fetch it from the server.
   * In case of an unknown tag id the result will be an empty {@link ClientDataTagValue}
   * object.
   * <p>
   * <b>Please notice</b>, that this method call does not write anything to the local
   * cache. This means that you might increase the server load when asking constantly
   * for tags on which no {@link DataTagUpdateListener} is subscribed to.
   *
   * @param tagId A data tag id
   * @return A <code>ClientDataTag</code> object
   * @throws RuntimeException In case a communication problems with JMS or the C2MON server
   *         occurs while trying to retrieve tag information.
   * @see #getDataTags(Collection)
   * @see #subscribeDataTags(Set, DataTagUpdateListener)
   * @see C2monSupervisionManager#isServerConnectionWorking()
   */
  ClientDataTagValue getDataTag(final Long tagId);

  /**
   * Returns for every valid id of the list a copy of the cached data tag.
   * If the value is not in the local cache it will try to fetch it from the server.
   * However, in case of an unknown tag id the corresponding tag might be missing.
   * <p>
   * <b>Please notice</b>, that this method call does not write anything to the local
   * cache. This means that you might increase the server load when asking constantly
   * for tags on which no {@link DataTagUpdateListener} is subscribed to.
   *
   * @param tagIds A collection of data tag id's
   * @return A collection of all <code>ClientDataTag</code> objects
   * @throws RuntimeException In case a communication problems with JMS or the C2MON server
   *         occurs while trying to retrieve tag information.
   * @see #subscribeDataTags(Set, DataTagUpdateListener)
   * @see C2monSupervisionManager#isServerConnectionWorking();
   */
  Collection<ClientDataTagValue> getDataTags(final Collection<Long> tagIds);

  /**
   * Returns a TagConfiguration object for every valid id on the list.
   * The values are fetched from the server.
   * However, in case of a connection error or an unknown tag id the corresponding
   * tag might be missing.
   *
   * @param tagIds A collection of data tag id's
   * @return A collection of all <code>TagConfiguration</code> objects
   */
  Collection<TagConfig> getTagConfigurations(final Collection<Long> tagIds);

  /**
   * Returns an {@link AlarmValue} object for every valid id on the list.
   * The values are fetched from the server.
   * However, in case of a connection error or an unknown tag id the corresponding
   * tag might be missing.
   *
   * @param alarmIds A collection of alarm id's
   * @return A collection of all <code>AlarmValue</code> objects
   */
  Collection<AlarmValue> getAlarms(final Collection<Long> alarmIds);

  /**
   * Returns an {@link AlarmValue} object for every active alarm found
   * in the server.
   *
   * @return A collection of all active <code>AlarmValue</code> objects
   */
  Collection<AlarmValue> getAllActiveAlarms();

  /**
   * Applies the configuration and returns a Configuration Report.
   * The values are fetched from the server.
   * However, in case of a connection error or an unknown configuration Id the corresponding
   * tag might be missing.
   *
   * @see C2monTagManager#applyConfiguration(Long, ClientRequestReportListener) that also sends
   * reports for the progress of the operation
   *
   * @param configurationId The configuration id used to fetch the Configuration Report object
   * @return A Configuration Report object
   */
  ConfigurationReport applyConfiguration(final Long configurationId);

  /**
   * Applies the configuration and returns a Configuration Report.
   * The values are fetched from the server.
   *
   * Has an extra parameter that allows the caller
   * to be informed for the progress of the operation.
   *
   * However, in case of a connection error or an unknown configuration Id the corresponding
   * tag might be missing.
   *
   * @param configurationId The configuration id used to fetch the Configuration Report object
   * @param reportListener Is informed about the progress of the operation on the server side.
   * @see ClientRequestProgressReport
   * @see ClientRequestErrorReport
   * @return A Configuration Report object
   */
  ConfigurationReport applyConfiguration(final Long configurationId, final ClientRequestReportListener reportListener);

  /**
   * Requests the DAQ config XML for a given process.
   *
   * @param processName the name of the Process
   * @return the DAQ XML as String
   */
  String getProcessXml(final String processName);

  /**
   * Requests a list of Names for all the existing processes.
   *
   * @return a list of all process names
   */
  Collection<ProcessNameResponse> getProcessNames();

  /**
   * Returns the number of connections in the Cache
   * @return the number of connections in the Cache
   */
  int getCacheSize();


  /**
   * This method is used to synchronize subscribed data tags with the
   * server. It will ask the server to send the actual tag information for
   * all subscribed data tags. The C2MON client API will then send an update
   * to all subscribed listeners.
   * @throws CacheSynchronizationException In case a communicatin problem with the C2MON server
   *         occurs while refreshing to the tags.
   */
  void refreshDataTags() throws CacheSynchronizationException;

  /**
   * This method is used to synchronize a list subscribed data tags with the
   * server. It will ask the server to send the actual tag information for
   * all subscribed tags of the given list. The C2MON client API will then send
   * an update to all subscribed listeners.
   *
   * @param tagIds A collection of data tag id's
   * @throws NullPointerException if the Collection is <code>null</code>.
   * @throws CacheSynchronizationException In case a communicatin problem with the C2MON server
   *         occurs while refreshing to the tags.
   */
  void refreshDataTags(Collection<Long> tagIds) throws CacheSynchronizationException;

  /**
   * Unregisters an <code>AlarmListener</code> from the <code>TagManager</code>.
   * @param listener The listener to be unregistered
   * @throws JMSException
   */
  void removeAlarmListener(AlarmListener listener) throws JMSException;

  /**
   * Registers an <code>AlarmListener</code> to the <code>TagManager</code>.
   * @param listener The listener to be registered
   * @throws JMSException
   */
  void addAlarmListener(AlarmListener listener) throws JMSException;

  /**
   * Checks whether the given listener is subscribed to any data tags.
   *
   * @param listener the listener to check subscriptions for
   * @return true if the listener is subscribed, false otherwise
   */
  boolean isSubscribed(DataTagUpdateListener listener);
}
