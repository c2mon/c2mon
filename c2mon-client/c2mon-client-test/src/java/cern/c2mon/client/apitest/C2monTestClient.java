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
package cern.c2mon.client.apitest;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;

/**
 * Client application that subscribes to a random collection of tags from a
 * text file. Specify the number of tags with the variable c2mon.client.test.subscription.number.
 *
 * <p>The text file expects one tag id per line. The file location needs storing in the
 * property c2mon.client.test.tagids.location
 *
 * <p>Incoming tags are logged in the log named c2mon.client.test.log.name.
 *
 * @author Mark Brightwell
 *
 */
public class C2monTestClient {

  private static final Logger log = LoggerFactory.getLogger(C2MonClientApiTest.class);

  /**
   * Log4j Logger for logging DataTag values.
   */
  protected static final Logger TAG_LOG = LoggerFactory.getLogger("ClientDataTagValueLogger");

  /**
   * @param args
   */
  public static void main(String[] args) {

    try {
      C2monServiceGateway.startC2monClientSynchronous();
    } catch (Exception e) {
      log.error(e.toString());
      System.exit(-1);
    }

    //parse text file
    ArrayList<Long> tagIds = new ArrayList<Long>();
    try {
      BufferedReader br = null;
      try {
        URL url = ResourceUtils.getURL(System.getProperty("c2mon.client.test.tagids.location"));
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        String currentLine;
        while ((currentLine = br.readLine()) != null) {
          tagIds.add(Long.valueOf(currentLine.trim()));
        }
      } finally {
        if (br != null)
          br.close();
      }
    } catch (Exception e) {
      log.error("Error while reading tag id file", e);
    }
   log.info("Found " + tagIds.size() + " tag ids in file.");

    //get random subset
    Set<Long> subscriptionIds = getRandomSubset(tagIds, Integer.valueOf(System.getProperty("c2mon.client.test.subscription.number")));

    //subscribe to random subset of these tags
    C2monTagManager tagManager = C2monServiceGateway.getTagManager();
    log.info(format("trying to subscribe to %s metrics", subscriptionIds.size()));
    tagManager.subscribeDataTags(subscriptionIds, new DataTagUpdateListener() {
      @Override
      public void onUpdate(ClientDataTagValue tagUpdate) {
        if (TAG_LOG.isInfoEnabled()) {
          TAG_LOG.info(tagUpdate.toString());
        }
      }

    });

  }

  private static Set<Long> getRandomSubset(List<Long> tagIds, Integer subsetSize) {
    if (tagIds.size() < subsetSize) {
      throw new IllegalArgumentException("Requested subset size smaller than set size!");
    }
    Random random = new Random();
    HashSet<Long> randomSubset = new HashSet<Long>();
    int setSize = tagIds.size();
    if (subsetSize > setSize/2) {
      Set<Long> oppositeSet = getRandomSubset(tagIds, setSize-subsetSize);
      for (Long item : tagIds) {
        if (!oppositeSet.contains(item))
          randomSubset.add(item);
      }
    } else {
      while (randomSubset.size() < subsetSize) {
        int randomLocation = random.nextInt(setSize);
        randomSubset.add(tagIds.get(randomLocation));
      }
    }
    return randomSubset;
  }

}
