package cern.c2mon.server.client.request;

import java.util.ArrayList;
import java.util.Collection;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.log4j.Logger;

import cern.c2mon.server.configuration.ConfigProgressMonitor;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Receives updates about long - running requests in the server
 * and sends them back as Progress Reports to the client.
 * 
 * Currently only used for APPLY_CONFIGURATION requests.
 * @see ClientRequestProgressReport
 * 
 * @author ekoufaki
 */
public class ClientRequestReportHandler implements ConfigProgressMonitor {

  /** Private class logger */
  private static final Logger LOG = Logger.getLogger(ClientRequestReportHandler.class);

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


  public ClientRequestReportHandler(final Session pSession, final Destination pReplyDestination
      , final long pDefaultReplyTTL) {

    this.session = pSession;
    this.replyDestination = pReplyDestination;
    this.defaultReplyTTL = pDefaultReplyTTL;
  }

  /**
   * Reports are sent to the clients as json messages.
   * @param jsonResponse the message to be sent
   */
  private void sendJsonResponse(final String jsonResponse) {

    if (replyDestination != null) {

      MessageProducer messageProducer;
      try {
        messageProducer = session.createProducer(replyDestination);

        messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        messageProducer.setTimeToLive(defaultReplyTTL);

        Message replyMessage = null;

        // Send response as Json message
        replyMessage = session.createTextMessage(jsonResponse);
        messageProducer.send(replyMessage);

        if (LOG.isDebugEnabled()) {
          LOG.debug("ClientRequestReportHandler() : Report sent.");
        }

      } catch (JMSException e) {
        LOG.warn("daqTotalParts(): Failed to send Progress report.");
      }
    } 
    else {
      LOG.error("sendJsonResponse() : JMSReplyTo destination is null - cannot send reply.");
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

    Collection<ClientRequestResult> response = new ArrayList<ClientRequestResult>();
    response.add(report);
    sendResponse(response);
  }

  @Override
  public void daqTotalParts(final int nbParts) {

    currentOperation = 2; // we know this operation is performed 2nd
    totalParts = nbParts;
    if (LOG.isDebugEnabled()) {
      LOG.debug("daqTotalParts() : totalParts=" + nbParts + " Sending Report..");
    }
    sendProgressReport(TOTAL_OPERATIONS, currentOperation, totalParts, 1, "Applying DAQ configurations");
  }

  @Override
  public void onDaqProgress(final int partNb) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("onDaqProgress() : currentPart=" + partNb + " Sending Report..");
    }
    sendProgressReport(TOTAL_OPERATIONS, currentOperation, totalParts, partNb, "Applying DAQ configurations");
  }

  @Override
  public void onServerProgress(final int partNb) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("onServerProgress() : currentPart=" + partNb + " Sending Report..");
    }
    sendProgressReport(TOTAL_OPERATIONS, currentOperation, totalParts, partNb, "Applying configuration to Server");
  }

  @Override
  public void serverTotalParts(final int nbParts) {
    
    currentOperation = 1; // we know this operation is performed 1st
    totalParts = nbParts;
    if (LOG.isDebugEnabled()) {
      LOG.debug("serverTotalParts() : totalParts=" + nbParts + " Sending Report..");
    }
    sendProgressReport(TOTAL_OPERATIONS, currentOperation, totalParts, 1, "Applying configuration to Server");
  }
}
