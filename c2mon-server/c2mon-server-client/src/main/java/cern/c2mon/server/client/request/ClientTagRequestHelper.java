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

import cern.c2mon.cache.actions.alarm.AlarmService;
import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.client.config.ClientProperties;
import cern.c2mon.server.client.publish.TopicProvider;
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
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for {@link ClientRequestDelegator} to handle
 * tag requests.
 *
 * @author Matthias Braeger
 */
@Service
class ClientTagRequestHelper {

  /** Private class logger */
  private static final Logger LOG = LoggerFactory.getLogger(ClientTagRequestHelper.class);

  /**
   * Reference to the tag facade gateway to retrieve a tag copies with the
   * associated alarms
   */
  private final AlarmService alarmService;

  /** Reference to the tag location service to check whether a tag exists */
  private final TagCacheCollection unifiedTagCacheFacade;

  /** Used to determine whether a Control Tag is an Alive tag */
  private final AliveTagService aliveTimerFacade;

  /**
   * Reference to the Process cache that provides a list of all the process
   * names
   */
  private final C2monCache<Process> processCache;

  /**
   * Used for statistics
   */
  private final ProcessDAO processDAO;

  private final ClientProperties properties;

  /**
   * Default Constructor
   *  @param aliveTimerFacade Used to determine whether a given tag is an Alive tag
   * @param unifiedTagCacheFacade Reference to the tag location service singleton
   * @param alarmService Reference to the tag facade gateway singleton
   * @param processCache Reference to the ProcessCache
   * @param processDAO
   */
  @Autowired
  public ClientTagRequestHelper(final AliveTagService aliveTimerFacade,
                                final TagCacheCollection unifiedTagCacheFacade,
                                final AlarmService alarmService,
                                final C2monCache<Process> processCache,
                                final ProcessDAO processDAO, final ClientProperties properties) {
    this.aliveTimerFacade = aliveTimerFacade;
    this.unifiedTagCacheFacade = unifiedTagCacheFacade;
    this.alarmService = alarmService;
    this.processCache = processCache;
    this.processDAO = processDAO;
    this.properties = properties;
  }

  /**
   * Handles the tag requests
   *
   * @param tagRequest The tag request sent from the client
   * @return Collection of
   */
  Collection<? extends ClientRequestResult> handleTagRequest(final ClientRequest tagRequest) {

    final Collection<TagValueUpdate> transferTags = new ArrayList<>(tagRequest.getIds().size());

    transferTags.addAll(getTagsById(tagRequest));
    transferTags.addAll(getTagsByRegex(tagRequest));

    if (LOG.isDebugEnabled()) {
      LOG.debug("Finished processing Tag request (values only): returning " + transferTags.size() + " Tags");
    }

    return transferTags;
  }

  /**
   * Retrieves all tags specified by the tag id list in the tag request
   * @param tagRequest request containing the list of tag ids to return
   * @return List of {@link TagValueUpdate}
   */
  private Collection<TagValueUpdate> getTagsById(final ClientRequest tagRequest) {
    final Collection<TagValueUpdate> transferTags = new ArrayList<>(tagRequest.getIds().size());

    for (Long tagId : tagRequest.getIds()) {
      if (unifiedTagCacheFacade.containsKey(tagId)) {
        final TagWithAlarms tagWithAlarms = alarmService.getTagWithAlarmsAtomically(tagId);

        switch (tagRequest.getResultType()) {
        case TRANSFER_TAG_LIST:
          TransferTagImpl transferTag = TransferObjectFactory.createTransferTag(tagWithAlarms,
              aliveTimerFacade.isRegisteredAliveTimer(tagId), TopicProvider.topicFor(tagWithAlarms.getTag(), properties));
          transferTags.add(transferTag);
          break;
        case TRANSFER_TAG_VALUE_LIST:
          transferTags.add(TransferObjectFactory.createTransferTagValue(tagWithAlarms));
          break;
        default:
          LOG.error("getTagsById() - Could not generate response message. Unknown enum ResultType " + tagRequest.getResultType());
        }
      } else {
        LOG.warn("getTagsById() - Received client request (TagRequest) for unrecognized Tag with id " + tagId);
      }
    } // end while

    return transferTags;
  }

  /**
   * Retrieves all tags which are matching the given regular expressions
   * @param tagRequest the request containing the regular expressions
   * @return List of tag updates matching the provided regular expressions
   */
  private Collection<TagValueUpdate> getTagsByRegex(final ClientRequest tagRequest) {
    final Collection<TagValueUpdate> transferTags = new ArrayList<>(tagRequest.getRegexList().size());

    for (String regex : tagRequest.getRegexList()) {

      try {
        final Collection<TagWithAlarms> tagsWithAlarms = unifiedTagCacheFacade
          .findByNameRegex(regex)
          .stream()
          .map(Cacheable::getId)
          .map(alarmService::getTagWithAlarmsAtomically)
          .collect(Collectors.toList());

        for (TagWithAlarms tagWithAlarms : tagsWithAlarms) {
          switch (tagRequest.getResultType()) {
          case TRANSFER_TAG_LIST:
            transferTags.add(TransferObjectFactory.createTransferTag(tagWithAlarms,
                aliveTimerFacade.isRegisteredAliveTimer(tagWithAlarms.getTag().getId()),
                TopicProvider.topicFor(tagWithAlarms.getTag(), properties)));
            break;
          case TRANSFER_TAG_VALUE_LIST:
            transferTags.add(TransferObjectFactory.createTransferTagValue(tagWithAlarms));
            break;
          default:
            LOG.error("getTagsByRegex() - Could not generate response message. Unknown enum ResultType " + tagRequest.getResultType());
          }
        }
      }
      catch (CacheElementNotFoundException ex) {
        LOG.warn(String.format("getTagsByRegex() - Received client request (TagRequest) where the requested name \"%s\" is not matching to any Tag cache entry.", regex));
      }

    } // end for loop

    return transferTags;
  }

  /**
   * Handles the Tag Configuration Requests
   *
   * @param tagConfigurationRequest The configuration request sent from the
   *          client
   * @return A tag configuration list
   */
  Collection<? extends ClientRequestResult> handleTagConfigurationRequest(final ClientRequest tagConfigurationRequest) {

    final Collection<TagConfig> transferTags = new ArrayList<TagConfig>(tagConfigurationRequest.getIds().size());

    for (Long tagId : tagConfigurationRequest.getIds()) {

      if (unifiedTagCacheFacade.containsKey(tagId)) {
        final TagWithAlarms tagWithAlarms = alarmService.getTagWithAlarmsAtomically(tagId);
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
        LOG.warn("handleConfigurationRequest() - Received client request (TagConfigRequest) for unrecognized Tag with id " + tagId);
      }
    } // end while
    if (LOG.isDebugEnabled()) {
      LOG.debug("handleConfigurationRequest() - Finished processing Tag request (with config info): returning " + transferTags.size() + " Tags");
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
      ProcessTagStatistics processStatistics = new ProcessTagStatistics(processDAO.getNumTags(processId), processDAO.getNumInvalidTags(processId));

      total += processStatistics.getTotal();
      invalid += processStatistics.getInvalid();

      processes.put(processCache.get(processId).getName(), processStatistics);
    }

    tagStatistics.add(new TagStatisticsResponseImpl(total, invalid, processes));
    LOG.debug("Finished processing tag statistics request request");
    return tagStatistics;
  }
}
