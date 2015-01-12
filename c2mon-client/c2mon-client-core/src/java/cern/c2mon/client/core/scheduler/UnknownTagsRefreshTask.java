/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
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
package cern.c2mon.client.core.scheduler;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.core.cache.ClientDataTagCache;

/**
 * Spring SchedulerTask which is executed every 5 minutes to ask
 * the server whether the unknown tags have been configured in meantime.
 *
 * @author Matthias Braeger
 */
@Configuration
@EnableScheduling
class UnknownTagsRefreshTask {
  
  /**
   * The cache instance which is managing all <code>ClientDataTag</code> objects
   */
  @Autowired
  private ClientDataTagCache cache;
  
  /** Logger instance */
  private static Logger LOG = Logger.getLogger(UnknownTagsRefreshTask.class);
   
  /**
   * This method is called every 5 minutes in order to refresh all Tags which are
   * currently marked as unknown.
   */
  @Scheduled(fixedRate=300000, initialDelay=300000)
  public void refreshAllUnknownTags() {
    if (LOG.isTraceEnabled()) {
      LOG.trace("refreshAllUnknownTags() - Start refreshing all unknown tags ... ");
    }
    Set<Long> unknownTags = new HashSet<>();
    
    try {
      if (!cache.isHistoryModeEnabled()) {
        for (ClientDataTag cdt : cache.getAllSubscribedDataTags()) {
          if (!cdt.getDataTagQuality().isExistingTag()) {
            unknownTags.add(cdt.getId());
          }
        }
        
        if (!unknownTags.isEmpty()) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("refreshAllUnknownTags() - trying to refresh " + unknownTags.size() + " tags marked as UNKNOWN ...");
          }
          cache.refresh(unknownTags);
        }
      
      } // end if block
    }
    catch (Exception e) {
      LOG.error("refreshAllUnknownTags() - An error occured while trying to refresh all unknown tags from the server.", e);
    }
    
    if (LOG.isTraceEnabled()) {
      LOG.trace("refreshAllUnknownTags() - Scheduled task finished. Next call in 5 minutes.");
    }
  }
}
