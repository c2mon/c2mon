package cern.c2mon.server.shorttermlog.mapper;

import java.util.List;

import cern.tim.shared.client.command.CommandTagLog;

/**
 * Mybatis mapper interface for logging executed Commands to
 * to the STL (with report details).
 * 
 * @author Mark Brightwell
 *
 */
public interface CommandTagLogMapper extends LoggerMapper<CommandTagLog> {

  /**
   * Returns all command logs for this command.
   * Used for test only, as does *not* return
   * correct timestamp (not adjusted for UTC).
   * 
   * @param id of the command
   * @return a list of log events
   */
  List<CommandTagLog> getCommandTagLog(Long id);
  
  /**
   * Delete all entries for this command.
   * Used for test only.
   * 
   * @param id of the command logs to remove
   */
  void deleteAllLogs(Long id);
  
}
