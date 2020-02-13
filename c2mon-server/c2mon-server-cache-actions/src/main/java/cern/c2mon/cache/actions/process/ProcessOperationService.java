package cern.c2mon.cache.actions.process;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;

import java.sql.Timestamp;

/**
 * @author Szymon Halastra
 */
public interface ProcessOperationService {

  /**
   * Records the start up time of the process and the host it is running on,
   * (and sets it's status to STARTUP - may remove this in the future as duplicate
   * of state tag of the DAQ)
   *
   * <p>Also starts the alive timer.
   *
   * @param processId the Id of the Process that is starting
   * @param hostName the hostname of the Process
   * @param startupTime the start up time
   *
   * @return A copy of the last modifications that were added to the process cache, or <code>null</code>
   */
  Process start(Long processId, String hostName, Timestamp startupTime);

  /**
   * Sets the status of the process to error AND updates
   * the state tag!!
   * @param processId id of the process
   */
  void setErrorStatus(Long processId, String errorMessage);

  /**
   * Returns the process id in the cache for a given Alive Timer id.
   *
   * <p>Throws a {@link CacheElementNotFoundException} if some cache object
   * cannot be located. Throws a {@link NullPointerException} is some parent
   * equipment or process id is not set.
   *
   * <p>Assumes relatedId of Alive Timer is not null.
   *
   * @param id id of Alive Timer of the process/equipment/subequipment linked to the alive
   * @return the Process id above the Alive Timer
   */
  Long getProcessIdFromAlive(Long aliveTimerId);

  /**
   * Returns the id of the Process to which this
   * Control tag belongs, for Control Tag associated to (Sub-)Equipments.
   * @param controlTagId id of Control tag
   * @return the process id; null if no Process can determined
   */
  Long getProcessIdFromControlTag(Long controlTagId);

  /**
   * Searches for the Process with the given name
   *
   * If there are more than one matches, a random one will be returned
   *
   * Will use {@code String.matches()}, so can support full regex
   *
   * @param name
   * @return the process, or throws
   * @throws CacheElementNotFoundException if the name does n
   */
  Process getProcessIdFromName(String name);

  /**
   * Returns true if the DAQ requires a reboot to
   * obtain the latest configuration from the server.
   * @param processId id of the process
   * @return true if restart required
   */
  Boolean isRebootRequired(Long processId);

  /**
   * Sets the Process reboot flag, indicating if the Process
   * needs restarting.
   * @param processId id of the process
   * @param reboot true if restart required
   */
  void setRequiresReboot(Long processId, boolean reboot);

  /**
   * Sets the PIK of the process.
   *
   * @param processId Id of the process
   * @param processPIK The process PIK
   */
  void setProcessPIK(Long processId, Long processPIK);

  /**
   * Sets the Configuration type to Y (Local) or N (Server)
   *
   * @param processId Id of the process
   * @param localConfig Y(LOCAL_CONFIG)/N(SERVER_CONFIG)
   */
  void setLocalConfig(Long processId, ProcessCacheObject.LocalConfig localType);
}
