/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cache.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.cache.CacheSupervisionListener;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
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
   * Default SynchroBuffer parameters.
   */
  public static final int BUFFER_MIN_TIME = 100;
  
  public static final int BUFFER_MAX_TIME = 1000;
  
  public static final int BUFFER_WINDOW_GROWTH = 100;
  
  
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
                                           final AlarmCache alarmCache) {
    super();
    this.dataTagCache = dataTagCache;
    this.controlTagCache = controlTagCache;
    this.ruleTagCache = ruleTagCache;
    this.alarmCache = alarmCache;
  }

  @Override
  public Lifecycle registerToAllTags(final C2monCacheListener<Tag> tagCacheListener, final int threads) {
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
  public Lifecycle registerBufferedListenerToTags(final BufferedTimCacheListener<Tag> bufferListener) {
    BufferedCacheListener<Tag> bufferedCacheListener = new BufferedCacheListener<Tag>(bufferListener);
    registerListenerToTags(bufferedCacheListener);
    return bufferedCacheListener;
  }

  @Override
  public Lifecycle registerToAlarms(final C2monCacheListener<Alarm> timCacheListener) {
    return alarmCache.registerListener(timCacheListener);
  }
  
  private void registerListenerToTags(C2monCacheListener<Tag> timCacheListener) {
    dataTagCache.registerSynchronousListener(timCacheListener);
    controlTagCache.registerSynchronousListener(timCacheListener);
    ruleTagCache.registerSynchronousListener(timCacheListener); 
  }

}
