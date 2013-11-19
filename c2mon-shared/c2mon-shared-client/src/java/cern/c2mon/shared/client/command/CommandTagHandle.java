package cern.c2mon.shared.client.command;


import java.io.Serializable;

import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.common.command.AuthorizationDetails;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;


/**
 * @author Jan Stowisek
 * @version $Revision: 1.4 $ ($Date: 2005/03/07 10:40:29 $ - $State: Exp $)
 */

public interface CommandTagHandle<T> extends Serializable, ClientRequestResult {
  
  static final String CMD_UNKNOWN = "UNKNOWN";
  
  /**
   * Get the unique numeric identifier of the CommandTag represented by the 
   * present CommandTagHandle object.
   */
  Long getId();

  /**
   * Get the name of the CommandTag represented by the present CommandTagHandle
   * object.
   */
  String getName();

  /**
   * Get the (optional) free-text description of the CommandTag represented by 
   * the present CommandTagHandle object.
   */
  String getDescription();

  /**
   * Get the name of the data type of the present CommandTagHandle object.
   * Only values of this data type can be set using setValue().
   */
  String getDataType();
  
  /** Returns the Source Timeout */
  int getSourceTimeout();

  /** Returns the SourceRetries */
  int getSourceRetries();
  
  /** Returns the ExecTimeout */
  int getExecTimeout();
  
  /** Returns the ProcessId */
  Long getProcessId();
  
  /** Returns the HardwareAddress */
  HardwareAddress getHardwareAddress();

  /**
   * Get the client timeout in milliseconds.
   * When a client sends a CommandTagHandle to the server for execution and 
   * has not received a CommandTagReport after 'clientTimeout' milliseconds,
   * it should consider the command execution as failed.
   */
  int getClientTimeout();
  
  /**
   * Get the authorized minimum for the command value. 
   * If the client tries to set a value less than this minimum, the 
   * setValue() method will throw a CommandTagValueException. If the minValue 
   * is null, it is not taken into account. The minValue will always be null
   * for non-numeric commands.
   */
  Comparable<T> getMinValue();

  /**
   * Get the authorized maximum for the command value. 
   * If the client tries to set a value greater than this maximum, the 
   * setValue() method will throw a CommandTagValueException. If the maxValue 
   * is null, it is not taken into account. The maxValue will always be null
   * for non-numeric commands.
   */
  Comparable<T> getMaxValue();

  /**
   * Check whether the present CommandTagHandle object represents a CommandTag
   * that exists on the server. If not, the client will not be able to 
   * execute the command. Preferably, clients should check isExistingCommand()
   * BEFORE they call the setValue() method. If the command doesn't exist,
   * setValue() will throw a CommandTagValueException.
   */
  boolean isExistingCommand();

  /**
   * Set the command value
   * This method must be called before CommandTagHandle objects are sent to the
   * server for command execution. The method will throw a CommandTagValueException
   * if one of the following conditions is met:
   * <UL>
   * <LI>the set value is null
   * <LI>the user is not authorized to execute this command
   * <LI>the present CommandTagHandle object does not represent a CommandTag that
   * exists on the server
   * <LI>the set value is not between the authorized minimum and maximum values
   */
  void setValue(T value) throws CommandTagValueException;

  /**
   * Get the command's present value as set by the user.
   * This field will always be null before the user executes the setValue()
   * method.
   */
  T getValue();
  
  
  /**
   * Returns the authorizations details for this command. Please notice
   * that the authorizations details have to be casted into the specific
   * implementation. In case of CERN it will be casted into an
   * {@link RbacAuthorizationDetails} object.
   * @return The authorizations details for this command.
   */
  AuthorizationDetails getAuthorizationDetails();
}
