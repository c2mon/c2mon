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
package cern.c2mon.server.client.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Helper class for {@link ClientRequestDelegator} to handle
 * requests of expressions with alarms.
 *
 * @author Franz Ritter
 */
@Slf4j
@Service
class ClientAlarmRequestHandlerNew {

  /** Reference to the tag location service to check whether a tag exists */
  private final TagLocationService tagLocationService;

  @Autowired
  protected ClientAlarmRequestHandlerNew(final TagLocationService tagLocationService) {
    this.tagLocationService = tagLocationService;
  }

  /**
   * Inner method which handles the active alarm request.
   *
   * @return Collection of all tags with active alarms
   */
  Collection<? extends ClientRequestResult> handleActiveAlarmRequest() {

    List<Tag> activeAlarmsFromCache = tagLocationService.getTagsWithActiveAlarms();
    Collection<TransferTagValueImpl> activeAlarmsForClient = activeAlarmsFromCache.stream()
        .map(TransferObjectFactory::createTransferTagValue)
        .collect(Collectors.toList());

    log.debug("Finished processing ACTIVE alarms request: returning {} active alarms", activeAlarmsForClient.size());
    return activeAlarmsForClient;
  }

  /**
   * Inner method which handles the alarm request.
   *
   * @param alarmRequest the alarm request sent from the client
   * @return Collection of alarms
   */
  Collection<? extends ClientRequestResult> handleAlarmRequest(final ClientRequest alarmRequest) {

    final Collection<TransferTagValueImpl> alarms = new ArrayList<>();

    for (Long tagId : alarmRequest.getIds()) {
      if (!alarmRequest.getResultType().equals(ClientRequest.ResultType.TRANSFER_ALARM_LIST)) {
        log.error("handleAlarmRequest() - Could not generate response message. Unknown enum ResultType {}",
            alarmRequest.getResultType());

      } else {
        if (tagLocationService.isInTagCache(tagId)) {
          Tag tag = tagLocationService.getCopy(tagId);
          alarms.add(TransferObjectFactory.createTransferTagValue(tag));
        } else {
          log.warn("handleAlarmRequest() - unrecognized Tag with id {}", tagId);
        }
      }
    }
    log.debug("Finished processing Alarm request: returning {} Alarms", alarms.size());
    return alarms;
  }
}
