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
package cern.c2mon.server.cache;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.daq.lifecycle.Lifecycle;

/**
 * Service providing helper methods for registering as cache listeners to the
 * different caches (currently only <b>update</b> notifications are used.
 * 
 * <p>Notice that only thread-safe listeners should register to receive
 * notifications on multiple threads. Similarly, only thread-safe listeners
 * should register to receive notifications from multiple caches, as these
 * will be called on different threads (this includes all register...ToTags
 * methods).
 * 
 * <p>Registration methods that do not specify the number of threads will
 * by default register on a single thread for update notifications. This 
 * thread is reserved for this particular listener. Methods that specify
 * the number of threads will use several threads to call the registered
 * listener notification method.
 * 
 * @author Mark Brightwell
 *
 */
public interface CacheRegistrationService {

  /**
   * Register for all updates to data tags, control tags and rules (default registration on separate thread)
   * @param tagCacheListener the listener to notify of the updates
   * @return a Lifecycle object to allow the registered listener to stop/start its thread as required;
   *          in general, the start() method should be called at the end of its own lifecycle start method,
   *          and the stop() at the beginning of its own stop method
   */
  Lifecycle registerToAllTags(C2monCacheListener<Tag> tagCacheListener); 
  
  /**
   * Registers this listener to receive invalidation/validation
   * callbacks on changes to the supervision status of Equipment/
   * DAQs. A callback is made for every relevant DataTag/Rule.
   * 
   * <p>Is called on a single thread.
   * 
   * <p>Consider registering for updates and supervision events
   * separately using the SupervisionNotifier service.
   * 
   * @param cacheSupervisionListener listener that should be notified
   */
  void registerForSupervisionChanges(CacheSupervisionListener<Tag> cacheSupervisionListener);
  
  /**
   * Register for all updates to data tags, control tags and rules
   * (default registration on separate thread).
   * @param tagCacheListener the listener to notify of the updates
   * @param nbThreads the number of threads used to call the listener
   * @return a Lifecycle object to allow the registered listener to stop/start its thread as required;
   *          in general, the start() method should be called at the end of its own lifecycle start method,
   *          and the stop() at the beginning of its own stop method
   */
  Lifecycle registerToAllTags(C2monCacheListener<Tag> tagCacheListener, int nbThreads);
  
  /**
   * Register to be notified of updates to the DataTag cache only.
   * @param dataTagCacheListener an implementation of the {@link DataTagCacheListener} abstract class
   * @return a Lifecycle object to allow the registered listener to stop/start its thread as required;
   *          in general, the start() method should be called at the end of its own lifecycle start method,
   *          and the stop() at the beginning of its own stop method   
   */
  Lifecycle registerToDataTags(C2monCacheListener<DataTag> dataTagCacheListener);

  /**
   * Register to be notified of updates to the RuleTag cache (single threaded notification).
   * 
   * @param ruleTagCacheListener an implementation of the {@link RuleTagCacheListener} abstract class
   * @return a Lifecycle object to allow the registered listener to stop/start its thread as required;
   *          in general, the start() method should be called at the end of its own lifecycle start method,
   *          and the stop() at the beginning of its own stop method
   */
  Lifecycle registerToRuleTags(C2monCacheListener<RuleTag> ruleTagCacheListener);

  /**
   * Register to received updates to the RuleTag cache on multiple threads.
   * Only thread-safe listeners should be used here.
   * 
   * @param ruleTagCacheListener an implementation of the {@link RuleTagCacheListener} abstract class
   * @param nbThreads the number of threads on which the listener will be called
   * @return a Lifecycle object to allow the registered listener to stop/start its thread as required;
   *          in general, the start() method should be called at the end of its own lifecycle start method,
   *          and the stop() at the beginning of its own stop method
   */
  Lifecycle registerToRuleTags(C2monCacheListener<RuleTag> ruleTagCacheListener, int nbThreads);

  /**
   * Register to received updates to the DataTag cache on multiple threads.
   * Only thread-safe listeners should be used here.
   * 
   * @param dataTagCacheListener an implementation of the {@link DataTagCacheListener} abstract class
   * @param nbThreads the number of threads on which the listener will be called
   * @return a Lifecycle object to allow the registered listener to stop/start its thread as required;
   *          in general, the start() method should be called at the end of its own lifecycle start method,
   *          and the stop() at the beginning of its own stop method
   */
  Lifecycle registerToDataTags(C2monCacheListener<DataTag> dataTagCacheListener, int nbThreads);

  /**
   * Registers to all Tag caches. The notification method is called on the same thread, so
   * this method slows down all notification calls. On the other hand, the notification
   * takes place within the JMS transaction which triggered this update: in the case of
   * failure, the message will be re-delivered. If the actions of the listener must be
   * guaranteed, consider this option (the number of JMS listener threads may need increasing
   * accordingly).
   * @param tagCacheListener the listener to register
   */
  void registerSynchronousToAllTags(C2monCacheListener<Tag> tagCacheListener);

  /**
   * Registers the listener to all Tag caches (listener is called on separate threads for each cache;
   * a single thread per listener; no order guaranteed).
   * 
   * @param bufferedTimCacheListener the listener to register (called on single thread)
   * @return a Lifecycle object to allow the registered listener to stop/start its thread as required;
   *          in general, the start() method should be called at the end of its own lifecycle start method,
   *          and the stop() at the beginning of its own stop method
   */
  Lifecycle registerBufferedListenerToTags(C2monBufferedCacheListener<Tag> bufferedTimCacheListener);

  /**
   * Registers for all updates to C2MON alarms. Notice this includes all invalidations
   * of alarms due to DAQ/Equipment supervision events. This listener is called on it's 
   * own thread.
   * 
   * @param timCacheListener the listener
   * @return a Lifecycle object to allow the registered listener to stop/start its thread as required;
   *          in general, the start() method should be called at the end of its own lifecycle start method,
   *          and the stop() at the beginning of its own stop method
   */
  Lifecycle registerToAlarms(C2monCacheListener<Alarm> timCacheListener);
}
