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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cern.c2mon.server.client.config.ClientProperties;
import cern.c2mon.server.client.publish.TopicProvider;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
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
  private static final Logger LOG = LoggerFactory.getLogger(ClientTagRequestHelper.class);

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

  private final ClientProperties properties;

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
                                final ProcessCache processCache,
                                final ClientProperties properties) {
    this.aliveTimerFacade = aliveTimerFacade;
    this.tagLocationService = tagLocationService;
    this.tagFacadeGateway = tagFacadeGateway;
    this.processCache = processCache;
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
    
    transferTags.addAll(getTagsByProcessIds(tagRequest));
    transferTags.addAll(getTagsByEquipmentIds(tagRequest));
    transferTags.addAll(getTagsBySubEquipmentIds(tagRequest));

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
      if (tagLocationService.isInTagCache(tagId)) {
        final TagWithAlarms tagWithAlarms = tagFacadeGateway.getTagWithAlarms(tagId);

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
  
  public Collection<TagValueUpdate> getTagsByProcessIds(final ClientRequest tagRequest){
      final Collection<TagValueUpdate> transferTags = new ArrayList<>(tagRequest.getIds().size());

      for(Long id : tagRequest.getTagProcessIds()){
        try {
          final Collection<TagWithAlarms> tagsWithAlarms = tagFacadeGateway.getTagsWithAlarmsByProcessId(id);

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
              LOG.error("getTagsByProcessIds() - Could not generate response message. Unknown enum ResultType " + tagRequest.getResultType());
            }
          }
        }
        catch (CacheElementNotFoundException ex) {
          LOG.warn(String.format("getTagsByProcessIds() - Received client request (TagRequest) where the requested process ids is not matching to any Tag cache entry."));
        }
      }

      return transferTags;
    }

    public Collection<TagValueUpdate> getTagsByEquipmentIds(final ClientRequest tagRequest){
      final Collection<TagValueUpdate> transferTags = new ArrayList<>(tagRequest.getIds().size());

      for(Long id : tagRequest.getTagEquipmentIds()){
        try {
          final Collection<TagWithAlarms> tagsWithAlarms = tagFacadeGateway.getTagsWithAlarmsByEquipmentId(id);

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
              LOG.error("getTagsByEquipmentIds() - Could not generate response message. Unknown enum ResultType " + tagRequest.getResultType());
            }
          }
        }
        catch (CacheElementNotFoundException ex) {
          LOG.warn(String.format("getTagsByEquipmentIds() - Received client request (TagRequest) where the requested equipment ids is not matching to any Tag cache entry."));
        }
      }

      return transferTags;
    }

    public Collection<TagValueUpdate> getTagsBySubEquipmentIds(final ClientRequest tagRequest){
      final Collection<TagValueUpdate> transferTags = new ArrayList<>(tagRequest.getIds().size());

      for(Long id : tagRequest.getTagSubEquipmentIds()){
        try {
          final Collection<TagWithAlarms> tagsWithAlarms = tagFacadeGateway.getTagsWithAlarmsBySubEquipmentId(id);

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
              LOG.error("getTagsBySubEquipmentIds() - Could not generate response message. Unknown enum ResultType " + tagRequest.getResultType());
            }
          }
        }
        catch (CacheElementNotFoundException ex) {
          LOG.warn(String.format("getTagsBySubEquipmentIds() - Received client request (TagRequest) where the requested sub equipment ids is not matching to any Tag cache entry."));
        }
      }

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
        final Collection<TagWithAlarms> tagsWithAlarms = tagFacadeGateway.getTagsWithAlarms(regex);

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
