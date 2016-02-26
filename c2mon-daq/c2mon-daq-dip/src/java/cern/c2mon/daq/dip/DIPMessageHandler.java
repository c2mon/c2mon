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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.DIPHardwareAddress;
import cern.dip.Dip;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The
 * class is used for communication driver - data sources using DIP protocol
 *
 * @author vilches
 */
public class DIPMessageHandler extends EquipmentMessageHandler {

  /** The dip message handler object used for handling dip callbacks. */
  private DipMessageHandlerDataListener handler;

  /**
   * Publishes the alive tag to DIP, which is read back and sent to server, thus
   * monitoring that the DIP subscriptions are working correctly.
   */
  private DipAlivePublisher alivePublisher;

  /**
   * DIP controller
   */
  private DIPController dipController;

  /**
   * This method is responsible for opening subscriptions for all supervised
   * SourceDataTags (data point elements). Also initializes the alive mechanism.
   *
   * @throws EqIOException In cast of connection errors
   */
  @Override
  public void connectToDataSource() {
    getEquipmentLogger().trace("connectToDataSource - Entering connectToDataSource..");

    // initialize alive mechanism
    alivePublisher = new DipAlivePublisher(getEquipmentConfiguration().getName(),
        getEquipmentConfiguration().getSourceDataTag(getEquipmentConfiguration().getAliveTagId()), // null
                                                                                                   // if
                                                                                                   // not
                                                                                                   // Tag
                                                                                                   // found
        getEquipmentConfiguration().getAliveTagInterval(), getEquipmentLoggerFactory());
    alivePublisher.start();

    // Controller
    if (this.dipController == null) {
      this.dipController = new DIPController(getEquipmentLoggerFactory(), getEquipmentConfiguration(), getEquipmentMessageSender());
    }

    if (this.dipController.getDipFactory() == null) {
      try {
        // Create a unique number
        long time = System.currentTimeMillis();
        // By using the create method with an unique number as
        // parameter, a different user application handler will be used
        // each time
        this.dipController.setDipFactory(Dip.create(getEquipmentConfiguration().getId() + "_" + Long.valueOf(time).toString()));
        this.handler = new DipMessageHandlerDataListener(this.dipController, getEquipmentLogger(DipMessageHandlerDataListener.class));

        this.dipController.setHandler(this.handler);
      }
      catch (Exception ex) {
        getEquipmentLogger().error("connectToDataSource - The handler cound not initialise properly its connection", ex);
        getEquipmentMessageSender().confirmEquipmentStateIncorrect(
            "The handler cound not initialise properly its connection. " + "Check if the DIP server is running and restart the DAQ if necessary");
      }
    }

    // we assume that DIP works
    getEquipmentMessageSender().confirmEquipmentStateOK();

    // Add Data Tag Changer
    DIPDataTagChanger dataTagChanger = new DIPDataTagChanger(this.dipController, getEquipmentLogger(DIPDataTagChanger.class));
    getEquipmentConfigurationHandler().setDataTagChanger(dataTagChanger);

    // Connection
    for (ISourceDataTag sourceDataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
      this.dipController.connect(sourceDataTag, null);
    }

    getEquipmentLogger().info("connectToDataSource - performing publication test..");

    Map<String, List<ISourceDataTag>> incorrectTags = testSubscriptions(this.dipController.getSubscribedDataTags());

    if (incorrectTags.isEmpty()) {
      getEquipmentLogger().info("\ttest OK");
    }
    else {
      logWarning(incorrectTags);
    }

    getEquipmentLogger().trace("connectToDataSource - leaving connectToDataSource");
  }

  /**
   * Private method to log which tags have been registered to exactly the same
   * complex DIP field.
   *
   * @param incorrectTags
   */
  private void logWarning(Map<String, List<ISourceDataTag>> incorrectTags) {
    getEquipmentLogger().warn("\tThose tags have been registered with the same item-names and field-names : ");

    for (Entry<String, List<ISourceDataTag>> entry : incorrectTags.entrySet()) {

      getEquipmentLogger().warn("\titem-name : " + entry.getKey());

      for (ISourceDataTag sdt1 : entry.getValue()) {
        getEquipmentLogger().warn("\t\t " + sdt1.getId() + ", ");
      }
    }
  }

  /**
   * This method checks if there are any errors in the subscription table, i.e :
   * if there's more than one tag registered with the same dip item-name AND
   * field-name. All 'problematic' tags are returned in the result array
   *
   * @param tags4topic - the map with all registered tags for topics
   *
   * @return
   */
  private Map<String, List<ISourceDataTag>> testSubscriptions(final Map<String, List<ISourceDataTag>> tags4topic) {

    Map<String, List<ISourceDataTag>> incorrectTags = new HashMap<>();

    for (Map.Entry<String, List<ISourceDataTag>> entry : tags4topic.entrySet()) {
      List<ISourceDataTag> sdtList = entry.getValue();
      String v_key = entry.getKey();
      List<ISourceDataTag> doubleEntries = new ArrayList<ISourceDataTag>();

      String tmpFieldName1 = null;
      String tmpFieldName2 = null;

      for (ISourceDataTag sdt1 : sdtList) {
        tmpFieldName1 = ((DIPHardwareAddress) sdt1.getHardwareAddress()).getFieldName();
        tmpFieldName2 = null;

        for (ISourceDataTag sdt2 : sdtList) {
          if (sdt1 == sdt2) {
            continue;
          }

          tmpFieldName2 = ((DIPHardwareAddress) sdt2.getHardwareAddress()).getFieldName();

          if (tmpFieldName2.equalsIgnoreCase(tmpFieldName1)) {
            doubleEntries.add(sdt2);
          }
        } // for

        if (!doubleEntries.isEmpty()) {
          incorrectTags.put(v_key, doubleEntries);
        }

      } // for
    } // for

    return incorrectTags;
  }

  /**
   * This method closes all previously opened subscriptions.
   *
   * @throws cern.c2mon.tools.equipmentexceptions.EqIOException
   */
  @Override
  public void disconnectFromDataSource() {
    getEquipmentLogger().trace("disconnectFromDataSource - disconnectFromDataSource() called.");

    if (alivePublisher != null) {
      alivePublisher.stop();
    }

    if (this.dipController != null) {
      for (String dipTopic : this.dipController.getDipSubscriptions().keySet()) {
        // The Key is the TopicName and the Value the DipSubscription
        this.dipController.disconnect(dipTopic, null);
      }
    }
    else {
      getEquipmentLogger().trace("disconnectFromDataSource - dipController was not initialice (null)");
    }
  }

  @Override
  public void refreshAllDataTags() {
    // TODO Implement this method at the moment it might be part of the
    // connectToDataSourceMehtod
  }

  @Override
  public void refreshDataTag(long dataTagId) {
    // TODO Implement this method.
  }

}
