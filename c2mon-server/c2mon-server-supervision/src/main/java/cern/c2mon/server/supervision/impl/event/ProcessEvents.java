package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import cern.c2mon.shared.daq.process.*;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Collection;

@Slf4j
public class ProcessEvents extends SupervisionEventHandler<Process> {

  private XMLConverter xmlConverter = new XMLConverter();

  @Inject
  public ProcessEvents(ProcessService processService) {
    super(Process.class, processService);
  }

  /**
   * TODO write...
   * The onUp() method is called in one of the following cases:
   * <UL>
   * <LI>an alive tag attached to a process/equipment has been received
   * <LI>a communication fault indicating an equipment is running has been received.
   * </UL>
   * The onUp() method sets the value of the state tag associated with the
   * process/equipment to "RUNNING". If the value of the state tag is already
   * "RUNNING", no further action is taken.
   * <p>
   * FURTHER DETAILS:
   * <p>
   * If the state tag is valid and not "RUNNING", it is updated to "RUNNING" (as is the associated
   * process field). In addition, if the state is DOWN due to an alive expiration,
   * then a request is made to the DAQ to send the latest values.
   * <p>
   * Notice that the process alive is of course revalidated on reception of a new alive.
   */
  @Override
  public void onUp(Process supervised, Timestamp timestamp, String message) {
    service.resume(supervised.getId(), timestamp, message);

    // TODO (Alex) Where and how can we "revalidate"? And how can we ask the DAQ for the latest values?

//    final Process process = processCache.getCopy(processId);
//
//    //check state tag is correctly set
//    Long stateTagId = process.getStateTagId(); //never null
//    controlTagCache.acquireWriteLockOnKey(stateTagId);
//    try {
//      ControlTag stateTag = controlTagCache.get(stateTagId);
//
//      if (stateTag.getValue() == null || !stateTag.getValue().equals(SupervisionConstants.SupervisionStatus.RUNNING.toString()) || !stateTag.isValid()) {
//
//        // If the process is running under a local configuration, set the status
//        // tag to RUNNING_LOCAL
//        if (process.getLocalConfig() != null && process.getLocalConfig().equals(ProcessCacheObject.LocalConfig.Y)) {
//          log.debug("onProcessUp(): Process is running on a local configuration, setting status to RUNNING_LOCAL");
//          controlTagFacade.updateAndValidate(stateTagId, SupervisionConstants.SupervisionStatus.RUNNING_LOCAL.toString(), pMessage, pTimestamp);
//
//        } else if (stateTag.getValue() == null || !stateTag.getValue().equals(SupervisionConstants.SupervisionStatus.RUNNING.toString())) {
//          controlTagFacade.updateAndValidate(stateTagId, SupervisionConstants.SupervisionStatus.RUNNING.toString(), pMessage, pTimestamp);
//        }
//      }
//    } catch (CacheElementNotFoundException controlCacheEx) {
//      log.error("Unable to locate state tag in cache (id is " + stateTagId + ")", controlCacheEx);
//    } finally {
//      controlTagCache.releaseWriteLockOnKey(stateTagId);
//    }
  }


