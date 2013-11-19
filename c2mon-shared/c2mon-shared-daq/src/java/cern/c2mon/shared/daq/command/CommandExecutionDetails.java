package cern.c2mon.shared.daq.command;

import java.sql.Timestamp;

/**
 * Contains details of a command execution. Only
 * to be used on the server.
 * 
 * @param <T> the type of the command value
 * @author Mark Brightwell
 *
 */
public class CommandExecutionDetails<T> {

  /**
   * Object value set for command execution.
   */
  private T value;
  
  /**
   * Start time of execution on server.
   */
  private Timestamp executionStartTime;
  
  /**
   * Time when response received from DAQ.
   */
  private Timestamp executionEndTime;
  
  /**
   * User requesting the execution.
   */
  private String username;
  
  /**
   * Host from which the execution was made.
   */
  private String host;

  /**
   * @return the value
   */
  public T getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final T value) {
    this.value = value;
  }

  /**
   * @return the executionStartTime
   */
  public Timestamp getExecutionStartTime() {
    return executionStartTime;
  }

  /**
   * @param executionStartTime the executionStartTime to set
   */
  public void setExecutionStartTime(final Timestamp executionStartTime) {
    this.executionStartTime = executionStartTime;
  }

  /**
   * @return the executionEndTime
   */
  public Timestamp getExecutionEndTime() {
    return executionEndTime;
  }

  /**
   * @param executionEndTime the executionEndTime to set
   */
  public void setExecutionEndTime(final Timestamp executionEndTime) {
    this.executionEndTime = executionEndTime;
  }

  /**
   * @return User requesting the execution
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username the user requesting the execution
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the host from which the execution was made
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host the host from which the execution was made
   */
  public void setHost(String host) {
    this.host = host;
  }
}

