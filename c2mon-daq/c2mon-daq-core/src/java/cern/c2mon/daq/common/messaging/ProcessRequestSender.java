package cern.c2mon.daq.common.messaging;

import org.w3c.dom.Document;

import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;

/**
 * The interface that must be implemented by the class responsible for requesting
 * and receiving the XML connection, configuration and disconnection procedures
 * 
 * @author mbrightw
 * @author vilches (refactoring updates)
 *
 */
public interface ProcessRequestSender {

  /**
   * This method publishes the ProcessConfigurationRequest message on the configured topic. 
   * As a result it expects to receive the ProcessConfigurationResponse message. 
   * 
   * If the server does not reply within the specified timeout, null is returned.
   * 
   * @return the ProcessConfigurationResponse object
   */
  ProcessConfigurationResponse sendProcessConfigurationRequest();

  /**
   * This method publishes the unique Process Id Key (PIK) message on the configured topic. 
   * As a result it expects to receive the Process Configuration Response message. 
   * 
   * If the server does not reply within the specified timeout, null is returned.
   * 
   * @return the parsed XML processConnectionResponse object
   */
  ProcessConnectionResponse sendProcessConnectionRequest();

  /**
   *  Send a ProcessDisconnectionRequest to the server. No reply is expected.
   */
  void sendProcessDisconnectionRequest();

  /**
   * TODO: Backward compatibility. remove after updating server
   */

  /**
   * This method publishes the ProcessConnection message on the configured topic. 
   * As a result it expects to receive the ProcessConfiguration message. The 
   * XML content of the message is then automatically parsed, and the DOM Document
   * object is returned for further processing. If the server does not reply
   * within the specified timeout, null is returned.
   * @return the parsed XML document
   */
  Document old_sendProcessConfigurationRequest();

  /**
   * This method publishes the ProcessConnection message on the configured topic. 
   * As a result it suspects to receive the ProcessConfiguration message. The 
   * XML content of the message is then automatically parsed, and the DOM Document
   * object is returned for further processing. If the argument contains a 
   * non-empty string (file name), the method will try to save received XML 
   * in a specified file.
   * @param fileToSaveConf the name of the file to save the config to
   * @return the parsed XML document
   */
  Document old_sendProcessConfigurationRequest(String fileToSaveConf);

  /**
   *  This method is invoked by the Kernel just before the 
   *  driver (clearly) exits. The server application stops 
   *  supervising the process in question and invalidates all 
   *  associated tags.The driver is also supposed to send a 
   *  ProcessDisconnection message in reply to a 
   *  ProcessTerminationRequest message (TODO is this implemented in the server?!).
   *  No response from the server is expected.
   */
  void old_sendProcessDisconnection();
}
