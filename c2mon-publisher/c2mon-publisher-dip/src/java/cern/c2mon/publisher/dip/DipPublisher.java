/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can
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
package cern.c2mon.publisher.dip;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.publisher.Publisher;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.dip.Dip;
import cern.dip.DipData;
import cern.dip.DipException;
import cern.dip.DipFactory;
import cern.dip.DipPublication;
import cern.dip.DipPublicationErrorHandler;
import cern.dip.DipQuality;
import cern.dip.DipTimestamp;
import cern.tim.shared.common.datatag.DataTagQuality;


/**
 * This classes manages the publication of the subscribed data tags
 * via the DIP protocol.
 *
 * @author Matthias Braeger
 */
@Service
public class DipPublisher implements Publisher {

  /** Log4j logger instance */
  private static final Logger LOG = Logger.getLogger(DipPublisher.class);
  
  /** The value field name which is used within a DIP publication */
  public static final String VALUE_FIELD_NAME = "value";
  
  /**
   * Maps the {@link DipPublication} objects to the tag id. For each tag subscription there is
   * exactly one entry registered in this map.
   */
  private final Map<Long, DipPublication> publications = new HashMap<Long, DipPublication>();
  
  /**
   * Allows to manage the life cycle management of publications and subscriptions.
   */
  private final DipFactory dipFactory;
  
  @Autowired
  public DipPublisher(@Value("${c2mon.publisher.dip.server.name}") final String serverName) {
    dipFactory = Dip.create(serverName);
    LOG.info("Created new DIP publisher: " + serverName);
  }
  
  @Override
  public void onUpdate(ClientDataTagValue cdt, TagConfig cdtConfig) {
    // Saves the received value into a separate file
    Logger logger = Logger.getLogger("ClientDataTagLogger");
    logger.debug(cdt);
    
    if (LOG.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("received tag update [\n");
      str.append(" id          : " + cdt.getId() + "\n");
      str.append(" name        : " + cdt.getName() + "\n");
      str.append(" type        : " + cdt.getType() + "\n");
      str.append(" value       : " + cdt.getValue() + "\n");
      str.append(" valueDescr  : " + cdt.getValueDescription() + "\n");
      str.append(" description : " + cdt.getDescription() + "\n");
      str.append(" isValid     : " + cdt.isValid() + "\n");
      str.append(" qdescription: " + cdt.getDataTagQuality().getDescription() + "\n");
      str.append(" qcode       : " + cdt.getDataTagQuality().toString() + "\n]");

      LOG.debug(str.toString());        
    }
    
    if (cdt.getDataTagQuality().isExistingTag() && cdtConfig != null) {
      String dipAddress = cdtConfig.getDipPublication();
      
      if (dipAddress == null) {
        LOG.error("No DIP publication address defined for tag " + cdt.getId() + " ==> No DIP publication possible!");
      }
      else {
        try {
          if (!publications.containsKey(cdt.getId())) {
            publications.put(cdt.getId(), createDipPublication(dipAddress.trim()));
          }
          
          DipPublication publication = publications.get(cdt.getId());
          // Check, if address has changed
          if (!dipAddress.trim().equalsIgnoreCase(publication.getTopicName())) {
            LOG.info("DIP publication address for tag " + cdt.getId() + " has changed! Destoying old publication: " + publication.getTopicName());
            dipFactory.destroyDipPublication(publication);
            publication = createDipPublication(dipAddress.trim());
            publications.put(cdt.getId(), publication);
          }
          
          publishTag(cdt, publication);
        }
        catch (DipException ex) {
          LOG.error("Error occured while creating DipPublication object for tag "
              + cdt.getId() + " ==> No DIP publication possible!", ex);
        }
      }
    }
    else {
      LOG.warn("Got value update for not existing tag "
          + cdt.getId() + " ==> No DIP publication possible!");
    }
  }

  @Override
  public void shutdown() {
    for (DipPublication pub : publications.values()) {
      try {
        dipFactory.destroyDipPublication(pub);
      }
      catch (Exception e) {
        LOG.warn("Error occured while trying to destroy DIP publication: " + pub.getTopicName());
      }
    }
  }
  
  /**
   * Creates a new DIP publication object. 
   * @param dipAddress The dip publication topic
   * @return A new DIP publication object
   * @throws DipException In case of errors during the creation
   */
  private DipPublication createDipPublication(final String dipAddress) throws DipException {
    return dipFactory.createDipPublication(dipAddress, new DIPErrHandler());
  }
  
