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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.address.DIPHardwareAddress;
import cern.dip.DipData;
import cern.dip.DipSubscription;
import cern.dip.DipSubscriptionListener;

/**
 * This class is a handler for connect/disconnect/data reception events
 *
 * @author vilches
 *
 */
public class DipMessageHandlerDataListener implements DipSubscriptionListener {

  /**
   * ThreadPoolExecutor parameter CORE_POOL_SIZE
   */
  private static final int CORE_POOL_SIZE = 2;

  /**
   * ThreadPoolExecutor parameter MAX_POOL_SIZE
   */
  private static final int MAX_POOL_SIZE = Integer.MAX_VALUE;

  /**
   * ThreadPoolExecutor parameter KEEP_ALIVE_TIME
   */
  private static final long KEEP_ALIVE_TIME = 10L * 1000L; // 60s

  /**
   * ThreadPoolExecutor parameter MAX_QUEUE_SIZE
   */
  private static final int MAX_QUEUE_SIZE = 1000;

  /**
   * DIP controller
   */
  private DIPController dipController;

  /** Inner class to extract and send the DIP values to the C2MON server */
  private ValueHandler valueHandler;

  /**
   * Executes each submitted task using one of possibly several pooled threads
   */
  private ThreadPoolExecutor executor;

  /**
   * The equipment logger of this class.
   */
  private EquipmentLogger equipmentLogger;


  /**
   * The default constructor.
   */
  public DipMessageHandlerDataListener(final DIPController dipController, final EquipmentLogger equipmentLogger) {
    this.dipController = dipController;
    this.equipmentLogger = equipmentLogger;

    init();
  }

