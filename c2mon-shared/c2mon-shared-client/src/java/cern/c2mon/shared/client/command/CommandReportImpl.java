package cern.c2mon.shared.client.command;


import java.io.Serializable;
import java.sql.Timestamp;

import cern.c2mon.shared.client.request.ClientRequestReport;
import cern.c2mon.shared.common.datatag.DataTagConstants;


/**
 * The CommandReport

 * The CommandReport class implements the java.io.Serializable interface because
 * CommandReport objects are sent to client applications as JMS ObjectMessages. 
 *
 * Remark: switched MODE constants from TagMode to DataTagConstants TODO may want to move these to .tag package if used for many tag types
 *
 * @author Jan Stowisek
 * @version $Revision: 1.8 $ ($Date: 2005/02/01 17:04:58 $ - $State: Exp $)
 */

public class CommandReportImpl extends ClientRequestReport implements Serializable, CommandReport {
  // ----------------------------------------------------------------------------
  // CONSTANT DEFINITIONS
  // ----------------------------------------------------------------------------

  /** Version number of the class used during serialization/deserialization.
   * This is to ensure that minor changes to the class do not prevent us
   * from reading back CommandReport objects we have serialized earlier. If
   * fields are added/removed from the class, the version number needs to 
   * change.
   */
  private static final long serialVersionUID = -145678L;

  // ----------------------------------------------------------------------------
  // MEMBERS
  // ----------------------------------------------------------------------------

  /**
   * Identifier of the CommandTag associated with this report.
   */
  protected Long commandId;

  /**
   * Status of the command execution. The status must always be one of the STATUS_*
   * constants defined in this class.
   */
  protected CommandExecutionStatus status;

  /**
   * Free-text description of the command execution status. The report text may
   * contain more detailed information about why a command succeeded/failed.
   */
  protected String reportText;
  
  /**
   * Value as returned by the equipment on execution. This is only set if
   * the execution was successful.
   */
  private String returnValue;

  /**
   * Time of command execution or time when command execution was abandoned.
   */
  protected Timestamp timestamp;

  /**
   * Mode of the CommandTag at the time of execution.
   */
  protected short mode;

  // ----------------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------------

  /**
   * Constructor 
   * The timestamp will be set to the time when the object is constructed, 
   * the report text will be initialised with an empty string
   * @param commandId   unique identifier of the command concerned
   * @param status      status of the command execution
   */
  public CommandReportImpl(Long commandId, CommandExecutionStatus status) {
    this(commandId, status, "", new Timestamp(System.currentTimeMillis()),
        DataTagConstants.MODE_OPERATIONAL);
  }
  
  /**
   * Constructor 
   * The timestamp will be set to the time when the object is constructed.
   * @param commandId   unique identifier of the command concerned
   * @param status      status of the command execution
   * @param reportText  more detailed free-text information about the status
   */
  public CommandReportImpl(Long commandId, CommandExecutionStatus status, String reportText) {
    this(commandId, status, reportText,
        new Timestamp(System.currentTimeMillis()), DataTagConstants.MODE_OPERATIONAL);
  }
  
  /**
   * Constructor 
   * @param commandId   unique identifier of the command concerned
   * @param status      status of the command execution
   * @param reportText  more detailed free-text information about the status
   * @param timestamp   time when the command was executed/execution was aborted
   */
  public CommandReportImpl(Long commandId, CommandExecutionStatus status, String reportText, Timestamp timestamp, short mode) {
    
    super();
    
    this.commandId = commandId;
    this.status = status;
    this.reportText = reportText;
    this.timestamp = timestamp;
    this.mode = mode;    
  }

  // ----------------------------------------------------------------------------
  // MEMBER ACCESSORS
  // ----------------------------------------------------------------------------

  /**
   * Return the unique identifier of the CommandTag concerned by this report
   */
  @Override
  public final Long getId() {
    return this.commandId;
  }

  /**
   * Return the execution status.
   */
  @Override
  public final CommandExecutionStatus getStatus() {
    return this.status;
  }

  @Override
  public final String getStatusText() {
    String text = null;

    if (status != null) {
      text = status.getDescription();
    } 
    else {
      text = "UNKNOWN";
    }
    return text;
  }

  /**
   * Return the free-text description of the execution status (if it has been
   * set)
   */
  @Override
  public final String getReportText() {
    return this.reportText;
  }

  
  /**
   * @return The value as returned by the equipment on execution. This is only set if
   * the execution was successful.
   */
  @Override
  public String getReturnValue() {
      return this.returnValue;
  }
  
  
  /**
   * Return the timestamp of the command execution.
   */
  @Override
  public final Timestamp getTimestamp() {
    return this.timestamp;
  }

  @Override
  public final short getMode() {
    return this.mode;
  }

  @Override
  public boolean isOK() {
    return status == CommandExecutionStatus.STATUS_OK;
  }

  /**
   * @param returnValue the returnValue to set
   */
  public void setReturnValue(String returnValue) {
    this.returnValue = returnValue;
  }
}
