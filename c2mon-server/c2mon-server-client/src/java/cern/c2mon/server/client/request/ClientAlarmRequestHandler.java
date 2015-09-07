/*******************************************************************************
 * This file is part of the C2MON project.
 * See http://cern.ch/c2mon
 *
 * Copyright (C) 2004 - 2015 CERN. 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: C2MON team, c2mon-support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.client.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.alarm.AlarmQuery;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * Helper class for {@link ClientRequestDelegator} to handle
 * alarm requests.
 *
 * @author Matthias Braeger
 */
@Service
class ClientAlarmRequestHandler {
  
  /** Private class logger */
  private static final Logger LOG = Logger.getLogger(ClientAlarmRequestHandler.class);
  
  /** Reference to the tag location service to check whether a tag exists */
  private final TagLocationService tagLocationService;

  /** Reference to the AlarmCache */
  private final AlarmCache alarmCache;
  
  @Autowired
  protected ClientAlarmRequestHandler(final AlarmCache alarmCache, final TagLocationService tagLocationService) {
    this.alarmCache = alarmCache;
    this.tagLocationService = tagLocationService;
  }

  /**
   * Inner method which handles the active alarm request.
   *
   * @param alarmRequest The alarm request sent from the client. It doesn't
   *          really contain any information other than the fact that this is an
   *          Active Alarms request.
   *
   * @return Collection of all the active alarms
   */
  Collection<? extends ClientRequestResult> handleActiveAlarmRequest(final ClientRequest alarmRequest) {

    final Collection<AlarmValue> activeAlarms = new ArrayList<>();

    AlarmQuery query = AlarmQuery.builder().active(true).build();
    
    Collection<Long> result = alarmCache.findAlarm(query);
    
    for (Long alarmId : result) {
        Alarm alarm = alarmCache.getCopy(alarmId);
        Tag tag = tagLocationService.getCopy(alarm.getTagId());
        if (tag == null) {
            LOG.warn("No tag found for TagID = " + alarm.getTagId() + ". This may be a configuration issue for alarm " + alarm.getId());
        } else {
            activeAlarms.add(TransferObjectFactory.createAlarmValue(alarm, tag));
        }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Finished processing ACTIVE alarms request: returning " + activeAlarms.size() + " active alarms");
    }
    return activeAlarms;
  }

  /**
   * Inner method which handles the alarm request.
   *
   * @param alarmRequest The alarm request sent from the client
   * @return Collection of alarms
   */
  Collection<? extends ClientRequestResult> handleAlarmRequest(final ClientRequest alarmRequest) {

    final Collection<AlarmValue> alarms = new ArrayList<>(alarmRequest.getTagIds().size());

    // !!! TagId field is also used for Alarm Ids
    for (Long alarmId : alarmRequest.getTagIds()) {

      if (alarmCache.hasKey(alarmId)) {
        final Alarm alarm = alarmCache.getCopy(alarmId);
        switch (alarmRequest.getResultType()) {
        case TRANSFER_ALARM_LIST:

          Long tagId = alarm.getTagId();
          if (tagLocationService.isInTagCache(tagId)) {
            Tag tag = tagLocationService.getCopy(tagId);
            alarms.add(TransferObjectFactory.createAlarmValue(alarm, tag));
          } else {
            LOG.warn("handleAlarmRequest() - unrecognized Tag with id " + tagId);
            alarms.add(TransferObjectFactory.createAlarmValue(alarm));
          }

          break;
        default:
          LOG.error("handleAlarmRequest() - Could not generate response message. Unknown enum ResultType " + alarmRequest.getResultType());
        }
      } else {
        LOG.warn("handleAlarmRequest() - request for unknown alarm with id " + alarmId);
      }

    } // end while
    if (LOG.isDebugEnabled()) {
      LOG.debug("Finished processing Alarm request: returning " + alarms.size() + " Alarms");
    }
    return alarms;
  }
}
