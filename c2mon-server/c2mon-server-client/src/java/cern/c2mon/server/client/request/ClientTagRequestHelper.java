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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.statistics.ProcessTagStatistics;
import cern.c2mon.shared.client.statistics.TagStatisticsResponse;
import cern.c2mon.shared.client.statistics.TagStatisticsResponseImpl;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * Helper class for {@link ClientRequestDelegator} to handle
 * tag requests.
 *
 * @author Matthias Braeger
 */
@Service
class ClientTagRequestHelper {
  
  /** Private class logger */
  private static final Logger LOG = Logger.getLogger(ClientTagRequestHelper.class);

  /**
   * Reference to the tag facade gateway to retrieve a tag copies with the
   * associated alarms
   */
  private final TagFacadeGateway tagFacadeGateway;
  
  /** Reference to the tag location service to check whether a tag exists */
  private final TagLocationService tagLocationService;
  
  /** Used to determine whether a Control Tag is an Alive tag */
  private final AliveTimerFacade aliveTimerFacade;
  
  /**
   * Reference to the Process cache that provides a list of all the process
   * names
   */
  private final ProcessCache processCache;
  
  /**
   * Default Constructor
   *
   * @param aliveTimerFacade Used to determine whether a given tag is an Alive tag
   * @param tagLocationService Reference to the tag location service singleton
   * @param tagFacadeGateway Reference to the tag facade gateway singleton
   * @param processCache Reference to the ProcessCache
   */
  @Autowired
  public ClientTagRequestHelper(final AliveTimerFacade aliveTimerFacade,
                          final TagLocationService tagLocationService,
                          final TagFacadeGateway tagFacadeGateway,
                          final ProcessCache processCache) {
    this.aliveTimerFacade = aliveTimerFacade;
    this.tagLocationService = tagLocationService;
    this.tagFacadeGateway = tagFacadeGateway;
    this.processCache = processCache;
  }
  
  /**
   * Inner method which handles the tag requests
   *
   * @param tagRequest The tag request sent from the client
   * @return Collection of
   */
  protected Collection<? extends ClientRequestResult> handleTagRequest(final ClientRequest tagRequest) {

    final Collection<TagValueUpdate> transferTags = new ArrayList<>(tagRequest.getTagIds().size());

    for (Long tagId : tagRequest.getTagIds()) {
      if (tagLocationService.isInTagCache(tagId)) {
        final TagWithAlarms tagWithAlarms = tagFacadeGateway.getTagWithAlarms(tagId);

        switch (tagRequest.getResultType()) {
        case TRANSFER_TAG_LIST:
          transferTags.add(TransferObjectFactory.createTransferTag(tagWithAlarms, aliveTimerFacade.isRegisteredAliveTimer(tagId)));
          break;
        case TRANSFER_TAG_VALUE_LIST:
          transferTags.add(TransferObjectFactory.createTransferTagValue(tagWithAlarms));
          break;
        default:
          LOG.error("handleTagRequest() - Could not generate response message. Unknown enum ResultType " + tagRequest.getResultType());
        }
      } else {
        LOG.warn("Received client request (TagRequest) for unrecognized Tag with id " + tagId);
      }
    } // end while
    if (LOG.isDebugEnabled()) {
      LOG.debug("Finished processing Tag request (values only): returning " + transferTags.size() + " Tags");
    }
    return transferTags;
  }
  
  /**
   * Inner method which handles the Tag Configuration Requests
   *
   * @param tagConfigurationRequest The configuration request sent from the
   *          client
   * @return A tag configuration list
   */
  protected Collection<? extends ClientRequestResult> handleTagConfigurationRequest(final ClientRequest tagConfigurationRequest) {
    
    final Collection<TagConfig> transferTags = new ArrayList<TagConfig>(tagConfigurationRequest.getTagIds().size());

    // !!! TagId field is also used for Configuration Ids
    for (Long tagId : tagConfigurationRequest.getTagIds()) {

      if (tagLocationService.isInTagCache(tagId)) {
        final TagWithAlarms tagWithAlarms = tagFacadeGateway.getTagWithAlarms(tagId);
        HashSet<Process> tagProcesses = new HashSet<Process>();
        for (Long procId : tagWithAlarms.getTag().getProcessIds()) {
          tagProcesses.add(processCache.get(procId));
        }
        switch (tagConfigurationRequest.getResultType()) {
        case TRANSFER_TAG_CONFIGURATION_LIST:
          transferTags.add(TransferObjectFactory.createTagConfiguration(tagWithAlarms, tagProcesses));
          break;
        default:
          LOG.error("handleConfigurationRequest() - Could not generate response message. Unknown enum ResultType " + tagConfigurationRequest.getResultType());
        }
      } else {
        LOG.warn("Received client request (TagConfigRequest) for unrecognized Tag with id " + tagId);
      }
    } // end while
    if (LOG.isDebugEnabled()) {
      LOG.debug("Finished processing Tag request (with config info): returning " + transferTags.size() + " Tags");
    }
    return transferTags;
  }
  
  /**
   * Inner method which handles the tag statistics request.
   *
   * @param tagStatisticsRequest the request sent by the client
   * @return a single-item collection containing the tag statistics response
   */
  Collection<? extends ClientRequestResult> handleTagStatisticsRequest(final ClientRequest tagStatisticsRequest) {
    Collection<TagStatisticsResponse> tagStatistics = new ArrayList<>();
    Map<String, ProcessTagStatistics> processes = new HashMap<>();
    int total = 0;
    int invalid = 0;

    for (Long processId : processCache.getKeys()) {
      ProcessTagStatistics processStatistics = new ProcessTagStatistics(processCache.getNumTags(processId), processCache.getNumInvalidTags(processId));

      total += processStatistics.getTotal();
      invalid += processStatistics.getInvalid();

      processes.put(processCache.get(processId).getName(), processStatistics);
    }

    tagStatistics.add(new TagStatisticsResponseImpl(total, invalid, processes));
    LOG.debug("Finished processing tag statistics request request");
    return tagStatistics;
  }
}
