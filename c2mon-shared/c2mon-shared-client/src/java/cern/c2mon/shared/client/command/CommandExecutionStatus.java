package cern.c2mon.shared.client.command;

/**
 * This <code>enum</code> represents the different command execution status that
 * can be reported within a {@link CommandReport} message.
 *
 * @author Matthias Braeger
 */
public enum CommandExecutionStatus {
  
  /** This status indicates that command execution was successful. */
  STATUS_OK((short) 0, "OK"),
  /**
   * This status indicates that the command is unknown to the server or the
   * driver. 
   */
  STATUS_CMD_UNKNOWN((short) 1, "Command id unknown to the server"),

  /**
   * This status indicates that the command has timed out. Commands can time
   * out on the client, application server or driver level.
   */
  STATUS_TIMED_OUT((short) 2, "Execution timed out"),

  /**
   * This status indicates that the command could not be executed because
   * the command value sent by the user was outside the valid range defined
   * for this command tag.
   */
  STATUS_RANGE_CHECK_FAILED((short) 3, "Command value outside valid range"),

  /**
   * This status indicates that the user is not authorised to execute this
   * command.
   */
  STATUS_AUTHORISATION_FAILED((short) 4, "User not authorized to execute command"),

  /**
   * This status indicates that command execution has failed. This status should
   * only be set by the driver when command execution fails on the lowest level
   * (e.g. communication with a PLC fails)
   */
  STATUS_EXECUTION_FAILED((short) 5, "FAILED"),
  
  /**
   * This status indicates that the command could not be executed since the process
   * to which the command should have been sent is actually down.
   */
  STATUS_PROCESS_DOWN((short) 6, "Process appears to be down"),

  /**
   * This status indicates that the command could not be executed, because of an
   * internal error of the C2MON server.
   */
  STATUS_SERVER_ERROR((short) 7, "Internal server error"),
  
  /**
   * This status indicates that the command was not executed. It should only be
   * set if the CommandTag is in maintenance or test mode and was therefore not 
   * sent to the equipment on the field.
   */
  STATUS_NOT_EXECUTED((short) 8, "NOT EXECUTED");
  
  
  /**
   * The status value of the enum field.
   */
  private final short status;
  
  /**
   * The status description
   */
  private final String description;
  
  /**
   * Hidden enum constructor
   * @param pStatus The status value
   * @param pDescription The status description
   */
  private CommandExecutionStatus(final short pStatus, final String pDescription) {
    this.status = pStatus;
    this.description = pDescription;
  }
  
  /**
   * @return The status
   */
  public short getStatus() {
    return status;
  }
  
  /**
   * @return The status description
   */
  public String getDescription() {
    return description;
  }
}