  public String onConnection(final ProcessConnectionRequest processConnectionRequest) {
    ProcessConnectionResponse processConnectionResponse = new ProcessConnectionResponse();

    // Protect the method against accidental null parameters
    if (processConnectionRequest == null) {
      log.error("onProcessConfiguration(null) called - rejecting the request.");
      processConnectionResponse.setprocessPIK(ProcessConnectionResponse.PIK_REJECTED);
      return this.xmlConverter.toXml(processConnectionResponse);
    }

    // Process name (NO_PROCESS by default)
    processConnectionResponse.setProcessName(processConnectionRequest.getProcessName());

    // Print some debug information
    if (log.isDebugEnabled()) {
      StringBuilder str = new StringBuilder("onProcessConnection([");
      str.append(processConnectionRequest.getProcessName());
      str.append(", ");
      str.append(processConnectionRequest.getProcessHostName());
      str.append(", ");
      str.append(processConnectionRequest.getProcessStartupTime());
      str.append("]) called.");
      log.debug(str.toString());
    }

    // Retrieve the cache object for the process as well
    try {
      Long processId = processCache.getProcessId(processConnectionRequest.getProcessName());
      processCache.acquireWriteLockOnKey(processId);
      try {
        Process process = processCache.get(processId);
        try {
          // If process is already currently running
          if (this.processFacade.isRunning(processId)) {
            // And TEST mode is on
            if (properties.isTestMode()) {
              log.info("onProcessConnection - TEST mode - Connection request for DAQ " + process.getName() + " authorized.");

              // Start Up the process
              this.controlTagFacade.updateAndValidate(process.getStateTagId(), SupervisionConstants.SupervisionStatus.STARTUP.toString(), "ProcessConnection message received.",
                processConnectionRequest.getProcessStartupTime());
              process = this.processFacade.start(processId, processConnectionRequest.getProcessHostName(), processConnectionRequest.getProcessStartupTime());

              // PIK
              processConnectionResponse.setprocessPIK(process.getProcessPIK());

              log.info("onProcessConnection - TEST Mode - Returning PIKResponse to DAQ " + process.getName()
                + ", PIK " + process.getProcessPIK());

              // If process is already currently running and TEST mode is off no connection is permitted
            } else {
              // Reject Connection
              processConnectionResponse.setprocessPIK(ProcessConnectionResponse.PIK_REJECTED);
              log.warn("onProcessConnection - The DAQ process is already running, returning rejected connection : "
                + processConnectionRequest.getProcessName());
            }
            // If process is not currently running the connection is permitted
          } else {
            log.info("onProcessConnection - Connection request for DAQ " + process.getName() + " authorized.");

            // Start Up the process
            this.controlTagFacade.updateAndValidate(process.getStateTagId(), SupervisionConstants.SupervisionStatus.STARTUP.toString(), "ProcessConnection message received.",
              processConnectionRequest.getProcessStartupTime());
            process = this.processFacade.start(processId, processConnectionRequest.getProcessHostName(), processConnectionRequest.getProcessStartupTime());

            // PIK
            processConnectionResponse.setprocessPIK(process.getProcessPIK());

            log.info("onProcessConnection - Returning PIKResponse to DAQ " + process.getName());
          }

        } catch (CacheElementNotFoundException cacheEx) {
          log.error("State tag " + process.getStateTagId() + " or the alive tag for process " + process.getId() +
            "could not be found in the cache.");
        }
      } finally {
        processCache.releaseWriteLockOnKey(processId);
      }
    } catch (CacheElementNotFoundException cacheEx) {
      log.warn("onProcessConnection - process not found in cache (name = "
        + processConnectionRequest.getProcessName() + ") - unable to accept connection request.", cacheEx);
      processConnectionResponse.setprocessPIK(ProcessConnectionResponse.PIK_REJECTED);
    } catch (Exception e) {
      log.error(new StringBuffer("onProcessConnection - An unexpected Exception occurred.").toString(), e);
      processConnectionResponse.setprocessPIK(ProcessConnectionResponse.PIK_REJECTED);
    }

    return this.xmlConverter.toXml(processConnectionResponse);
  }

