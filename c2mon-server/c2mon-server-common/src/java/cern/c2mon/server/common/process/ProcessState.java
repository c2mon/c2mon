package cern.c2mon.server.common.process;

public interface ProcessState {
  
  /**
   * The state tag of a process is set to the value PROCESS_CREATED immediately
   * after the process itself (entity bean) has been created.
   */
  String PROCESS_CREATED = "CREATED";

  /**
   * The state tag of a process is set to the value PROCESS_REMOVED once the 
   * Process itself has been removed.
   */
  String PROCESS_REMOVED = "REMOVED";

  /**
   * The state tag of a process is set to the value PROCESS_DOWN when big 
   * brother believes that the DAQ process is not running (e.g. when an alive
   * has expired).
   */
  String PROCESS_DOWN = "DOWN";

  /**
   * The state tag of a process is set to PROCESS_STARTUP as soon as
   * a ProcessConnectionRequest has been received by the server. It is
   * then moved to PROCESS_RUNNING once the start up has been authorized
   * and the XML configuration returned (the alive timer has also been
   * initialized and will only expire if no alive message is received
   * within the process-specific delay.
   */
  String PROCESS_STARTUP = "STARTUP";

  /**
   * The state tag of a process is set to the value PROCESS_RUNNING when big 
   * brother believes that the DAQ process is running.
   */
  String PROCESS_RUNNING = "RUNNING";
  
  /**
   * Something was reconfigured within this process and it needs a restart.
   */
  String PROCESS_RECONFIGURED = "RECONFIGURED";
  
  /**
   * Error occurred during reconfiguration of this process or some contained entity.
   */
  String PROCESS_ERROR = "ERROR";
  
  String CAUSE_STOPPED="The DAQ process was stopped.";
  
  String CAUSE_ALIVE_EXPIRATION="The alive timer of the DAQ process expired.";

}