  /**
   * Inner method which publishes the tag value. We assume that stage we have a valid
   * {@link DipPublication} object for this tag.
   * 
   * @param cdt The tag update value
   * @param pub DIP publication address
   */
  private void publishTag(final ClientDataTagValue cdt, final DipPublication pub) {
    DipTimestamp ts = new DipTimestamp();
    ts.setMillis(System.currentTimeMillis());
    DipData data = dipFactory.createDipData();

    try {
      switch (cdt.getTypeNumeric()) {
        case TYPE_BOOLEAN:
          data.insert(VALUE_FIELD_NAME, ((Boolean) cdt.getValue()).booleanValue());
          break;
        case TYPE_BYTE:
          data.insert(VALUE_FIELD_NAME, ((Byte) cdt.getValue()).byteValue());
          break;
        case TYPE_DOUBLE:
          data.insert(VALUE_FIELD_NAME, ((Double) cdt.getValue()).doubleValue());
          break;
        case TYPE_FLOAT:
          data.insert(VALUE_FIELD_NAME, ((Float) cdt.getValue()).floatValue());
          break;
        case TYPE_INTEGER:
          data.insert(VALUE_FIELD_NAME, ((Integer) cdt.getValue()).intValue());
          break;
        case TYPE_LONG:
          data.insert(VALUE_FIELD_NAME, ((Long) cdt.getValue()).longValue());
          break;
        case TYPE_SHORT:
          data.insert(VALUE_FIELD_NAME, ((Short) cdt.getValue()).shortValue());
          break;
        case TYPE_STRING:
          data.insert(VALUE_FIELD_NAME, ((String) cdt.getValue()));
          break;
        default:
          LOG.error("\ttag's type is UNKNOWN! for tag " + cdt.getId());
          LOG.info("\tthe tag update for tag " + cdt.getId() + " will not be forwarded to DIP");
      }

      if (cdt.getDataTagQuality().isExistingTag()) {
        LOG.debug("\ttag's type: " + cdt.getType().getName());

        data.insert("id", cdt.getId().longValue());
        data.insert("valueDescription", cdt.getValueDescription());
        data.insert("timestamp", cdt.getServerTimestamp().getTime());
        data.insert("sourceTimestamp", cdt.getTimestamp().getTime());
        data.insert("unit", cdt.getUnit());
        data.insert("name", cdt.getName());
        data.insert("description", cdt.getDescription());
        data.insert("mode", cdt.getMode().toString());
        data.insert("simulated", cdt.isSimulated());
      
      
        if (cdt.isValid()) {
          LOG.debug("\tsending the value via DIP");
          pub.send(data, ts);
        }
        else { // the quality != OK
          DataTagQuality tagQuality = cdt.getDataTagQuality();
          if (!tagQuality.isAccessible()) {
            LOG.debug("setting DIP tag " + cdt.getId() + "to inaccessible. Description : " + cdt.getDataTagQuality().getDescription());
            pub.send(data, ts, DipQuality.DIP_QUALITY_UNCERTAIN, tagQuality.getDescription());
          }
          else if (!tagQuality.isInitialised()) {
            LOG.debug("setting publication QUALITY to BAD for tag " + cdt.getId() + ". Reason: not initialized");
            pub.send(data, ts, DipQuality.DIP_QUALITY_UNINITIALIZED, "Value has not correctly been initialized by the source.");
          }
          else if (cdt.getDataTagQuality().getDescription() != null) {
            LOG.debug("setting publication QUALITY to BAD for tag " + cdt.getId() + ". Descr: " + cdt.getDataTagQuality().getDescription());
            pub.send(data, ts, DipQuality.DIP_QUALITY_BAD, cdt.getDataTagQuality().getDescription());
          }
          else {
            LOG.debug("setting publication QUALITY to BAD for tag " + cdt.getId());
            pub.send(data, ts, DipQuality.DIP_QUALITY_BAD, "Unknown reason");
          }
        }
      }
      else {
        LOG.warn("setting publication QUALITY to BAD for tag " + cdt.getId() + " - Reason: UNKNOWN");
        pub.setQualityBad("The requested TIM tag " + cdt.getId()
            + " is not known by the system or the DIP publisher for TIM has a communication problem with the server.");
      }
    }
    catch (DipException ex) {
      LOG.error("DipException was caught", ex);
    }
    catch (Exception ex) {
      LOG.error("An exception was caught", ex);
    }
    
  }
  
  /**
   * This class implements DIP error handler
   */
  private static class DIPErrHandler implements DipPublicationErrorHandler {
    /**
     * This method implements the error handler
     */
    public void handleException(final DipPublication dp, final DipException de) {
      LOG.error("Publication source " + dp.getTopicName() + " has error: " + de.getMessage());
    }
  } 

}
