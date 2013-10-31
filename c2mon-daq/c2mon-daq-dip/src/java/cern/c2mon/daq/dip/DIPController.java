/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.dip;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import cern.dip.DipException;
import cern.dip.DipFactory;
import cern.dip.DipSubscription;
import cern.c2mon.daq.common.EquipmentLogger;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.tim.shared.common.datatag.address.DIPHardwareAddress;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * DIP Controller to control tag configuration
 * 
 * @author vilches
 *
 */
public class DIPController {
  
  /**
   * The equipment logger of this class.
   */
  private EquipmentLogger equipmentLogger;
  
  /**
   * The equipment configuration of this handler.
   */
  private IEquipmentConfiguration equipmentConfiguration;
  
  /**
   * The equipment message sender to send to the server.
   */
  private IEquipmentMessageSender equipmentMessageSender;
  
//  /** A vector of handled dip subscriptions */
//  private final List<DipSubscription> dipSubscriptions = new Vector<DipSubscription>();
//
//  /** A vector of for topics that the handler subscribes to */
//  private List<String> dipSubscribedItems = new Vector<String>();
  
  /**
   * A HashMap with topics that the handler subscribes to as keys and
   * a its handled dip subscriptions as value
   */
  private final ConcurrentHashMap<String, DipSubscription> dipSubscriptions = new ConcurrentHashMap<String, DipSubscription>();


  /**
   * A HashMap for handling quick lookups for a data tags related with
   * particular topics.
   */
  private final ConcurrentHashMap<String, Vector<ISourceDataTag>> subscribedDataTags = new ConcurrentHashMap<String, Vector<ISourceDataTag>>();
  
  /** The DIP object */
  private DipFactory dipFactory;

  /** The dip message handler object used for handling dip callbacks. */
  private DipMessageHandlerDataListener handler;

  
  /**
   * Default constructor
   * 
   */
  public DIPController() {}
  
  /**
   * Constructor
   * 
   * @param dipFactory
   * @param handler
   * @param equipmentLogger
   * @param equipmentConfiguration
   * @param equipmentMessageSender
   * 
   */
  public DIPController(DipFactory dipFactory, DipMessageHandlerDataListener handler, EquipmentLogger equipmentLogger, 
      IEquipmentConfiguration equipmentConfiguration, IEquipmentMessageSender equipmentMessageSender) {
    this.dipFactory = dipFactory;
    this.handler = handler;
    this.equipmentLogger = equipmentLogger;
    this.equipmentConfiguration = equipmentConfiguration;
    this.equipmentMessageSender = equipmentMessageSender;
  }
  
  /**
   * Connection
   * 
   * @param sourceDataTag
   * @param changeReport 
   * 
   */
  public CHANGE_STATE connection(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
    this.equipmentLogger.debug("connection - Connecting " + sourceDataTag.getId());
    
    // Hardware Address
    DIPHardwareAddress sdtAddress = (DIPHardwareAddress) sourceDataTag.getHardwareAddress();

    // !!!! Put extra comments in case the item-name is empty !!!
    if (sdtAddress == null || sdtAddress.getItemName() == null) {
      this.equipmentLogger.error("connection - corrupted configuration. SDT does not contain correct hardware address!!");
      this.equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, 
          "No valid DIP address defined. Please check the configuration!");
      if (changeReport != null) {
        changeReport.appendError("connection - corrupted configuration. SDT does not contain correct hardware address!!");
      }
      
      return CHANGE_STATE.FAIL;
    }
    // Create a new subscription only if one with the same item name hasn't yet been created 
    // Pair [topicName ==> vector of related SourceDataTags]
    else if (!subscribedDataTags.containsKey(sdtAddress.getItemName())) {
      // it that's the first tag subscribed for that topic
      Vector<ISourceDataTag> v = new Vector<ISourceDataTag>(1);
      v.addElement(sourceDataTag);
      this.subscribedDataTags.put(sdtAddress.getItemName(), v);
      // If there're already tags subscribed for that topic
    } else {
      Vector<ISourceDataTag> v = this.subscribedDataTags.get(sdtAddress.getItemName());
      v.addElement(sourceDataTag);
      this.subscribedDataTags.put(sdtAddress.getItemName(), v);
      this.equipmentLogger.debug("connection - adding tag for item : " + sdtAddress.getItemName() + " (" + sdtAddress.getFieldName() + ")");
      if (changeReport != null) {
        changeReport.appendInfo("connection - adding tag for item : " + sdtAddress.getItemName() + " (" + sdtAddress.getFieldName() + ")");
      }
    }
    
