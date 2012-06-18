/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.publisher.Publisher;
import cern.c2mon.shared.client.tag.TagConfig;


/**
 * This class subscribes to the {@link C2monTagManager} and publishes the incoming
 * data tags via RdaPublisher.
 * 
 * @author Matthias Braeger 
 */
@Service
public class Gateway implements DataTagUpdateListener {
  /**  The Log4j's logger  */
  private static final Logger LOG = Logger.getLogger(Gateway.class);
  
  /** The C2MON tag manager */
  private final C2monTagManager tagManager;
  
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
    tagManager = C2monServiceGateway.getTagManager();
  }
  
  /**
   * Used to subscribe all data tags IDs that are specified in the
   * file.
   *  
   * @param dataTagList a file that contains a list of data tag IDs
   * @return true, if subscription was successful
   */
  protected final synchronized boolean subscribeDataTags(final File dataTagList) {
    boolean tagSubscriptionSuccessful = false;
    
    Set<Long> newTagIds = parseDataTags(dataTagList);
    
    if (!newTagIds.isEmpty()) {
      LOG.info("Found " + newTagIds.size() + " new tag IDs in TID file. Trying to subscribe...");
      while (!tagSubscriptionSuccessful) {
        try {
          // Get first the static tag configurations for the new tags 
          Collection<TagConfig> newTagConfigs = tagManager.getTagConfigurations(newTagIds);
          for (TagConfig tagConfig : newTagConfigs) {
            tagConfigs.put(tagConfig.getId(), tagConfig);
          }
          // Subscribe to the tags
          tagSubscriptionSuccessful = tagManager.subscribeDataTags(newTagIds, this);
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
    Collection<TagConfig> newTagConfigs = tagManager.getTagConfigurations(tagIds);
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
    int counter = 0;
    
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
                counter++;
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
      
      try {
        in.close();
      } catch (IOException e) {
        LOG.error("parseDataTags() - Error while closing file " + dataTagList.getAbsolutePath(), e);
        return null;
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
  public synchronized void onUpdate(final ClientDataTagValue tagUpdate) {
    publisher.onUpdate(tagUpdate, tagConfigs.get(tagUpdate.getId()));
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
    tagManager.unsubscribeAllDataTags(this);
    publisher.shutdown();
  }
}
