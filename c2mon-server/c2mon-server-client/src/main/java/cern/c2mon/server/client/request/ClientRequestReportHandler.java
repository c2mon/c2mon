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
package cern.c2mon.server.client.request;

import cern.c2mon.server.configuration.ConfigProgressMonitor;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.util.json.GsonFactory;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import javax.jms.*;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Receives updates about long - running requests in the server
 * and sends them back as Progress Reports to the client.
 *
 * Currently only used for APPLY_CONFIGURATION requests.
 * @see ClientRequestProgressReport
 *
 * @author ekoufaki
 */
@Slf4j
public class ClientRequestReportHandler implements ConfigProgressMonitor {

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  /**
   * Every progress report consists of a number of operations.
   *
   * Right now only ConfigurationReports are supported and
   * the number of operations for that case == 2.
   * This can change in the future.
   */
  private static final int TOTAL_OPERATIONS = 2;

  /**
   * The current operation.
   */
  private int currentOperation;

  /**
   * How many parts to expect for this progress report.
   */
  private int totalParts;

  /**
   * Default TTL of replies to client requests
   */
  private final long defaultReplyTTL;

  /**
   * Used to send the reports.
   */
  private final Session session;

  /**
   * Used to send the reports.
   */
  private final Destination replyDestination;

  private AtomicInteger progressCounter;


  public ClientRequestReportHandler(final Session pSession, final Destination pReplyDestination ,
                                    final long pDefaultReplyTTL) {

    this.session = pSession;
    this.replyDestination = pReplyDestination;
    this.defaultReplyTTL = pDefaultReplyTTL;
    this.progressCounter = new AtomicInteger(1);
  }

  /**
   * Reports are sent to the clients as json messages.
   * @param jsonResponse the message to be sent
   */
  private void sendJsonResponse(final String jsonResponse) {

    if (replyDestination == null) {
      log.error("sendJsonResponse() : JMSReplyTo destination is null - cannot send reply.");
      return;
    }

    MessageProducer messageProducer = null;
    try {
      messageProducer = session.createProducer(replyDestination);
      messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      messageProducer.setTimeToLive(defaultReplyTTL);

      Message replyMessage = null;

      // Send response as Json message
      replyMessage = session.createTextMessage(jsonResponse);
      messageProducer.send(replyMessage);

      log.debug("ClientRequestReportHandler() : Report sent.");
    } catch (Exception e) {
      log.warn("daqTotalParts(): Failed to send Progress report :" + e.getMessage(), e);
    } finally {
      if (messageProducer != null) {
        try {
          messageProducer.close();
        } catch (JMSException ignore) { // IGNORE
        }
      }
    }
  }

  /**
   * Helper method. Encodes the progress report in JSON format.
   * @param response the report to be sent to the client.
   */
  private void sendResponse(final Collection< ? extends ClientRequestResult> response) {

    sendJsonResponse(GSON.toJson(response));
  }

  /**
   * Sends a progress report.
   * @param totalOperations How many operations to expect for this progress report.
   * @param currentOperation The current operation
   * @param totalParts How many parts to expect for this progress report.
   * @param currentPart The current progress
   * @param description a description of what is happening
   */
  private void sendProgressReport(
      final int totalOperations,
      final int currentOperation,
      final int totalParts,
      final int currentPart,
      final String description) {

    ClientRequestResult report = new ConfigurationReport(totalOperations, currentOperation, totalParts, currentPart, description);

    sendResponse(Collections.singletonList(report));
  }

  @Override
  public void daqTotalParts(final int nbParts) {

    currentOperation = 2; // we know this operation is performed 2nd
    totalParts = nbParts;

    log.debug("daqTotalParts() : totalParts=" + nbParts + " Sending Report..");

    sendProgressReport(TOTAL_OPERATIONS, currentOperation, totalParts, 1, "Applying DAQ configurations");
  }

  @Override
  public void incrementDaqProgress() {
    log.debug("incrementDaqProgress() : currentPart=" + progressCounter + " Sending Report..");
    sendProgressReport(TOTAL_OPERATIONS, currentOperation, totalParts, progressCounter.getAndIncrement(),
        "Applying DAQ configurations");
  }

  @Override
  public void resetCounter() {
    progressCounter = new AtomicInteger(1);
  }

  @Override
  public void incrementServerProgress(String description) {
    log.debug("incrementServerProgress() : currentPart=" + progressCounter + " Sending Report...");
    sendProgressReport(TOTAL_OPERATIONS, currentOperation, totalParts, progressCounter.getAndIncrement(), description);
  }

  @Override
  public void serverTotalParts(final int nbParts) {

    currentOperation = 1; // we know this operation is performed 1st
    totalParts = nbParts;
    log.debug("serverTotalParts() : totalParts=" + nbParts + " Sending Report..");
    sendProgressReport(TOTAL_OPERATIONS, currentOperation, totalParts, 1, "Applying configuration to Server");
  }
}