    DipSubscription dipSubscr = null;
    try {
      dipSubscr = this.dipFactory.createDipSubscription(sdtAddress.getItemName(), this.handler);
      this.dipSubscriptions.put(sdtAddress.getItemName(), dipSubscr);

      this.equipmentLogger.debug("connection - Creating subscription for " + dipSubscr.getTopicName());
    } catch (DipException ex) {
      this.equipmentLogger.error("connection - A problem with creating subscription occured : " + ex.getMessage());
      if(changeReport != null) {
        changeReport.appendError("connection - A problem with creating subscription occured");
      }
      
      Collection<ISourceDataTag> collection = subscribedDataTags.get(sdtAddress.getItemName());
      if (collection != null) {
        Iterator<ISourceDataTag> iter = collection.iterator();
        while (iter.hasNext()) {
          this.equipmentMessageSender.sendInvalidTag(iter.next(), SourceDataQuality.INCORRECT_NATIVE_ADDRESS, 
              "DIP subscription error! Reason: " + ex.getMessage());
        }
      }
      
      return CHANGE_STATE.FAIL;
    }
    
    this.equipmentLogger.debug("connection - Leaving ...");
    if (changeReport != null) {
      changeReport.appendInfo("connection - DIP subscription succesfully created.");
    }
    
    return CHANGE_STATE.SUCCESS;
  }
  
  /**
   * Disconnection
   * 
   * @param dipSubscription
   * @param changeReport 
   */
  public CHANGE_STATE disconnection(final DipSubscription dipSubscription, final ChangeReport changeReport) {
    this.equipmentLogger.debug("disconnection - Starting ...");
    
    try {
      getEquipmentLogger().debug(new StringBuffer("disconnection - destroying subscription ").append(dipSubscription.getTopicName()));
      this.dipFactory.destroyDipSubscription(dipSubscription);
    } catch (DipException de) {
      getEquipmentLogger().error("disconnection - A problem occured while trying to destroy dip subscription : " + de.getMessage());
      if (changeReport != null) {
        changeReport.appendError("disconnection - DipException - A problem occured while trying to destroy dip subscription");
      }
      return CHANGE_STATE.FAIL;
    } catch (Exception ex) {
      getEquipmentLogger().error("disconnection - A problem occured while trying to destroy dip subscription : ", ex);
      if (changeReport != null) {
        changeReport.appendError("disconnection - A problem occured while trying to destroy dip subscription");
      }
      return CHANGE_STATE.FAIL;
    }
    
    this.equipmentLogger.debug("disconnection - Leaving ...");
    if (changeReport != null) {
      changeReport.appendInfo("disconnection - DIP unsubscription succesfully done.");
    }
    
    return CHANGE_STATE.SUCCESS;
  }
  
  /**
   * Disconnection
   * 
   * @param sourceDataTag
   * @param changeReport
   */
  public CHANGE_STATE disconnection(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
    // Hardware Address
    DIPHardwareAddress sdtAddress = (DIPHardwareAddress) sourceDataTag.getHardwareAddress();
    
    // Disconnect the sourcedataTag
    CHANGE_STATE state = disconnection(this.dipSubscriptions.get(sdtAddress.getItemName()), changeReport);
    
    // Remove it from the list of dipSubscriptions
    if (state == CHANGE_STATE.SUCCESS) {
      if (this.dipSubscriptions.remove(sdtAddress.getItemName()) == null) {
        if (changeReport != null) {
          changeReport.appendInfo("disconnection - No mapping for key " + sdtAddress.getItemName());
        }
        
        return CHANGE_STATE.FAIL;
      }
    }
    
    return state;
  }

  /**
   * @return the equipmentMessageSender
   */
  public IEquipmentMessageSender getEquipmentMessageSender() {
      return this.equipmentMessageSender;
  }
  
  /**
   * @param equipmentMessageSender The equipmentMessageSender to set
   */
  public void setEquipmentMessageSender(final IEquipmentMessageSender equipmentMessageSender) {
      this.equipmentMessageSender = equipmentMessageSender;
  }
  
  /**
   * @param the equipmentLogger The equipmentLogger to set
   */
  public void setEquipmentLogger(final EquipmentLogger equipmentLogger) {
      this.equipmentLogger = equipmentLogger;
  }
  
  /**
   * @return the equipmentLogger
   */
  public EquipmentLogger getEquipmentLogger() {
      return this.equipmentLogger;
  }
  
  /**
   * @param the equipmentConfiguration The equipmentConfiguration to set
   */
  public void setEquipmentConfiguration(final IEquipmentConfiguration equipmentConfiguration) {
      this.equipmentConfiguration = equipmentConfiguration;
  }
  
  /**
   * @return the equipmentConfiguration
   */
  public IEquipmentConfiguration getEquipmentConfiguration() {
      return this.equipmentConfiguration;
  }

  /**
   * @return the subscribedDataTags
   */
  public ConcurrentHashMap<String, Vector<ISourceDataTag>> getSubscribedDataTags() {
      return this.subscribedDataTags;
  }
  
  /**
   * @return the dipSubscriptions
   */
  public ConcurrentHashMap<String, DipSubscription> getDipSubscriptions() {
      return this.dipSubscriptions;
  }

}