  /**
   * Synchronized on the Process cache object. Catches all exceptions. There is no need to
   * send Process PIK to get the configuration file (Test mode can access it then)
   *
   * @param processConfigurationRequest the configuration request object
   * @return the configuration XML as a String or null if there was an exception
   */
  public String onConfiguration(final ProcessConfigurationRequest processConfigurationRequest) {
    ProcessConfigurationResponse processConfigurationResponse = new ProcessConfigurationResponse();

    // Protect the method against accidental null parameters
    if (processConfigurationRequest == null) {
      log.error("onProcessConfiguration(null) called - rejecting the request.");
      processConfigurationResponse.setConfigurationXML(ProcessConfigurationResponse.CONF_REJECTED);
      return this.xmlConverter.toXml(processConfigurationResponse);
    }

    // Process name (NO_PROCESS by default)
    processConfigurationResponse.setProcessName(processConfigurationRequest.getProcessName());

    // Print some debug information
    log.debug("onProcessConfiguration([" + processConfigurationRequest.getProcessName() +
      ", " +
      processConfigurationRequest.getProcessPIK() +
      "]) called.");

    // Retrieve the cache object for the process as well as its state tag
    try {
      Long processId = processCache.getProcessId(processConfigurationRequest.getProcessName());
      Process processCopy = processCache.getCopy(processId);
      log.info("onProcessConfiguration - Configuration request for DAQ " + processCopy.getName() + " authorized.");

      // Only If PIK is the same we have in cache we change Y(LOCAL_CONFIG) by N(SERVER_CONFIG) for the current process
      if (processConfigurationRequest.getProcessPIK().equals(processCopy.getProcessPIK())) {
        log.info("onProcessConfiguration - SERVER_CONFIG");
        this.processFacade.setLocalConfig(processId, ProcessCacheObject.LocalConfig.N);
      }

      // We get the configuration XML file (empty by default)
      processConfigurationResponse.setConfigurationXML(processXMLProvider.getProcessConfigXML(processCopy));
      log.info("onProcessConfiguration - Returning configuration XML to DAQ " + processCopy.getName());
    } catch (CacheElementNotFoundException cacheEx) {
      log.warn("onProcessConfiguration - process not found in cache (name = "
        + processConfigurationRequest.getProcessName() + ") - unable to accept connection request.", cacheEx);
      processConfigurationResponse.setConfigurationXML(ProcessConfigurationResponse.CONF_REJECTED);
    } catch (Exception e) {
      log.error(new StringBuffer("onProcessConfiguration - An unexpected Exception occurred.").toString(), e);
      processConfigurationResponse.setConfigurationXML(ProcessConfigurationResponse.CONF_REJECTED);
    }

    return this.xmlConverter.toXml(processConfigurationResponse);
  }

  public void onDisconnection(final ProcessDisconnectionRequest processDisconnectionRequest) {
    // Protect the method against accidental null parameters
    if (processDisconnectionRequest == null) {
      log.error("onProcessDisconnection(null) called - ignoring the request.");
      return;
    }

    // (1) Print some debug output

    if (log.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("onProcessDisconnection([");
      str.append(processDisconnectionRequest.getProcessName());
      str.append(", ");
      str.append(processDisconnectionRequest.getProcessPIK());
      str.append(", ");
      str.append(processDisconnectionRequest.getProcessStartupTime());
      str.append("]) called.");
      log.debug(str.toString());
    }
    //TODO remove inaccessible once fixed on client
    //int invalidationFlags = DataTagQuality.INACCESSIBLE + DataTagQuality.PROCESS_DOWN;

    try {

      Long processId;
      // (2) Check if the process exists (get methods throw exception otherwise) //check if id was sent - if not use name field
      if (processDisconnectionRequest.getProcessId() != ProcessDisconnectionRequest.NO_ID) {
        processId = processDisconnectionRequest.getProcessId();
      } else {
        processId = processCache.getProcessId(processDisconnectionRequest.getProcessName());
      }

      // (3) Get the corresponding state and alive tags
      try {
        Process processCopy = processCache.getCopy(processId);
        try {
          // If the process is stopped the PIK is null
          if (processCopy.getProcessPIK() == null) {
            log.warn("onProcessDisconnection - Received Process disconnection message for "
              + "a process PIK null. Id is " + processCopy.getId() + ". Message ignored.");
          } else {
            // Check if PIK is the same we disconnect otherwise we ignore the message
            if (processDisconnectionRequest.getProcessPIK().equals(processCopy.getProcessPIK())) {
              // (4) Only proceed if the process is actually running
              if (processFacade.isRunning(processId)) {

                String processStopMessage = "DAQ process " + processCopy.getName() + " was stopped.";
                log.trace("onProcessDisconnection - " + processStopMessage);

                // (5) LEGACY: TODO what does this comment mean?!
                Timestamp stopTime = new Timestamp(System.currentTimeMillis());
                this.processFacade.stop(processId, stopTime); //also stops alive timer

                // (7) Update process state tag
                stopStateTag(processCopy.getStateTagId(), stopTime, processStopMessage);
                stopEquipments(processCopy.getEquipmentIds(), stopTime, processStopMessage); //state tags to stopped
                // (8) Invalidate alive tag (no need to synchronize here as no "if then" update statement
                //dataTagFacade.setQuality(aliveTag, invalidationFlags, 0, processStopMessage, stopTime);
                // (9) Invalidate attached equipment (if any) -- keep lock on parent process (TODO config loader should hold lock on process while it runs?)
                //setControlTagsQuality(process, processStopMessage, stopTime,
                //                        true, true, true,                       //invalidate state, alive and commfault tags
                //                        invalidationFlags, 0);
              } else {
                log.warn("onProcessDisconnection - Received Process disconnection message for "
                  + "a process that is not running. Id is " + processCopy.getId() + ". Message ignored.");
              }
            } else {
              log.warn("onProcessDisconnection - Received Process disconnection message for "
                + "a process with a diferent PIK (" + processDisconnectionRequest.getProcessPIK()
                + " vs " + processCopy.getProcessPIK() + "). Id is " + processCopy.getId() + ". Message ignored.");
            }
          }
        } catch (CacheElementNotFoundException cacheEx) {
          log.error("State tag " + processCopy.getStateTagId() + " or the alive tag "
            + processCopy.getAliveTagId() + " for process " + processCopy.getId() + " could not be found in the "
            + "cache - disconnection actions could not be completed.", cacheEx);
        }

      } catch (CacheElementNotFoundException cacheEx) {
        log.error("Unable to locate process " + processId + " in cache.", cacheEx);
      }
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("Process object could not be retrieved from cache - disconnection actions may be incomplete.", cacheEx);
    } catch (IllegalArgumentException argEx) {
      log.error("IllegalArgument exception caught on processing DAQ disconnection - disconnection actions may be incomplete.", argEx);
    } catch (NullPointerException nullEx) {
      log.error("NullPointer exception caught on processing DAQ disconnection - disconnection actions may be incomplete.", nullEx);
    }
  }


