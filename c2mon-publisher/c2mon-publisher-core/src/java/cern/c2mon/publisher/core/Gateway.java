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
package cern.c2mon.publisher.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.ConfigurationService;
import cern.c2mon.client.core.TagService;
import cern.c2mon.publisher.Publisher;
import cern.c2mon.shared.client.tag.TagConfig;


/**
 * This class subscribes to the {@link TagService} and publishes the incoming
 * data tags via RdaPublisher.
 * 
 * @author Matthias Braeger 
 */
@Service
public class Gateway implements BaseTagListener {
  /**  The Log4j's logger  */
  private static final Logger LOG = LoggerFactory.getLogger(Gateway.class);
  
  /** C2MON tag service */
  private final TagService tagService;
  
  /** C2MON tag configuration service */
  private final ConfigurationService configService;
  
  /** The list of subscribed tags */
  private final Set<Long> tagIds = new HashSet<Long>();
  
  /** The publisher instance */
  private final Publisher publisher;
  
  /** Map containing all configuration information for all subscribed tags */
  private final Map<Long, TagConfig> tagConfigs = new HashMap<Long, TagConfig>();

  /**
   * Default Constructor
   * 
   * @param publisherService The publisher service which is setup on top of the publisher
   *                         core.
   */
  @Autowired
  public Gateway(final Publisher publisherService) {
    LOG.info("Starting publisher gateway...");
    publisher = publisherService;
    
    // Initialize global Tag Manager variable
    tagService = C2monServiceGateway.getTagService();
    configService = C2monServiceGateway.getConfigurationService();
  }
  
  /**
   * Used to subscribe all data tags IDs that are specified in the
   * file.
   *  
   * @param dataTagList a file that contains a list of data tag IDs
   * @return true, if subscription was successful
   */
  protected final synchronized boolean subscribe(final File dataTagList) {
    boolean tagSubscriptionSuccessful = false;
    
    Set<Long> newTagIds = parseDataTags(dataTagList);
    
    if (!newTagIds.isEmpty()) {
      LOG.info("Found " + newTagIds.size() + " new tag IDs in TID file. Trying to subscribe...");
      while (!tagSubscriptionSuccessful) {
        try {
          // Get first the static tag configurations for the new tags 
          Collection<TagConfig> newTagConfigs = configService.getTagConfigurations(newTagIds);
          for (TagConfig tagConfig : newTagConfigs) {
            tagConfigs.put(tagConfig.getId(), tagConfig);
          }
          // Subscribe to the tags
          tagService.subscribe(newTagIds, this);
          tagSubscriptionSuccessful = true;
        }
        catch (Exception ex) {
          LOG.error("Error occured while trying to subscribe to the list of data tags.", ex);
          LOG.debug("Retrying tag subscription in 5 seconds ...");
          try { Thread.sleep(5000); } catch (InterruptedException ie) { /* Do nothing */ }
        }
      }
      
      // updating all the other tag information so that we are sure having the latest publication addresses
      updateAllTagConfigurations();
      this.tagIds.addAll(newTagIds);
    }
    else {
      updateAllTagConfigurations();
      LOG.info("No new tags to subscribe were found in TID file.");
      tagSubscriptionSuccessful = true;
    }
    
    return tagSubscriptionSuccessful;
  }
  
  /**
   * Triggers a call to the server for synchronizing all static tag configuration details.
   * This method could called from external, if you know that your configuration details
   * have changed. Otherwise, this method is anyway called everytime the TID file is
   * changed.
   */
  public synchronized void updateAllTagConfigurations() {
    LOG.info("Updating configuration information from all subscribed tags.");
    Collection<TagConfig> newTagConfigs = configService.getTagConfigurations(tagIds);
    for (TagConfig tagConfig : newTagConfigs) {
      tagConfigs.put(tagConfig.getId(), tagConfig);
    }
  }
  
  
  /**
   * Parses the given file and caches the list of data tag IDs.
   * 
   * @param dataTagList A file that contains a list of data tag IDs
   * @return The list of data tags to which we are not yet subscribed to, or
   *         otherwise an empty list.
   */
  private Set<Long> parseDataTags(final File dataTagList) {    
    LOG.debug("entering parseDataTags()...");
    final Set<Long> dataTags = new HashSet<Long>();
    BufferedReader in = null;

    
    try {
      in = new BufferedReader(new FileReader(dataTagList));
    } catch (FileNotFoundException e) {
      LOG.error("parseDataTags() - The file " + dataTagList.getAbsolutePath() + "does not exist!");
      return null;
    }
    
    if (in != null) {
      try {
        // The current line of the file
        String line = in.readLine();
        // The current line split into pieces
        String[] lineBuffer = null;
        while (line != null) {
          // We try to split the line, in case that there is more than one ID per line
          lineBuffer = line.split(",");
          if (lineBuffer != null) {
            for (int i = 0; i < lineBuffer.length; i++) {
              try {
                dataTags.add(Long.valueOf(lineBuffer[i].trim()));
                LOG.debug("parseDataTags() - Added data tag ID " + lineBuffer[i] + " to list.");
              } catch (NumberFormatException nfe) {
                // Do nothing
              }
            }
          }
          // read next line from file
          line = in.readLine();
        }
      } catch (IOException e) {
        LOG.error("parseDataTags() - An error occured while parsing the file.", e);
        return null;
      }
      finally {
        try {
          in.close();
        } catch (IOException e) {
          LOG.error("parseDataTags() - Error while closing file " + dataTagList.getAbsolutePath(), e);
          return null;
        }
      }
    }
    
    // Filtering all tags out to which we are already subscribed to
    final Set<Long> newDataTags = new HashSet<Long>();
    for (Long tagId : dataTags) {
      if (!this.tagIds.contains(tagId)) {
        newDataTags.add(tagId);
      }
    }
    
    LOG.debug("leaving parseDataTags()");
    return newDataTags;
  }

  /**
   * This method gets called when the value or quality property of a ClientDataTag is changed
   * @param tagUpdate A ClientDataTag object with updated properties
   */
  @Override
  public synchronized void onUpdate(final Tag tagUpdate) {
    if (tagUpdate.getDataTagQuality().isExistingTag()) {
      publisher.onUpdate(tagUpdate, tagConfigs.get(tagUpdate.getId()));
    }
    else {
      LOG.warn("onUpdate() - Got value update for unknown tag "
               + tagUpdate.getId() + " ==> No publication possible!");
    }
  }

  /**
   * @return The tag id list of all subscribed tags
   */
  public Set<Long> getSubscribedTagIds() {
    return tagIds;
  }
  
  
  /**
   * This class coordinates the shutdown of the {@link Gateway}. This involves, in the
   * following order:
   * 
   * <ol>
   * <li>Unsubscribing from tag updates
   * <li>Shutdown the RDA publisher
   * </ol>
   * 
   * <p>
   * Notice that the {@link Gateway} does not rely on the Spring shutdown hook to close
   * down. This means all shutdown methods must be called explicitly in the appropriate methods.
   */
  @PreDestroy
  private void shutdown() {
    LOG.info("Shutdown of JAPC publisher was triggered...");
    tagService.unsubscribe(this);
    publisher.shutdown();
  }
}
