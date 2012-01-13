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
package cern.c2mon.japc.publisher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.shared.client.supervision.Heartbeat;


/**
 * This class publishes the incoming data tags to JAPC.
 * @author Matthias Braeger 
 */
public class JAPCGateway  implements DataTagUpdateListener, HeartbeatListener {
  /**  The Log4j's logger  */
  private static final Logger LOG = Logger.getLogger(JAPCGateway.class);
  
  /** Used to remember when the last heartbeat was received */
  private Timestamp lastHartbeat = null;
  
  /** The C2MON tag manager */
  private final C2monTagManager tagManager;

  /**
   * Default Constructor
   */
  public JAPCGateway() {
    // Initialize global Tag Manager variable
    tagManager = C2monServiceGateway.getTagManager();
    
    // Register JAPC publisher for receiving C2MON heartbeat notifications
    C2monServiceGateway.getSupervisionManager().addHeartbeatListener(this);
  }
  
  /**
   * Used to subscribe all data tags IDs that are specified in the
   * file.
   *  
   * @param dataTagList a file that contains a list of data tag IDs
   * @return true, if subscription was successful
   */
  public final boolean subscribeDataTags(final File dataTagList) {
    boolean tagSubscriptionSuccessful = false;
    
    Set<Long> tagIds = parseDataTags(dataTagList); 
    if (tagIds != null) {    
      while (!tagSubscriptionSuccessful) {
        try {
          tagSubscriptionSuccessful = tagManager.subscribeDataTags(tagIds, this);
        }
        catch (Exception ex) {
          LOG.error("error occured while trying to subscribe to the list of data tags.", ex);
          LOG.debug("retrying tag subscription in 5 seconds ...");
          try { Thread.sleep(5000); } catch (InterruptedException ie) { /* Do nothing */ }
        }
      }
    }
    
    return tagSubscriptionSuccessful;
  }
  
  
  /**
   * Parses the given file and caches the list of data tag IDs.
   * 
   * @param dataTagList A file that contains a list of data tag IDs
   * @return <code>true</code>, if the file could successfully be parsed.
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
    
    LOG.debug("leaving parseDataTags()");
    return dataTags;
  }

  /**
   * In case we receive a heartbeat we publish everything that we
   * have in our data tag cache. Except of those updates that were
   * already sent since the last heartbeat.
   * 
   * @param heartbeat The last heartbeat received from server
   */
  public final void onHeartbeatReceived(final Heartbeat heartbeat) {
    LOG.debug("entering onHeartbeatReceived()...");
    final Iterator<ClientDataTagValue> iter = tagManager.getAllSubscribedDataTags(this).iterator();
    ClientDataTagValue clientDataTag = null; // dummy
    
    if (lastHartbeat != null) { 
      while (iter.hasNext()) {
        clientDataTag = iter.next();  
        if (clientDataTag != null && clientDataTag.getTimestamp().before(lastHartbeat)) {
          JAPCPublisher.getInstance().publish(clientDataTag);
        }
      }
    }
    
    lastHartbeat = heartbeat.getTimestamp(); 
    LOG.debug("leaving onHeartbeatReceived()");
  }




  @Override
  public void onHeartbeatExpired(Heartbeat pHeartbeat) {
    // TODO Auto-generated method stub
    
  }


  @Override
  public void onHeartbeatResumed(Heartbeat pHeartbeat) {
    // TODO Auto-generated method stub
    
  }

  /**
   * This method gets called when the value or quality property of a ClientDataTag is changed
   * @param tagUpdate A ClientDataTag object with updated properties
   */
  @Override
  public void onUpdate(ClientDataTagValue tagUpdate) {
    LOG.debug("entering onUpdate()...");
    JAPCPublisher.getInstance().publish(tagUpdate);
    LOG.debug("leaving onUpdate()");
  }
}
