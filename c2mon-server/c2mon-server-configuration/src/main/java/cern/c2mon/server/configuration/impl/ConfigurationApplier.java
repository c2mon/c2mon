package cern.c2mon.server.configuration.impl;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.configuration.ConfigProgressMonitor;
import cern.c2mon.server.configuration.dao.ConfigurationDAO;
import cern.c2mon.server.daq.out.ProcessCommunicationManager;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Provides a single API for taking in a config report and applying all its elements
 */
@Slf4j
@Named
public final class ConfigurationApplier {

  private final ProcessCommunicationManager processCommunicationManager;
  private final ConfigurationHandlerSelector configurationHandlerSelector;
  private final ConfigurationDAO configurationDAO;
  private final ProcessService processService;
  private final C2monCache<Process> processCache;

  /**
   * Flag recording if configuration events should be sent to the DAQ layer (set in XML).
   */
  boolean daqConfigEnabled;

  @Inject
  public ConfigurationApplier(ProcessCommunicationManager processCommunicationManager,
                              ConfigurationHandlerSelector configurationHandlerSelector, ConfigurationDAO configurationDAO,
                              ProcessService processService,
                              C2monCache<Process> processCache) {
    this.processCommunicationManager = processCommunicationManager;
    this.configurationHandlerSelector = configurationHandlerSelector;
    this.configurationDAO = configurationDAO;
    this.processService = processService;
    this.processCache = processCache;
  }

  /**
   * Apply a list of ConfigurationElements.
   *
   * Note: configuration lock must be acquired before entering.
   *
   * @return the configuration report
   */
  ConfigurationReport applyList(final int configId, final String configName,
                        final List<ConfigurationElement> configElements,
                        final ConfigProgressMonitor configProgressMonitor,
                        final boolean isDBConfig
  ) {
    ConfigurationReport report = new ConfigurationReport(configId, configName, "");

    //map of element reports that need a DAQ child report adding
    Map<Long, ConfigurationElementReport> daqReportPlaceholder = new HashMap<>();
    //map of elements themselves elt_seq_id -> element
    Map<Long, ConfigurationElement> elementPlaceholder = new HashMap<>();
    //map of lists, where each list needs sending to a particular DAQ (processId -> List of events)
    Map<Long, List<Change>> processLists = new HashMap<>();

    if (configProgressMonitor != null){
      configProgressMonitor.serverTotalParts(configElements.size());
      configProgressMonitor.resetCounter();
    }

    if (!isDBConfig && runInParallel(configElements)) {
      log.debug("Enter parallel configuration");
      parallelConfiguration(configId, configElements, configProgressMonitor, report, daqReportPlaceholder, elementPlaceholder, processLists);
    } else {
      log.debug("Enter serialized configuration");
      configElements.forEach(element ->
        applyConfigurationElement(element, processLists, elementPlaceholder, daqReportPlaceholder, report, configId, configProgressMonitor));
    }

    //send events to Process if enabled, convert the responses and introduce them into the existing report; else set all DAQs to restart
    if (daqConfigEnabled) {
      if (configProgressMonitor != null){
        configProgressMonitor.daqTotalParts(processLists.size());
        configProgressMonitor.resetCounter();
      }

      log.info(configId + " Reconfiguring " + processLists.keySet().size()+ " processes ...");

      for (Long processId : processLists.keySet()) {

        List<Change> processChangeEvents = processLists.get(processId);
        if (processService.isRunning(processId) && !processService.isRebootRequired(processId)) {
          try {
            log.trace(configId + " Sending " + processChangeEvents.size() + " change events to process " + processId + "...");
            ConfigurationChangeEventReport processReport = processCommunicationManager.sendConfiguration(processId, processChangeEvents);
            if (!processReport.getChangeReports().isEmpty()) {

              log.trace(configId + " Received " + processReport.getChangeReports().size() + " back from process.");
            } else {
              log.trace(configId + " Received 0 reports back from process");
            }
            for (ChangeReport changeReport : processReport.getChangeReports()) {
              ConfigurationElementReport convertedReport =
                ConfigurationReportConverter.fromProcessReport(changeReport, daqReportPlaceholder.get(changeReport.getChangeId()));
              daqReportPlaceholder.get(changeReport.getChangeId()).addSubReport(convertedReport);
              //if change report has REBOOT status, mark this DAQ for a reboot in the configuration
              if (changeReport.isReboot()) {
                report.addProcessToReboot(processCache.get(processId).getName());
                elementPlaceholder.get(changeReport.getChangeId()).setDaqStatus(ConfigConstants.Status.RESTART);
                //TODO set flag & tag to indicate that process restart is needed
              } else if (changeReport.isFail()) {
                log.debug(configId + " changeRequest failed at process " + processCache.get(processId).getName());
                report.addStatus(ConfigConstants.Status.FAILURE);
                report.setStatusDescription("Failed to apply the configuration successfully. See details in the report below.");
                elementPlaceholder.get(changeReport.getChangeId()).setDaqStatus(ConfigConstants.Status.FAILURE);
              //success, override default failure
              } else {
                if (elementPlaceholder.get(changeReport.getChangeId()).getDaqStatus().equals(ConfigConstants.Status.RESTART)) {
                  elementPlaceholder.get(changeReport.getChangeId()).setDaqStatus(ConfigConstants.Status.OK);
                }
              }
            }
          } catch (Exception e) {
            String errorMessage = "Error during DAQ reconfiguration: unsuccessful application of configuration (possible timeout) to Process " + processCache.get(processId).getName();
            log.error(errorMessage, e);
            processService.setRequiresReboot(processId, true);
            report.addProcessToReboot(processCache.get(processId).getName());
            report.addStatus(ConfigConstants.Status.FAILURE);
            report.setStatusDescription(report.getStatusDescription() + errorMessage + "\n");
          }
        } else {
          processService.setRequiresReboot(processId, true);
          report.addProcessToReboot(processCache.get(processId).getName());
          report.addStatus(ConfigConstants.Status.RESTART);
        }
        if (configProgressMonitor != null) {
          configProgressMonitor.incrementDaqProgress();
        }

      }
    } else {
      log.debug("DAQ runtime reconfiguration not enabled - setting required restart flags");
      if (!processLists.isEmpty()){
        report.addStatus(ConfigConstants.Status.RESTART);
        for (Long processId : processLists.keySet()) {
          processService.setRequiresReboot(processId, true);
          report.addProcessToReboot(processCache.get(processId).getName());
        }
      }
    }

    if (isDBConfig) {
      //save Configuration element status information in the DB tables
      for (ConfigurationElement element : configElements) {
        configurationDAO.saveStatusInfo(element);
      }
      //mark the Configuration as applied in the DB table, with timestamp set
      configurationDAO.markAsApplied(configId);
    }

    log.info("Finished applying configuraton " + configId);

    report.normalize();

    return report;
  }