  /**
   * Post constructor
   */
  private void init() {
    this.valueHandler = new ValueHandler(dipController);

    this.executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE));

    this.executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
      @Override
      public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
        getEquipmentLogger().warn("rejectedExecution - Error in DIP thread pool. Is it running full?");
      }
    });
  }

  /**
   * The DIPMessageHandler uses DipMessageHandlerDataListener to handle changes
   * to subscribed data
   *
   * @param subscription
   *          the subscription object
   * @param message
   *          the DIP incoming message
   */
  @Override
  public void handleMessage(final DipSubscription subscription, final DipData message) {
    this.executor.execute(new Runnable() {

      @Override
      public void run() {
        // TODO Auto-generated method stub

        List<ISourceDataTag> t4topic = null;
        try {
          getEquipmentLogger().trace("handleMessage - entering handleMessage..");

          // get DipData's timestamp
          final long timestamp = message.extractDipTime().getAsMillis();
          getEquipmentLogger().debug("\ttimestamp retrieved from the DipData : " + new Timestamp(timestamp));

          getEquipmentLogger().debug("\tchecking data quality");
          boolean propagateData = true;
          String errorQualityDescription = "";

          switch (message.extractDataQualityEnum()) {
          case UNINITIALIZED:
            getEquipmentLogger().debug("\treceived DipData with quality : DIP_QUALITY_UNINITIALIZED, quality descr.: " + message.extractQualityString());
            getEquipmentLogger().debug("\tfor topic : " + subscription.getTopicName());
            getEquipmentLogger().debug("\tNOTE : This value will not be propagated");

            errorQualityDescription = "DIP_QUALITY_UNINITIALIZED:" + message.extractQualityString();
            propagateData = false;
            break;

          case UNCERTAIN:
            getEquipmentLogger().debug("\treceived DipData with quality : DIP_QUALITY_UNCERTAIN, quality descr.: " + message.extractQualityString());
            getEquipmentLogger().debug("\tfor topic : " + subscription.getTopicName());
            getEquipmentLogger().debug("\tNOTE : This value will not be propagated !");

            errorQualityDescription = "DIP_QUALITY_UNCERTAIN:" + message.extractQualityString();
            propagateData = false;
            break;

          case BAD:
            getEquipmentLogger().debug("\treceived DipData with quality : DIP_QUALITY_BAD, quality descr.: " + message.extractQualityString());
            getEquipmentLogger().debug("\tfor topic : " + subscription.getTopicName());
            getEquipmentLogger().debug("\tNOTE : This value will not be propagated !");

            errorQualityDescription = "DIP_QUALITY_BAD:" + message.extractQualityString();
            propagateData = false;
            break;

          case GOOD:
            getEquipmentLogger().debug("\treceived DipData with quality : DIP_QUALITY_GOOD");
            break;

          default:
            getEquipmentLogger().warn("\tno data quality matches the options");
          }



          // get all SourceDataTags object related with that
          // item-name (topic)
          t4topic = dipController.getSubscribedDataTags().get(subscription.getTopicName());


          // if the quality wasn't good - exit
          if (!propagateData) {

            for (ISourceDataTag sdt : t4topic) {
              getEquipmentLogger().info("\tinvalidating SourceDataTag: "
                  + sdt.getName() + " tag id: " + sdt.getId() + " ,with description: " + errorQualityDescription);

              dipController.getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.DATA_UNAVAILABLE, errorQualityDescription,
                  new Timestamp(timestamp));
            }

            getEquipmentLogger().debug("leaving handleMessage");
            return;
          }


          if (t4topic.isEmpty()) {
            getEquipmentLogger().debug("\treceived a callback for unsubscribed tag! Skipping it..");
            getEquipmentLogger().debug("leaving handleMessage");
            return;
          } else {
            getEquipmentLogger().debug("\t" + t4topic.size() + " tags are currently registered for topic " + subscription.getTopicName());
          }

          for (ISourceDataTag sdt : t4topic) {
            getEquipmentLogger().debug("\tSourceDataTag name: " + sdt.getName() + " id: " + sdt.getId());

            // get related subscription object form the
            // collection
            getEquipmentLogger().debug("\tsubscription topicName : " + subscription.getTopicName());

            DIPHardwareAddress dipDataTagAddress = (DIPHardwareAddress) sdt.getHardwareAddress();
            getEquipmentLogger().debug("\tdip-hw-address item name : " + dipDataTagAddress.getItemName());


            valueHandler.parseMessageAndSendValue(message, sdt, dipDataTagAddress);

          } // for
        }
        catch (Throwable ex) {

          if (t4topic != null) {
            for (ISourceDataTag tag : t4topic) {
              getEquipmentLogger().error("\tReceived exception while treating incoming DIP value update for tag " + tag.getId(), ex);

              dipController.getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.UNKNOWN,
                  "Problem occured when receiving DIP value: " + ex.getMessage() + ". Review tag configuration.");
            }
          }
        }

        getEquipmentLogger().trace("\thandleMessage - leaving handleMessage");
      }
    });
  }

  /**
   * This method is called-back when a publication the driver is subscribed to
   * becomes available. In the subscription has previously been lost, the method
   * requests update of the tags related to the subscription that has been
   * re-established.
   *
   * @param subscription
   *          The subscription who's publication is available
   */
  @Override
  public void connected(DipSubscription subscription) {
    getEquipmentLogger().info("connected - Publication " + subscription.getTopicName() + " is available");
    getEquipmentLogger().debug("\t requesting update!");
    // removed as not used
    // connectionDropped4Subscription.put(subscription,Boolean.FALSE);
    subscription.requestUpdate();
  }

  /**
   * This method is called-back when a publication the driver is subscribed to
   * becomes unavailable. The method invalidates the tags related with to the
   * subscription that has been dropped.
   *
   * @param subscription
   *          The subscription who's publication is unavailable.
   * @param reason
   *          String providing more information about why the publication is
   *          unavailable.
   */
  @Override
  public void disconnected(DipSubscription subscription, String reason) {
    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("disconnected - entering disconnected()..");
      getEquipmentLogger().debug("disconnected - invalidating all tags registered for subscription: " + subscription.getTopicName());
    }

    getEquipmentLogger().warn("disconnected - Publication " + subscription.getTopicName() + " is unavailable. Reason : " + reason);

    // get related SourceDataTag objects
    List<ISourceDataTag> tags4topic = dipController.getSubscribedDataTags().get(subscription.getTopicName());


    for (ISourceDataTag sdt : tags4topic) {
      getEquipmentLogger().info("\tinvalidating SourceDataTag: " + sdt.getName() + " tag id: " + sdt.getId());
      this.dipController.getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.DATA_UNAVAILABLE, "subscription lost. Reason: " + reason);

    } // while

    // removed as not used
    // connectionDropped4Subscription.put(subscription, Boolean.TRUE);

    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("disconnected - exiting disconnected()..");
    }
  }

  /**
   * This method is called-back when some problems with particular subscription
   * appears. To recover from the error the method will try a re-subscription.
   *
   * @param ds
   *          - dip subscription
   * @param de
   *          - an exception with problem explanation
   */
  @Override
  public void handleException(DipSubscription ds, Exception de) {
    getEquipmentLogger()
        .error("handleException - An error appeared for publication source: " + ds.getTopicName() + ", error msg.: " + de.getMessage() + ", error trace: ", de);
    Collection<ISourceDataTag> collection = dipController.getSubscribedDataTags().get(ds.getTopicName());
    if (collection != null) {
      for (ISourceDataTag sdt : collection) {
        this.dipController.getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.DATA_UNAVAILABLE,
            "handleException() received from DIP: " + de.getMessage());
      }
    }

    dipController.renewSubscription(ds.getTopicName());
  }

  /**
   * @return the equipmentLogger
   */
  public EquipmentLogger getEquipmentLogger() {
    return this.equipmentLogger;
  }
}
