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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.listener.CacheRegistrationService;
import cern.c2mon.cache.api.listener.CacheSupervisionListener;
import cern.c2mon.cache.api.listener.impl.DefaultBufferedCacheListener;
import cern.c2mon.cache.api.listener.impl.MultiThreadedCacheListener;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.util.threadhandler.ThreadHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the cache registration service bean.
 *
 * <p>Internally, registration on a single thread uses the
 * {@link ThreadHandler} implementation while registrations
 * on several threads use the {@link MultiThreadedCacheListener}
 * and the Java concurrency library.
 *
 * @author Mark Brightwell
 *
 */
@Service
public class CacheRegistrationServiceImpl implements CacheRegistrationService {

  /**
   * Size of the queue in which update events are
   * stored.
   *
   * <p>Update events are stored in a queue while waiting
   * to be passed to the listener. If the queue fills up,
   * the cache notification thread will have to wait,
   * which will slow down the server (resulting in JMS
   * queue increases if endemic in the listener module).
   */
  public static final int QUEUE_SIZE_DEFAULT = Integer.MAX_VALUE;

  /**
   * Reference to the DataTag cache.
   */
  private C2monCache<DataTag> dataTagCache;

  /**
   * Reference to the ControlTag cache.
   */
  private C2monCache<ControlTag> controlTagCache;

  /**
   * Reference to the RuleTag cache.
   */
  private C2monCache<RuleTag> ruleTagCache;

  /**
   * Reference to the Alarm cache.
   */
  private C2monCache<Alarm> alarmCache;

  private CacheProperties properties;

  /**
   * Autowired constructor.
   * @param dataTagCache the DataTag cache
   * @param controlTagCache the ControlTag cache
   * @param ruleTagCache the RuleTag cache
   * @param alarmCache the alarm cache
   */
  @Autowired
  public CacheRegistrationServiceImpl(final C2monCache<DataTag> dataTagCache,
                                      final C2monCache<ControlTag> controlTagCache,
                                      final C2monCache<RuleTag> ruleTagCache,
                                      final C2monCache<Alarm> alarmCache,
                                      final CacheProperties properties) {
    super();
    this.dataTagCache = dataTagCache;
    this.controlTagCache = controlTagCache;
    this.ruleTagCache = ruleTagCache;
    this.alarmCache = alarmCache;
    this.properties = properties;
  }

  @Override
  public Lifecycle registerToAllTags(final CacheListener<Tag> tagCacheListener, final int threads) {
    if (threads == 1) {
      return registerToAllTags(tagCacheListener);
    } else {
      MultiThreadedCacheListener<Tag> threadedCacheListener = new MultiThreadedCacheListener<Tag>(tagCacheListener, QUEUE_SIZE_DEFAULT, threads);
      registerListenerToTags(threadedCacheListener);
      return threadedCacheListener;
    }
  }

  @Override
  public void registerForSupervisionChanges(CacheSupervisionListener<Tag> cacheSupervisionListener) {
//    dataTagCache.registerListenerWithSupervision(cacheSupervisionListener);
//    ruleTagCache.registerListenerWithSupervision(cacheSupervisionListener);
  }


  @Override
  public Lifecycle registerToAllTags(final CacheListener<Tag> tagCacheListener) {
//    CacheListener<Tag> wrappedCacheListener = new CacheListener<Tag>(tagCacheListener);
//    registerListenerToTags(wrappedCacheListener);
//    return wrappedCacheListener;
    return null;
  }

  @Override
  public void registerSynchronousToAllTags(final CacheListener<Tag> tagCacheListener) {
    registerListenerToTags(tagCacheListener);
  }

  //TODO needs testing
  @Override
  public Lifecycle registerToDataTags(final CacheListener<DataTag> dataTagCacheListener) {
    return dataTagCache.registerListener(dataTagCacheListener);
  }

  @Override
  public Lifecycle registerToDataTags(final CacheListener<DataTag> dataTagCacheListener, final int threads) {
    if (threads == 1) {
      return dataTagCache.registerListener(dataTagCacheListener);
    } else {
      return dataTagCache.registerThreadedListener(dataTagCacheListener, QUEUE_SIZE_DEFAULT, threads);
    }
  }


  @Override
  public Lifecycle registerToRuleTags(final CacheListener<RuleTag> ruleTagCacheListener) {
    return ruleTagCache.registerListener(ruleTagCacheListener);
  }

  @Override
  public Lifecycle registerToRuleTags(final CacheListener<RuleTag> ruleTagCacheListener, final int threads) {
    if (threads == 1) {
      return ruleTagCache.registerListener(ruleTagCacheListener);
    } else {
      return ruleTagCache.registerThreadedListener(ruleTagCacheListener, QUEUE_SIZE_DEFAULT, threads);
    }
  }

  @Override
  public Lifecycle registerBufferedListenerToTags(final BufferedCacheListener<Tag> bufferListener) {
//    int frequency = properties.getBufferedListenerPullFrequency();
//    DefaultBufferedCacheListener<Tag> bufferedCacheListener = new DefaultBufferedCacheListener<>(bufferListener, frequency);
//    registerListenerToTags(bufferedCacheListener);
//    return bufferedCacheListener;
    return null;
  }

  @Override
  public Lifecycle registerToAlarms(final CacheListener<Alarm> cacheListener) {
    return alarmCache.registerListener(cacheListener);
  }

  private void registerListenerToTags(CacheListener<Tag> cacheListener) {
    dataTagCache.registerSynchronousListener(cacheListener);
    controlTagCache.registerSynchronousListener(cacheListener);
    ruleTagCache.registerSynchronousListener(cacheListener);
  }

}