  private void parallelConfiguration(int configId, List<ConfigurationElement> configElements, ConfigProgressMonitor configProgressMonitor, ConfigurationReport report, Map<Long, ConfigurationElementReport> daqReportPlaceholder, Map<Long, ConfigurationElement> elementPlaceholder, Map<Long, List<Change>> processLists) {
    // TODO (Alex) Switch this to commonPool
    ForkJoinPool forkJoinPool = new ForkJoinPool(10);
    try {
      //https://blog.krecan.net/2014/03/18/how-to-specify-thread-pool-for-java-8-parallel-streams/
      forkJoinPool.submit(() ->
        configElements.parallelStream().forEach(element ->
          applyConfigurationElement(element, processLists, elementPlaceholder, daqReportPlaceholder, report, configId, configProgressMonitor))
      ).get(300, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      String errorMessage = "Error applying configuration elements in parallel, timeout after 5 minutes";
      log.error(errorMessage, e);
      report.addStatus(ConfigConstants.Status.FAILURE);
      report.setStatusDescription(report.getStatusDescription() + errorMessage + "\n");
    } catch (InterruptedException | ExecutionException e) {
      String errorMessage = "Error applying configuration elements in parallel";
      log.error(errorMessage, e);
      report.addStatus(ConfigConstants.Status.FAILURE);
      report.setStatusDescription(report.getStatusDescription() + errorMessage + "\n");
    }
  }

  /**
   * Determine if the configuration can be applied in parallel.
   *
   * @param elements List of entities which needs to be configured.
   * @return True if the configuration can be applied in parallel.
   */
  private boolean runInParallel(List<ConfigurationElement> elements) {
    return elements.stream().noneMatch(element ->
      element.getEntity().equals(ConfigConstants.Entity.SUBEQUIPMENT) ||
        element.getEntity().equals(ConfigConstants.Entity.EQUIPMENT) ||
        element.getEntity().equals(ConfigConstants.Entity.PROCESS) ||
        element.getAction().equals(ConfigConstants.Action.REMOVE));
  }

  /**
   * Apply the list of ConfigurationElement to the server and creates a list of
   * change elements for the daq configuration.
   *
   * @param element The configuration which needs to be applied to the server.
   * @param processLists A map which will be filled with the changes based on the process the change belongs to.
   * @param elementPlaceholder A Map which contains the configuration element based on the id.
   * @param daqReportPlaceholder  A Map which contains the report of the configuration based on the id.
   * @param report The overall configuration report.
   * @param configId The id of the current configuration.
   * @param configProgressMonitor The monitor which observes the progress of the configuration.
   */
  private void applyConfigurationElement(ConfigurationElement element, Map<Long, List<Change>> processLists,
                                         Map<Long, ConfigurationElement> elementPlaceholder,
                                         Map<Long, ConfigurationElementReport> daqReportPlaceholder,
                                         ConfigurationReport report, Integer configId,
                                         final ConfigProgressMonitor configProgressMonitor){
    if (element.getEntity().equals(ConfigConstants.Entity.MISSING)) {
      ConfigurationElementReport elementReport = new ConfigurationElementReport(element.getAction(), element.getEntity(), element.getEntityId());
      elementReport.setWarning("Entity " + element.getEntityId() + " does not exist");
      report.addElementReport(elementReport);
      report.setStatusDescription("Please check subreport description for details");
      report.addStatus(ConfigConstants.Status.WARNING);
      return;
    }
    //initialize success report
    ConfigurationElementReport elementReport = new ConfigurationElementReport(element.getAction(),
      element.getEntity(),
      element.getEntityId());
    report.addElementReport(elementReport);
    List<ProcessChange> processChanges;
    try {
      //never returns null
      processChanges = configurationHandlerSelector.applyConfigElement(element, elementReport);

      if (processChanges != null) {

        for (ProcessChange processChange : processChanges) {
          handleProcessChange(element, processLists, elementPlaceholder, daqReportPlaceholder, report, configId, elementReport, processChange);
        }
      }
    } catch (Exception ex) {
      String errMessage = configId + " Exception caught while applying the configuration change (Action, Entity, " +
        "Entity id) = (" + element.getAction() + "; " + element.getEntity() + "; " + element.getEntityId() + ")";
      log.error(errMessage, ex.getMessage());
      elementReport.setFailure("Exception caught while applying the configuration change.", ex);
      element.setStatus(ConfigConstants.Status.FAILURE);
      report.addStatus(ConfigConstants.Status.FAILURE);
      report.setStatusDescription("Failure: see details below.");
    }
    if (configProgressMonitor != null){
      configProgressMonitor.incrementServerProgress(element.buildDescription());
    }
  }

  private void handleProcessChange(ConfigurationElement element,
                                   Map<Long, List<Change>> processLists,
                                   Map<Long, ConfigurationElement> elementPlaceholder,
                                   Map<Long, ConfigurationElementReport> daqReportPlaceholder,
                                   ConfigurationReport report,
                                   Integer configId,
                                   ConfigurationElementReport elementReport,
                                   ProcessChange processChange) {
    Long processId = processChange.getProcessId();
    if (processChange.processActionRequired()) {

      if (!processLists.containsKey(processId)) {
        processLists.put(processId, new ArrayList<>());
      }

      //cast to implementation needed as DomFactory uses this - TODO change to interface
      processLists.get(processId).add((Change) processChange.getChangeEvent());

      if (processChange.hasNestedSubReport()) {
        elementReport.addSubReport(processChange.getNestedSubReport());
        daqReportPlaceholder.put(processChange.getChangeEvent().getChangeId(), processChange.getNestedSubReport());
      } else {
        daqReportPlaceholder.put(processChange.getChangeEvent().getChangeId(), elementReport);
      }

      elementPlaceholder.put(processChange.getChangeEvent().getChangeId(), element);
      //default to restart; if successful on DAQ layer switch to OK
      element.setDaqStatus(ConfigConstants.Status.RESTART);
    } else if (processChange.requiresReboot()) {
      log.debug(configId + " RESTART for " + processChange.getProcessId() + " required");
      element.setDaqStatus(ConfigConstants.Status.RESTART);
      report.addStatus(ConfigConstants.Status.RESTART);
      report.addProcessToReboot(processCache.get(processId).getName());
      element.setStatus(ConfigConstants.Status.RESTART);
      processService.setRequiresReboot(processId, Boolean.TRUE);
    }
  }
}
