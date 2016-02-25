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
  public CHANGE_STATE connection(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
    getEquipmentLogger().debug("connection - Connecting " + sourceDataTag.getId());

    // Hardware Address
    DIPHardwareAddress sdtAddress = (DIPHardwareAddress) sourceDataTag.getHardwareAddress();

    // !!!! Put extra comments in case the item-name is empty !!!
    if (sdtAddress == null || sdtAddress.getItemName() == null) {
      getEquipmentLogger().error("connection - corrupted configuration. SDT does not contain correct hardware address!!");
      this.equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
          "No valid DIP address defined. Please check the configuration!");
      if (changeReport != null) {
        changeReport.appendError("connection - corrupted configuration. SDT does not contain correct hardware address!!");
      }

      return CHANGE_STATE.FAIL;
    }
    // Create a new subscription only if one with the same item name hasn't yet been created
    // Pair [topicName ==> list of related SourceDataTags]
    else if (!subscribedDataTags.containsKey(sdtAddress.getItemName())) {
      // it that's the first tag subscribed for that topic
      List<ISourceDataTag> list = new ArrayList<>(1);
      list.add(sourceDataTag);
      this.subscribedDataTags.put(sdtAddress.getItemName(), list);
      // If there're already tags subscribed for that topic
    } else {
      List<ISourceDataTag> list = this.subscribedDataTags.get(sdtAddress.getItemName());
      list.add(sourceDataTag);
      this.subscribedDataTags.put(sdtAddress.getItemName(), list);
      getEquipmentLogger().debug("connection - adding tag for item : " + sdtAddress.getItemName() + " (" + sdtAddress.getFieldName() + ")");
      if (changeReport != null) {
        changeReport.appendInfo("connection - adding tag for item : " + sdtAddress.getItemName() + " (" + sdtAddress.getFieldName() + ")");
      }
    }

    return createDipSubscription(sdtAddress.getItemName(), changeReport);
  }

  /**
   * Creates the and stores the DIP subscription. In case of an address problem all related tags are
   * marked as invalid.
   * @param topicName The DIP address
   * @param changeReport The change report
   * @return {@link CHANGE_STATE#SUCCESS} in case all went fine, otherwise {@link CHANGE_STATE#FAIL}
   */
  private CHANGE_STATE createDipSubscription(final String topicName, final ChangeReport changeReport) {
    DipSubscription dipSubscr = null;
    try {
      dipSubscr = this.dipFactory.createDipSubscription(topicName, this.handler);
      this.dipSubscriptions.put(topicName, dipSubscr);

      getEquipmentLogger().debug("connection - Creating subscription for " + dipSubscr.getTopicName());
    } catch (DipException ex) {
      getEquipmentLogger().error("connection - A problem with creating subscription occured : " + ex.getMessage());
      if(changeReport != null) {
        changeReport.appendError("connection - A problem with creating subscription occured");
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
  public CHANGE_STATE disconnection(final DipSubscription dipSubscription, final ChangeReport changeReport) {
    getEquipmentLogger().debug("disconnection - Starting ...");

    try {
      getEquipmentLogger().debug(new StringBuffer("disconnection - destroying subscription ").append(dipSubscription.getTopicName()));
      this.dipFactory.destroyDipSubscription(dipSubscription);
    }
    catch (DipException de) {
      getEquipmentLogger().error("disconnection - A problem occured while trying to destroy dip subscription : " + de.getMessage());
      if (changeReport != null) {
        changeReport.appendError("disconnection - DipException - A problem occured while trying to destroy dip subscription");
      }
      return CHANGE_STATE.FAIL;
    }
    catch (Exception ex) {
      getEquipmentLogger().error("disconnection - A problem occured while trying to destroy dip subscription : ", ex);
      if (changeReport != null) {
        changeReport.appendError("disconnection - A problem occured while trying to destroy dip subscription");
      }
      return CHANGE_STATE.FAIL;
    }

    getEquipmentLogger().debug("disconnection - Leaving ...");
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
    disconnection(this.dipSubscriptions.get(topicName), null);
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
