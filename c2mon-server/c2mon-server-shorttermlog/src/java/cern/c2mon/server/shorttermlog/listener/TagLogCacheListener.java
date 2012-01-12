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
package cern.c2mon.server.shorttermlog.listener;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.shorttermlog.logger.BatchLogger;
import cern.tim.server.cache.BufferedTimCacheListener;
import cern.tim.server.cache.CacheRegistrationService;
import cern.tim.server.common.tag.Tag;
import cern.tim.server.common.thread.Event;
import cern.tim.util.buffer.PullEvent;
import cern.tim.util.buffer.PullException;
import cern.tim.util.buffer.SynchroBufferListener;

/**
 * Listens to updates in the Rule and DataTag caches and calls the DAO
 * for logging these to the database (STL account).
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class TagLogCacheListener implements BufferedTimCacheListener<Tag> {

  /**
   * Reference to registration service.
   */
  private CacheRegistrationService cacheRegistrationService;
  
  /**
   * Bean that logs Tags into the STL.
   */
  private BatchLogger<Tag> tagLogger;
  
  /**
   * Autowired constructor.
   * 
   * @param cacheRegistrationService for registering cache listeners
   * @param tagLogger for logging cache objects to the STL
   */
  @Autowired
  public TagLogCacheListener(final CacheRegistrationService cacheRegistrationService, final BatchLogger<Tag> tagLogger) {
    super();
    this.cacheRegistrationService = cacheRegistrationService;
    this.tagLogger = tagLogger;
  }

  /**
   * Registers to be notified of all Tag updates (data, rule and control tags).
   */
  @PostConstruct
  public void init() {
    cacheRegistrationService.registerBufferedListenerToTags(this);
  }

  @Override
  public void confirmStatus(Collection<Tag> tagCollection) {
    //do not log confirm callbacks (STL data not essential)
  }

  @Override
  public void notifyElementUpdated(Collection<Tag> tagCollection) { 
    tagLogger.log(tagCollection);
  }
 

}