  /**
   * Stops the state tag (for Process disconnection).
   *
   * @param stateTagId id of tag
   * @param pTimestamp time to use
   * @param message    stop message
   */
  private void stopStateTag(final Long stateTagId, final Timestamp pTimestamp, final String message) {
    controlTagFacade.updateAndValidate(stateTagId, SupervisionConstants.SupervisionStatus.DOWN.toString(), message, pTimestamp);
  }

  /**
   * Stops equipments and subequipments linked to this process (updates the cache objects & state tags).
   * Called on DAQ disconnection (not alive expiry).
   *
   * @param equipmentIds List of equipment ids of a given Process
   * @param timestamp    time of stop
   * @param message      stop message
   */
  private void stopEquipments(final Collection<Long> equipmentIds, final Timestamp timestamp, final String message) {
    Equipment currentEquipmentCopy;
    for (Long equipmentId : equipmentIds) {
      try {
        try {
          currentEquipmentCopy = equipmentCache.getCopy(equipmentId);
          equipmentFacade.stop(equipmentId, timestamp);
          stopStateTag(currentEquipmentCopy.getStateTagId(), timestamp, message);
          for (Long subId : currentEquipmentCopy.getSubEquipmentIds()) {
            try {
              SubEquipment subEquipmentCopy = subEquipmentCache.getCopy(subId);
              stopStateTag(subEquipmentCopy.getStateTagId(), timestamp, message);
              subEquipmentFacade.stop(subId, timestamp);
            } catch (CacheElementNotFoundException ex) {
              log.error("Subequipment could not be retrieved from cache - unable to update Subequipment state.", ex);
            }
          }
        } catch (CacheElementNotFoundException e) {
          log.error("Equipment could not be retrieved from cache - unable to update Equipment state.", e);
        }
      } catch (Exception e) {
        log.error("Unable to acquire lock on Equipment/SubEquipment object", e);
      }
    }
  }

}
