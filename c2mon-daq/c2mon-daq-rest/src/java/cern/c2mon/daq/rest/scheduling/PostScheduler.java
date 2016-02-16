/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.rest.scheduling;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.rest.address.RestPostAddress;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.common.type.TypeConverter;
import org.springframework.http.HttpStatus;

import java.util.TimerTask;

/**
 * @author Franz Ritter
 */
public class PostScheduler extends RestScheduler {


  public PostScheduler(IEquipmentMessageSender sender, IEquipmentConfiguration configuration, EquipmentLogger logger) {
    super(sender, configuration, logger);

  }

  /**
   * If the Controller received successfully a message from a client this method handles
   * all actions which needs to be done in that case.
   * After receiving a message the ReceiverTask of the id must be restarted.
   * Furthermore value must be cast to the given DataType of the corresponding DataTag.
   * <p/>
   * If this things are successful the scheduler sends the value to the server and gives
   * a HttpStatus.OK return.
   *
   * @param id    The id of the corresponding DataTag which this message belongs to.
   * @param value The Value for the DataTag
   * @return Status based on the success of the processing of the value.
   */
  public HttpStatus sendValueToServer(Long id, Object value) {

    // send message if the id is known to the server(and daq)
    if (this.contains(id)) {

      ISourceDataTag tag = equipmentConfiguration.getSourceDataTag(id);
      RestPostAddress address = getAddress(id);
      ReceiverTask newTask = new ReceiverTask(id);

      if (TypeConverter.isConvertible(value, tag.getDataType())) {

        Object message = TypeConverter.cast(value, tag.getDataType());
        equipmentMessageSender.sendTagFiltered(tag, message, System.currentTimeMillis());

      } else {

        equipmentMessageSender.sendInvalidTag(tag, SourceDataQuality.UNSUPPORTED_TYPE, "Message received for DataTag:" + id + " which DataType is not supported.");
        equipmentLogger.warn("Message received for DataTag:" + id + " which DataType is not supported.");
        return HttpStatus.BAD_REQUEST;

      }

      // reset the timer for the tag
      if (idToTask.get(id).cancel()) {
        idToTask.put(id, newTask);
        timer.purge();
        timer.schedule(newTask, address.getFrequency());

      } else if (address.getFrequency() != null) {
        idToTask.put(id, newTask);
        timer.schedule(newTask, address.getFrequency());
      }

      return HttpStatus.OK;
    } else {

      equipmentLogger.warn("DAQ received a message with the id:" + id + ". This id is not supported from the DAQ.");
      return HttpStatus.BAD_REQUEST;
    }
  }

  @Override
  public void addTask(Long id) {

    RestPostAddress address = getAddress(id);
    ReceiverTask task = new ReceiverTask(id);

    idToTask.put(id, task);

    // only add the task to the timer if the frequency ist set.
    if (address.getFrequency() != null) {
      timer.schedule(task, address.getFrequency());
    }

  }

  @Override
  /**
   * Simply send the last received tag value again
   */
  public void refreshDataTag(Long id) {

    ISourceDataTag tag = equipmentConfiguration.getSourceDataTag(id);
    equipmentMessageSender.sendTagFiltered(tag, tag.getCurrentValue(), System.currentTimeMillis());


  }

  private RestPostAddress getAddress(Long id) {
    return (RestPostAddress) this.equipmentConfiguration.getSourceDataTag(id).getHardwareAddress();
  }

  //===========================================================================
  // Inner helper class
  //===========================================================================

  /**
   * A instance of the SendRequestTask holds all information for sending a invalid message to the equipment.
   */
  class ReceiverTask extends TimerTask {

    private Long id;

    ReceiverTask(Long id) {
      this.id = id;
    }

    /**
     * This method is called after the interval of this task expires.
     * If the interval expire the client did not send a post message with the given id to the REST daq.
     * Because of that the daq thinks that the data is invalid and an invalid message is end to the server.
     */
    @Override
    public void run() {

      ISourceDataTag sdt = equipmentConfiguration.getSourceDataTag(id);

      // sending the reply to the server
      equipmentMessageSender.sendInvalidTag(sdt, SourceDataQuality.DATA_UNAVAILABLE, "No value received in the given time interval of the DataTag-" + id);


    }

  }


}
