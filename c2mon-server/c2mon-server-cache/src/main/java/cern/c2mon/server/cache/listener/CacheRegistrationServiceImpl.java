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
package cern.c2mon.server.cache.listener;

import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.cache.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.util.threadhandler.ThreadHandler;

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
  private DataTagCache dataTagCache;

  /**
   * Reference to the ControlTag cache.
   */
  private ControlTagCache controlTagCache;

  /**
   * Reference to the RuleTag cache.
   */
  private RuleTagCache ruleTagCache;

  /**
   * Reference to the Alarm cache.
   */
  private AlarmCache alarmCache;

  private CacheProperties properties;

  /**
   * Autowired constructor.
   * @param dataTagCache the DataTag cache
   * @param controlTagCache the ControlTag cache
   * @param ruleTagCache the RuleTag cache
   * @param alarmCache the alarm cache
   */
  @Autowired
  public CacheRegistrationServiceImpl(final DataTagCache dataTagCache,
                                      final ControlTagCache controlTagCache,
                                      final RuleTagCache ruleTagCache,
                                      final AlarmCache alarmCache,
                                      final CacheProperties properties) {
    super();
    this.dataTagCache = dataTagCache;
    this.controlTagCache = controlTagCache;
    this.ruleTagCache = ruleTagCache;
    this.alarmCache = alarmCache;
    this.properties = properties;
  }

  @Override
  public Lifecycle registerToAllTags(final C2monCacheListener<Tag> tagCacheListener, final int threads) {
    if (threads == 1) {
      return registerToAllTags(tagCacheListener);
    } else {
      MultiThreadedCacheListener<Tag> threadedCacheListener = new MultiThreadedCacheListener<>(tagCacheListener, QUEUE_SIZE_DEFAULT, threads);
      registerListenerToTags(threadedCacheListener);
      return threadedCacheListener;
    }
  }

  @Override
  public void registerForSupervisionChanges(CacheSupervisionListener<Tag> cacheSupervisionListener) {
    dataTagCache.registerListenerWithSupervision(cacheSupervisionListener);
    //controlTagCache.registerSynchronousListener(tagCacheListener);
    ruleTagCache.registerListenerWithSupervision(cacheSupervisionListener);
  }


  @Override
  public Lifecycle registerToAllTags(final C2monCacheListener<Tag> tagCacheListener) {
    CacheListener<Tag> wrappedCacheListener = new CacheListener<Tag>(tagCacheListener);
    registerListenerToTags(wrappedCacheListener);
    return wrappedCacheListener;
  }

  @Override
  public void registerSynchronousToAllTags(final C2monCacheListener<Tag> tagCacheListener) {
    registerListenerToTags(tagCacheListener);
  }

  //TODO needs testing
  @Override
  public Lifecycle registerToDataTags(final C2monCacheListener<DataTag> dataTagCacheListener) {
    return dataTagCache.registerListener(dataTagCacheListener);
  }

  @Override
  public Lifecycle registerToDataTags(final C2monCacheListener<DataTag> dataTagCacheListener, final int threads) {
    if (threads == 1) {
      return dataTagCache.registerListener(dataTagCacheListener);
    } else {
      return dataTagCache.registerThreadedListener(dataTagCacheListener, QUEUE_SIZE_DEFAULT, threads);
    }
  }


  @Override
  public Lifecycle registerToRuleTags(final C2monCacheListener<RuleTag> ruleTagCacheListener) {
    return ruleTagCache.registerListener(ruleTagCacheListener);
  }

  @Override
  public Lifecycle registerToRuleTags(final C2monCacheListener<RuleTag> ruleTagCacheListener, final int threads) {
    if (threads == 1) {
      return ruleTagCache.registerListener(ruleTagCacheListener);
    } else {
      return ruleTagCache.registerThreadedListener(ruleTagCacheListener, QUEUE_SIZE_DEFAULT, threads);
    }
  }

  @Override
  public Lifecycle registerBufferedListenerToTags(final C2monBufferedCacheListener<Tag> bufferListener) {
    int frequency = properties.getBufferedListenerPullFrequency();
    DefaultBufferedCacheListener<Tag> bufferedCacheListener = new DefaultBufferedCacheListener<>(bufferListener, frequency);
    registerListenerToTags(bufferedCacheListener);
    return bufferedCacheListener;
  }

  @Override
  @Deprecated
  public Lifecycle registerToAlarms(final C2monCacheListener<Alarm> cacheListener) {
    return alarmCache.registerListener(cacheListener);
  }

  @Override
  public void registerToAlarmExpressions(final ComparableCacheListener<Tag> compareCacheListener) {
    dataTagCache.registerComparableListener(compareCacheListener);
    controlTagCache.registerComparableListener(compareCacheListener);
    ruleTagCache.registerComparableListener(compareCacheListener);
  }

  private void registerListenerToTags(C2monCacheListener<Tag> cacheListener) {
    dataTagCache.registerSynchronousListener(cacheListener);
    controlTagCache.registerSynchronousListener(cacheListener);
    ruleTagCache.registerSynchronousListener(cacheListener);
  }

}
