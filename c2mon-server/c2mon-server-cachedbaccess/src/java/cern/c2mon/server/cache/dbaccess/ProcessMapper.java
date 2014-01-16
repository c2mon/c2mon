package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.process.Process;

public interface ProcessMapper extends LoaderMapper, PersistenceMapper<Process> {
  void insertProcess(Process process);
  void deleteProcess(Long id);
  
  /**
   * Updates the configuration data in the database by
   * accessing the appropriate fields in the Process object.
   * @param process the object for which the process has been updated
   */
  void updateProcessConfig(Process process);
}
