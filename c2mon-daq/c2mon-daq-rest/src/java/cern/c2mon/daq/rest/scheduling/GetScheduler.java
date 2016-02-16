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
import cern.c2mon.daq.rest.address.RestGetAddress;
import cern.c2mon.daq.rest.webaccess.RESTConnector;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.springframework.web.client.RestClientException;

import java.util.TimerTask;


/**
 * This class managed the Timer threads to for making requests to the Webservice.
 * It creates the timer and adds Task to the Timer which are making the request.
 * Besides creating the task this class also manges the deleting of obsolete tasks.
 * <p/>
 *
 * @author Franz Ritter
 */
public class GetScheduler extends RestScheduler {

  public GetScheduler(IEquipmentMessageSender sender, IEquipmentConfiguration configuration, EquipmentLogger logger) {
    super(sender, configuration, logger);
  }

  @Override
  public void addTask(Long id) {

    // create task
    RestGetAddress hardwareAddress = (RestGetAddress) this.equipmentConfiguration.getSourceDataTag(id).getHardwareAddress();
    SendRequestTask task = new SendRequestTask(hardwareAddress.getUrl(), id, hardwareAddress.getJsonPathExpression());

    //save the Task in map and add it to the timer
    idToTask.put(id, task);

    timer.schedule(task, hardwareAddress.getFrequency(), hardwareAddress.getFrequency());

  }


  @Override
  public void refreshDataTag(Long id) {

    // get information:
    ISourceDataTag dataTag = this.equipmentConfiguration.getSourceDataTag(id);
    RestGetAddress hardwareAddress = (RestGetAddress) dataTag.getHardwareAddress();

    // http request:
    String restMessage = RESTConnector.sendAndReceiveRequest(hardwareAddress.getUrl());

    // sending the reply to the server:
    equipmentMessageSender.sendTagFiltered(dataTag, restMessage, System.currentTimeMillis());

  }

  // TODO check for more types;
  private static String getJavaLangType(String type) {
    if (type.equals("Long") ||
        type.equals("String") ||
        type.equals("Integer") ||
        type.equals("Byte") ||
        type.equals("Char") ||
        type.equals("Boolean") ||
        type.equals("Float") ||
        type.equals("Double")) {

      return "java.lang.";
    } else {
      return "";
    }

  }


  //===========================================================================
  // Inner class
  //===========================================================================

  /**
   * A instance of the SendRequestTask holds all information for sending a GET request to a webservice.
   */
  class SendRequestTask extends TimerTask {

    private String url;

    private Long id;

    private String jsonPathExpression;

    SendRequestTask(String url, Long id, String jsonPathExpression) {
      this.url = url;
      this.id = id;
      this.jsonPathExpression = jsonPathExpression;
    }

    /**
     * Run method for the given Thread to make a get Request based on the given url to a web service.
     * Since this thread is managed my a Timer the run method will be triggered in a given interval.
     * <p/>
     * After receiving the message from the web service this method also sends the answer from service to the server.
     */
    @Override
    public void run() {

      SourceDataTag sdt = (SourceDataTag) equipmentConfiguration.getSourceDataTags().get(id);
      try {
        Object serverMessage;

        // request to the web service
        String restMessage = RESTConnector.sendAndReceiveRequest(url);
        Class dataType = Class.forName(getJavaLangType(sdt.getDataType()) + sdt.getDataType());

        // convert Message if jsonPathExpression is given
        if (jsonPathExpression != null) {
          serverMessage = JsonPath.parse(restMessage).read(jsonPathExpression, dataType);
        } else {
          serverMessage = restMessage;
        }

        // sending the reply to the server
        equipmentMessageSender.sendTagFiltered(sdt, serverMessage, System.currentTimeMillis());

      } catch (RestClientException | ClassNotFoundException e) {
        equipmentLogger.warn("Problem occurred at the REST get-operation: " + e.getMessage());
        equipmentMessageSender.sendInvalidTag(sdt, SourceDataQuality.DATA_UNAVAILABLE, "Problem occurred at the REST get-operation: " + e.getMessage());

      }

    }

  }

}
