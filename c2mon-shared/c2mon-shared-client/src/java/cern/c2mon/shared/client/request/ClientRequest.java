package cern.c2mon.shared.client.request;

import java.util.Collection;

import cern.c2mon.shared.client.command.CommandExecuteRequest;
import cern.c2mon.shared.client.command.CommandTagHandle;

/**
 * This interface describes the request message object
 * that is sent from the C2MON client API to the server.
 * The following request types are currently supported:
 * <li> <b>TAG_REQUEST</b>: For requesting a list of
 *      <code>TransferTag</code> or <code>TransferTagValue</code>
 *      objects.
 * <li> <b>SUPERVISION_REQUEST</b>: For requesting the actual
 *      list of all <code>SupervisionEvent</code> states
 * <li> <b>ALARM_REQUEST</b>: For requesting an Alarm
 * <li> <b>CONFIGURATION_REPORT_REQUEST</b>: For requesting a Configuration Report
 *
 * @author Matthias Braeger
 */
public interface ClientRequest {

  /**
   * Enumeration for specifying the request type:
   *
   * @author Matthias Braeger
   */
  enum RequestType {
    /** 
     * Used to request a list of either <code>TransferTag</code>
     * or <code>TransferTagValue</code> objects.
     * @see ResultType
     */
    TAG_REQUEST,
    /** 
     * Used to request a list of <code>TagConfig</code>
     * objects.
     */
    TAG_CONFIGURATION_REQUEST,
    /**
     * Used at client startup to request all current supervision events
     * in order to know which tags must be marked as inaccesible.
     */
    SUPERVISION_REQUEST,
    /**
     * Used to request a list of Alarm values from the server.
     */
    ALARM_REQUEST,
    /**
     * Used to request a list of all ACTIVE Alarm values from the server.
     */
    ACTIVE_ALARMS_REQUEST,
    /**
     * Used to request a list of Command handles from the server.
     */
    COMMAND_HANDLE_REQUEST,
    /**
     * Used to request a list of Command handles from the server.
     */
    EXECUTE_COMMAND_REQUEST,
    /**
     * Used to request a Configuration Report from the server.
     */
    APPLY_CONFIGURATION_REQUEST,    
    /**
     * Used to request the DAQ XML configuration from the server.
     * The request parameter needs setting to the DAQ name.
     * Only a single XML can be put in a given request.
     */
    DAQ_XML_REQUEST,
    /**
     * Used to request a list of all the process names from the server.
     */
    PROCESS_NAMES_REQUEST
  };
  
  /** 
   * Enumeration for specifying the expected result type of the response.
   * The two values correspond to the <code>TransferTag</code> and
   * <code>TransferTagValue</code> interfaces.
   * @see cern.c2mon.shared.client.tag.TagValueUpdate
   * @see cern.c2mon.shared.client.tag.TagUpdate
   * @see cern.c2mon.shared.client.supervision.SupervisionEvent
   */
  enum ResultType {
    /** @see cern.c2mon.shared.client.TransferTag */
    TRANSFER_TAG_LIST,
    /** @see cern.c2mon.shared.client.TransferTagValue */
    TRANSFER_TAG_VALUE_LIST,
    /** @see ... */
    TRANSFER_TAG_CONFIGURATION_LIST,
    /** @see cern.c2mon.shared.client.supervision.SupervisionEvent */
    SUPERVISION_EVENT_LIST,
    /** @see ... */
    TRANSFER_ALARM_LIST,
    /** @see ... */
    TRANSFER_ACTIVE_ALARM_LIST,
    /** @see ... */
    TRANSFER_COMMAND_HANDLES_LIST,   
    /** @see ... */
    TRANSFER_COMMAND_REPORT,
    /** @see ... */
    TRANSFER_CONFIGURATION_REPORT,    
    /** A  is returned for these request.*/
    TRANSFER_DAQ_XML,
    /** A  is returned for these request.*/
    TRANSFER_PROCESS_NAMES
  };
  
  /**
   * Returns a list of tag Ids for which a client wants to
   * receive the <code>TransferTag</code> or <code>TransferTagValue</code>
   * objects.
   * 
   * This is actually used to return Alarm, Configuration and Command Ids as well.
   * @return A list of tag ids
   */
  Collection<Long> getTagIds();

  /**
   * This method returns the type of the client request
   * @return The type of request
   * @see RequestType
   */
  RequestType getRequestType();
  
  /**
   * @return The expected <code>ResultType</code> of the response message
   * @see ResultType
   */
  ResultType getResultType();

  /**
   * @return the request parameter (may be null for types that do not need this set).
   * @see RequestType
   */
  String getRequestParameter();
  
  /**
   * Only supported by EXECUTE_COMMAND_REQUESTS so far.
   * @return the Object parameter (may be null for types that do not need this set).
   * In case of the EXECUTE_COMMAND_REQUEST this is a {@link CommandExecuteRequest}.
   */
  Object getObjectParameter();
  
  /**
   * @return <code>true</code> if the response for this Request should
   * be sent as an Object false if the response should be sent in Json format.
   */
  boolean requiresObjectResponse();
  
  /**
   * Every request has a different timeout depending.
   * @return request timeout in Milliseconds. 
   */  
  int getTimeout();
}
