package cern.c2mon.cache.actions.process;

import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import lombok.NonNull;

import java.sql.Timestamp;
import java.util.Random;

public class ProcessController {

  /**
   * PIK numbers limit (max)
   */
  private static final int PIK_MAX = 999999;
  /**
   * PIK numbers limit (min)
   */
  private static final int PIK_MIN = 100000;

  private ProcessController() {

  }

  /**
   * Records the start up time of the process and the host it is running on,
   * (and sets it's status to STARTUP)
   *
   * @param process      the Process that is starting
   * @param hostName    the hostname of the Process
   * @param startupTime the start up time
   */
  public static Process start(@NonNull Process process, final String hostName, final Timestamp startupTime) {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;

    final Long newPIK = createProcessPIK();
    processCacheObject.setCurrentHost(hostName);
    processCacheObject.setStartupTime(startupTime);
    processCacheObject.setRequiresReboot(Boolean.FALSE);
    processCacheObject.setProcessPIK(newPIK);
    processCacheObject.setLocalConfig(ProcessCacheObject.LocalConfig.Y);

    return processCacheObject;
  }

  public static Process stop(@NonNull Process process) {
    ProcessCacheObject processCacheObject = (ProcessCacheObject) process;

    processCacheObject.setCurrentHost(null);
    processCacheObject.setStartupTime(null);
    processCacheObject.setRequiresReboot(Boolean.FALSE);
    processCacheObject.setProcessPIK(null);
    processCacheObject.setLocalConfig(null);

    return processCacheObject;
  }

  /**
   * Creation of the random PIK (between PIK_MIN and PIK_MAX)
   */
  private static Long createProcessPIK() {
    Random r = new Random();

    int pik = r.nextInt(PIK_MAX + 1);
    if (pik < PIK_MIN) {
      pik += PIK_MIN;
    }

    return (long) pik;
  }
}
