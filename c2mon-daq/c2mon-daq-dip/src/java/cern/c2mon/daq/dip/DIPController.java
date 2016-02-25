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
package cern.c2mon.daq.dip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.address.DIPHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.dip.DipException;
import cern.dip.DipFactory;
import cern.dip.DipSubscription;

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

  /**
   * A HashMap with topics that the handler subscribes to as keys and
   * a its handled dip subscriptions as value
   */
  private final Map<String, DipSubscription> dipSubscriptions = new ConcurrentHashMap<>();


  /**
   * A HashMap for handling quick lookups for a data tags related with
   * particular topics.
   */
  private final Map<String, List<ISourceDataTag>> subscribedDataTags = new ConcurrentHashMap<>();

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
  public DIPController(EquipmentLoggerFactory equipmentLoggerFactory, IEquipmentConfiguration equipmentConfiguration,
          IEquipmentMessageSender equipmentMessageSender) {
    this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());
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
  public CHANGE_STATE connect(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
    getEquipmentLogger().trace("connection - Connecting " + sourceDataTag.getId());

    // Hardware Address
    DIPHardwareAddress sdtAddress = (DIPHardwareAddress) sourceDataTag.getHardwareAddress();

    // !!!! Put extra comments in case the item-name is empty !!!
    if (sdtAddress == null || sdtAddress.getItemName() == null) {
      getEquipmentLogger().error("connection - corrupted configuration. Tag #" + sourceDataTag.getId() + " has a wrongly defined hardware address!!");

      this.equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
          "No valid DIP address defined. Please check the configuration!");

      if (changeReport != null) {
        changeReport.appendError("No valid DIP address defined. Please check the configuration!");
      }

      return CHANGE_STATE.FAIL;
    }

    // Create a new subscription only if one with the same item name hasn't yet been created
    // Pair [topicName ==> list of related SourceDataTags]
    boolean createSubscription = false;
    if (!subscribedDataTags.containsKey(sdtAddress.getItemName())) {
      this.subscribedDataTags.put(sdtAddress.getItemName(), new ArrayList<ISourceDataTag>(1));
      createSubscription = true;
    }

    this.subscribedDataTags.get(sdtAddress.getItemName()).add(sourceDataTag);

    String logMsg = "Registered tag #" + sourceDataTag.getId() + " for DIP item : " + sdtAddress.getItemName() + " (" + sdtAddress.getFieldName() + ")";
    getEquipmentLogger().debug("connection - " + logMsg);

    if (changeReport != null) {
      changeReport.appendInfo(logMsg);
    }

    if (createSubscription) {
      return createDipSubscription(sdtAddress.getItemName(), changeReport);
    }
    else {
      return CHANGE_STATE.SUCCESS;
    }
  }

  /**
   * Creates the and stores the DIP subscription. In case of an address problem all related tags are
   * marked as invalid.
   * @param topicName The DIP address
   * @param changeReport The change report
   * @return {@link CHANGE_STATE#SUCCESS} in case all went fine, otherwise {@link CHANGE_STATE#FAIL}
   */
  private CHANGE_STATE createDipSubscription(final String topicName, final ChangeReport changeReport) {

    if (!dipSubscriptions.containsKey(topicName)) {
      try {
        DipSubscription dipSubscr = this.dipFactory.createDipSubscription(topicName, this.handler);
        this.dipSubscriptions.put(topicName, dipSubscr);
  
        getEquipmentLogger().debug("connection - Creating subscription for " + dipSubscr.getTopicName());
      } catch (DipException ex) {
        getEquipmentLogger().error("connection - A problem with creating subscription occured : " + ex.getMessage());
  
        if(changeReport != null) {
          changeReport.appendError("connection - A problem with creating subscription occured. Reason: " + ex.getMessage());
        }
  
        Collection<ISourceDataTag> collection = subscribedDataTags.get(topicName);
        if (collection != null) {
          for (ISourceDataTag sdt : collection) {
            this.equipmentMessageSender.sendInvalidTag(sdt, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                "DIP subscription error! Reason: " + ex.getMessage());
          }
        }
  
        return CHANGE_STATE.FAIL;
      }
    }

    getEquipmentLogger().debug("connection - Leaving ...");
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
  public synchronized CHANGE_STATE disconnect(final String topicName, final ChangeReport changeReport) {
    getEquipmentLogger().trace("disconnection - Starting ...");

    DipSubscription dipSubscription = this.dipSubscriptions.remove(topicName);
    
    try {
      if (dipSubscription != null) {
        getEquipmentLogger().debug(new StringBuffer("disconnection - destroying subscription ").append(dipSubscription.getTopicName()));
        this.dipFactory.destroyDipSubscription(dipSubscription);
      }
    }
    catch (Exception ex) {
      getEquipmentLogger().error("disconnection - A problem occured while trying to destroy dip subscription : ", ex);
      if (changeReport != null) {
        changeReport.appendError("disconnection - A problem occured while trying to destroy dip subscription. Reason: " + ex.getMessage());
      }
      return CHANGE_STATE.FAIL;
    }

    getEquipmentLogger().trace("disconnection - Leaving ...");
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
    return disconnect(sdtAddress.getItemName(), changeReport);
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
  public Map<String, List<ISourceDataTag>> getSubscribedDataTags() {
      return this.subscribedDataTags;
  }

  /**
   * @return the dipSubscriptions
   */
  public Map<String, DipSubscription> getDipSubscriptions() {
      return this.dipSubscriptions;
  }

  /**
   * Renews the subscription to a DIP client by disconnecting and
   * then reconnecting.
   * @param topicName The DIP topic name.
   */
  public void renewSubscription(final String topicName) {
    disconnect(topicName, null);
    createDipSubscription(topicName, null);
  }

  /**
   *
   * @param dipFactory
   */
  public void setDipFactory(final DipFactory dipFactory) {
    this.dipFactory = dipFactory;
  }

  /**
   *
   * @return dipFactory
   */
  public DipFactory getDipFactory() {
    return this.dipFactory;
  }

  /**
   *
   * @param handler
   */
  public void setHandler(final DipMessageHandlerDataListener handler) {
    this.handler = handler;
  }


}
