/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
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

package cern.c2mon.daq.dip;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import cern.dip.Dip;
import cern.dip.DipFactory;
import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.address.DIPHardwareAddress;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The
 * class is used for communication driver - data sources using DIP protocol
 * 
 * @author vilches
 */
public class DIPMessageHandler extends EquipmentMessageHandler {

  /** The DIP object */
  private DipFactory dipFactory;

  /** The dip message handler object used for handling dip callbacks. */
  private DipMessageHandlerDataListener handler;

  /**
   * Publishes the alive tag to DIP, which is read back and sent to server,
   * thus monitoring that the DIP subscriptions are working correctly.
   */
  private DipAlivePublisher alivePublisher;

  /**
   * DIP controller
   */
  private DIPController dipController;

  /**
   * This method is responsible for opening subscriptions for all supervised
   * SourceDataTags (data point elements). Also initializes the alive
   * mechanism.
   * 
   * @throws EqIOException
   *             In cast of connection errors
   * @roseuid 409A0D150295
   */
  public void connectToDataSource() {
    getEquipmentLogger().info("connectToDataSource - Entering connectToDataSource..");

    // initialize alive mechanism
    alivePublisher = new DipAlivePublisher(getEquipmentConfiguration().getName(), 
        getEquipmentConfiguration().getSourceDataTag(getEquipmentConfiguration().getAliveTagId()), // null if not Tag found
        getEquipmentConfiguration().getAliveTagInterval(), getEquipmentLoggerFactory());
    alivePublisher.start();
    
    // Controller
    this.dipController = new DIPController(this.dipFactory, this.handler, getEquipmentLogger(), 
        getEquipmentConfiguration(), getEquipmentMessageSender());

    if (this.dipFactory == null) {
      try {
        // Create a unique number
        long time = System.currentTimeMillis();
        // By using the create method with an unique number as
        // parameter, a different user application handler will be used
        // each time
        this.dipFactory = Dip.create(getEquipmentConfiguration().getId() + "_" + Long.valueOf(time).toString());
        this.handler = new DipMessageHandlerDataListener(this.dipController);
      } catch (Exception ex) {
        getEquipmentLogger().error("connectToDataSource - The handler cound not initialise properly its connection", ex);
        getEquipmentMessageSender().confirmEquipmentStateIncorrect(
            "The handler cound not initialise properly its connection. " + "Check if the DIP server is running and restart the DAQ if necessary");
      }
    }

    // we assume that DIP works
    getEquipmentMessageSender().confirmEquipmentStateOK();

    // Add Data Tag Changer
    DIPDataTagChanger dataTagChanger = new DIPDataTagChanger(this.dipController);
    getEquipmentConfigurationHandler().setDataTagChanger(dataTagChanger);

    // Connection
    for (ISourceDataTag sourceDataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
      this.dipController.connection(sourceDataTag, null);
    }

    getEquipmentLogger().info("connectToDataSource - performing publication test..");
    Hashtable<String, Vector<ISourceDataTag>> incorrectTags = testSubscriptions(this.dipController.getSubscribedDataTags());
    if (incorrectTags != null) {
      getEquipmentLogger().error("\tThose tags have been registered with the same item-names and field-names : ");
      Enumeration<String> e1 = incorrectTags.keys();
      Enumeration<Vector<ISourceDataTag>> e2 = incorrectTags.elements();
      while (e2.hasMoreElements()) {
        Vector<ISourceDataTag> v = e2.nextElement();
        String itemName = (String) e1.nextElement();
        getEquipmentLogger().error("\titem-name : " + itemName);
        for (int i = 0; i < v.size(); i++) {
          ISourceDataTag sdt1 = (ISourceDataTag) v.get(i);
          getEquipmentLogger().error("\t\t " + sdt1.getId() + ", ");
        }
      }
    } else {
      getEquipmentLogger().info("\ttest OK");
    }

    getEquipmentLogger().info("connectToDataSource - leaving connectToDataSource");
  }

  /**
   * This method checks if there are any errors in the subscription table, i.e
   * : if there's more than one tag registered with the same dip item-name AND
   * field-name. All 'problematic' tags are returned in the result array
   * 
   * @param tags4topic
   *            - the hashtable with all registered tags for topics
   *            
   * @return 
   */
  private Hashtable<String, Vector<ISourceDataTag>> testSubscriptions(final Map<String, Vector<ISourceDataTag>> tags4topic) {
    Hashtable<String, Vector<ISourceDataTag>> tmptable = new Hashtable<String, Vector<ISourceDataTag>>();
    Hashtable<String, Vector<ISourceDataTag>> incorrectTags = new Hashtable<String, Vector<ISourceDataTag>>();

    for (Map.Entry<String, Vector<ISourceDataTag>> entry : tags4topic.entrySet()) {
      Vector<ISourceDataTag> v = entry.getValue();
      String v_key = entry.getKey();
      Vector<ISourceDataTag> v2 = new Vector<ISourceDataTag>();
      for (int i = 0; i < v.size(); i++) {
        ISourceDataTag sdt_i = (ISourceDataTag) v.get(i);
        String viFieldName = ((DIPHardwareAddress) sdt_i.getHardwareAddress()).getFieldName();
        for (int j = 0; j < v.size(); j++) {
          if (j == i)
            continue;
          ISourceDataTag sdt_j = (ISourceDataTag) v.get(j);
          String vjFieldName = ((DIPHardwareAddress) sdt_j.getHardwareAddress()).getFieldName();
          if (vjFieldName.equalsIgnoreCase(viFieldName)) {
            v2.add(sdt_j);
          }
        }

        tmptable.put(v_key, v2);
      }// for

      Enumeration<Vector<ISourceDataTag>> e2_elems = tmptable.elements();
          Enumeration<String> e2_keys = tmptable.keys();
          while (e2_elems.hasMoreElements()) {
            Vector<ISourceDataTag> v3 = e2_elems.nextElement();
            String v3key = (String) e2_keys.nextElement();
            if (v3.size() > 0) {
              incorrectTags.put(v3key, v3);
            }
          }
    }// for

    if (incorrectTags.size() > 0) {
      return incorrectTags;
    } else {
      return null;
    }

  }

  /**
   * This method closes all previously opened subscriptions.
   * 
   * @throws cern.c2mon.tools.equipmentexceptions.EqIOException
   * @roseuid 409A0D1502EF
   */
  @Override
  public void disconnectFromDataSource()  {
    getEquipmentLogger().debug("disconnectFromDataSource - disconnectFromDataSource() called.");

    if (alivePublisher != null) {
      alivePublisher.stop();
    }

    if (this.dipController != null) {
    	for (String key :  this.dipController.getDipSubscriptions().keySet()) {
    		// The Key is the TopicName and the Value the DipSubscription
    		this.dipController.disconnection(dipController.getDipSubscriptions().get(key), null);
    	}
    }
    else {
    	getEquipmentLogger().debug("disconnectFromDataSource - dipController was not initialice (null)");
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
