/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.configuration.impl;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.util.KotlinAPIs;
import cern.c2mon.server.configuration.ConfigProgressMonitor;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.config.ConfigurationProperties;
import cern.c2mon.server.configuration.dao.ConfigurationDAO;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.daq.out.ProcessCommunicationManager;
import cern.c2mon.shared.client.configuration.*;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.converter.DateFormatConverter;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.transform.RegistryMatcher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of the server ConfigurationLoader bean.
 *
 * <p>This implementation uses the injected DAO for all database access,
 * so alternative DAO implementation can be wired in if required. The
 * default provided DAO uses iBatis in the background.
 *
 * <p>Notice that creating a cache object will also notify any update
 * listeners. In particular, new datatags, rules and control tags will
 * be passed on to the client, history module etc.
 *
 * <p>Creations of processes and equipments require a DAQ restart.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Component
public class ConfigurationLoaderImpl implements ConfigurationLoader {

  //TODO element & element report status always both need updating - redesign this part

  private AtomicInteger changeId = new AtomicInteger(0); //unique id for all generated changes (including those recursive ones during removal)

  private final ProcessCommunicationManager processCommunicationManager;

  private final ConfigurationDAO configurationDAO;

  private final SequenceDAO sequenceDAO;

  private final ProcessService processService;

  private final C2monCache<Process> processCache;

  /**
   * Flag recording if configuration events should be sent to the DAQ layer (set in XML).
   */
  private boolean daqConfigEnabled;

  private ConfigurationHandlerActor configHandlerActor;
  /**
   * The directory name in C2MON home where the configreports will be saved.
   */
  private String reportDirectory;

  /**
   * Flag indicating if a cancel request has been made.
   */
  private volatile boolean cancelRequested = false;

  /**
   * singelton helper-object for parsing POJO Configuration objects into ConfigurationElements
   */
  private ConfigurationParser configParser;

  @Inject
  public ConfigurationLoaderImpl(ProcessCommunicationManager processCommunicationManager,
                                 ConfigurationDAO configurationDAO,
                                 ProcessService processService,
                                 C2monCache<Process> processCache,
                                 ConfigurationParser configParser,
                                 SequenceDAO sequenceDAO,
                                 ConfigurationProperties properties,
                                 ConfigurationHandlerActor configHandlerActor,
                                 ServerProperties serverProperties) {
    super();
    this.processCommunicationManager = processCommunicationManager;
    this.configurationDAO = configurationDAO;
    this.processService = processService;
    this.processCache = processCache;
    this.configParser = configParser;
    this.sequenceDAO = sequenceDAO;
    this.daqConfigEnabled = properties.isDaqConfigEnabled();
    this.configHandlerActor = configHandlerActor;
    this.reportDirectory = serverProperties.getHome() + "/reports";
  }

  @Override
  public ConfigurationReport applyConfiguration(Configuration configuration) {
    log.info("Applying configuration with {} item(s)", configuration.getEntities().size());
    Long configId = -1L;
    ConfigurationReport report = null;

    try {
      configId = sequenceDAO.getNextConfigId();
      List<ConfigurationElement> configurationElements = configParser.parse(configuration);

      report = applyConfiguration(configId.intValue(), configuration.getName(), configurationElements, null, false);

    } catch (Exception ex) {
      log.error("Exception caught while applying configuration", ex);
      report = new ConfigurationReport(configId, configuration.getName(), "", Status.FAILURE, "Exception caught when applying configuration");
      report.setExceptionTrace(ex);
      throw new ConfigurationException(report, ex);
    } finally {
      if (report != null) {
        archiveReport(configId.toString(), report.toXML());
      }
    }

    return report;
  }

  @Override
  public ConfigurationReport applyConfiguration(final int configId, final ConfigProgressMonitor configProgressMonitor) {
    log.info(configId + " Applying configuration");
    ConfigurationReport report = null;

    try {

      String configName = configurationDAO.getConfigName(configId);
      if (configName == null) {
        log.warn(configId + " Unable to locate configuration - cannot be applied.");
        return new ConfigurationReport(
            configId,
            "UNKNOWN",
            "", //TODO set user name through RBAC once available
            Status.FAILURE,
            "Configuration with id <" + configId + "> not found. Please try again with a valid configuration id"
          );
      }

      List<ConfigurationElement> configElements;
      try {
        log.debug(configId + " Fetching configuration items from DB...");
        configElements = configurationDAO.getConfigElements(configId);
        log.debug(configId + " Got " + configElements.size() + " elements from DB");
      } catch (Exception e) {
        String message = "Exception caught while loading the configuration for " + configId + " from the DB: " + e.getMessage();
        log.error(message, e);
        throw new RuntimeException(message, e);
      }

      report = applyConfiguration(configId, configName, configElements, configProgressMonitor, true);

    } catch (Exception ex) {
      log.error("Exception caught while applying configuration " + configId, ex);
        report = new ConfigurationReport(configId, "UNKNOWN", "", Status.FAILURE,
            "Exception caught when applying configuration with id <" + configId + ">.");
        report.setExceptionTrace(ex);
      throw new ConfigurationException(report, ex);
    } finally {
      if (report != null) {
        archiveReport(String.valueOf(configId), report.toXML());
      }
    }

    return report;
  }

  /**
   * Private method to apply a list of ConfigurationElements.
   *
   * Note: configuration lock must be acquired before entering.
   *
   * @param configId
   * @param configName
   * @param configElements
   * @param configProgressMonitor
   * @param isDBConfig
   * @return the configuration report
   */
  private ConfigurationReport applyConfiguration(final int configId, final String configName,
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
          report.addStatus(Status.FAILURE);
          report.setStatusDescription(report.getStatusDescription() + errorMessage + "\n");
      } catch (InterruptedException | ExecutionException e) {
          String errorMessage = "Error applying configuration elements in parallel";
          log.error(errorMessage, e);
          report.addStatus(Status.FAILURE);
          report.setStatusDescription(report.getStatusDescription() + errorMessage + "\n");
      }
    } else {
      log.debug("Enter serialized configuration");
      configElements.stream().forEach(element ->
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
        if (!cancelRequested){
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
                  elementPlaceholder.get(changeReport.getChangeId()).setDaqStatus(Status.RESTART);
                  //TODO set flag & tag to indicate that process restart is needed
                } else if (changeReport.isFail()) {
                  log.debug(configId + " changeRequest failed at process " + processCache.get(processId).getName());
                  report.addStatus(Status.FAILURE);
                  report.setStatusDescription("Failed to apply the configuration successfully. See details in the report below.");
                  elementPlaceholder.get(changeReport.getChangeId()).setDaqStatus(Status.FAILURE);
                } else { //success, override default failure
                  if (elementPlaceholder.get(changeReport.getChangeId()).getDaqStatus().equals(Status.RESTART)) {
                    elementPlaceholder.get(changeReport.getChangeId()).setDaqStatus(Status.OK);
                  }
                }
              }
            } catch (Exception e) {
              String errorMessage = "Error during DAQ reconfiguration: unsuccessful application of configuration (possible timeout) to Process " + processCache.get(processId).getName();
              log.error(errorMessage, e);
              processService.setRequiresReboot(processId, true);
              report.addProcessToReboot(processCache.get(processId).getName());
              report.addStatus(Status.FAILURE);
              report.setStatusDescription(report.getStatusDescription() + errorMessage + "\n");
            }
          } else {
            processService.setRequiresReboot(processId, true);
            report.addProcessToReboot(processCache.get(processId).getName());
            report.addStatus(Status.RESTART);
          }
          if (configProgressMonitor != null) {
            configProgressMonitor.incrementDaqProgress();
          }
        } else {
          log.info("Interrupting configuration " + configId + " due to cancel request.");
        }
      }
    } else {
      log.debug("DAQ runtime reconfiguration not enabled - setting required restart flags");
      if (!processLists.isEmpty()){
        report.addStatus(Status.RESTART);
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
        element.getAction().equals(Action.REMOVE));
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
    if (!cancelRequested) {
      if (element.getEntity().equals(ConfigConstants.Entity.MISSING)) {
        ConfigurationElementReport elementReport = new ConfigurationElementReport(element.getAction(), element.getEntity(), element.getEntityId());
        elementReport.setWarning("Entity " + element.getEntityId() + " does not exist");
        report.addElementReport(elementReport);
        report.setStatusDescription("Please check subreport description for details");
        report.addStatus(Status.WARNING);
      } else {
          //initialize success report
          ConfigurationElementReport elementReport = new ConfigurationElementReport(element.getAction(),
                  element.getEntity(),
                  element.getEntityId());
          report.addElementReport(elementReport);
          List<ProcessChange> processChanges;
          try {
            processChanges = applyConfigElement(element, elementReport);  //never returns null

            if (processChanges != null) {


              for (ProcessChange processChange : processChanges) {

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
                  element.setDaqStatus(Status.RESTART); //default to restart; if successful on DAQ layer switch to OK
                } else if (processChange.requiresReboot()) {
                  if (log.isDebugEnabled()) {
                    log.debug(configId + " RESTART for " + processChange.getProcessId() + " required");
                  }
                  element.setDaqStatus(Status.RESTART);
                  report.addStatus(Status.RESTART);
                  report.addProcessToReboot(processCache.get(processId).getName());
                  element.setStatus(Status.RESTART);
                  processService.setRequiresReboot(processId, Boolean.TRUE);
                }
              }
            }
          } catch (Exception ex) {
            String errMessage = configId + " Exception caught while applying the configuration change (Action, Entity, " +
                    "Entity id) = (" + element.getAction() + "; " + element.getEntity() + "; " + element.getEntityId() + ")";
            log.error(errMessage, ex.getMessage());
            elementReport.setFailure("Exception caught while applying the configuration change.", ex);
            element.setStatus(Status.FAILURE);
            report.addStatus(Status.FAILURE);
            report.setStatusDescription("Failure: see details below.");
          }
      }
      if (configProgressMonitor != null){
        configProgressMonitor.incrementServerProgress(element.buildDescription());
      }
    } else {
      log.info(configId + " Interrupting configuration due to cancel request.");
    }
  }


  /**
   * Applies a single configuration element. On the DB level, this action should
   * either be entirely applied or the transaction rolled back. In the case of
   * a rollback, the cache should also reflect this rollback (emptied and reloaded
   * for instance).
   *
   * @param element the details of the configuration action
   * @param elementReport report that should be set to failed if there is a problem
   * @return list of DAQ configuration events; is never null but may be empty
   * @throws IllegalAccessException
   **/
  private List<ProcessChange> applyConfigElement(final ConfigurationElement element,
                                                 final ConfigurationElementReport elementReport) throws IllegalAccessException {

    //initialize the DAQ config event
    final List<ProcessChange> daqConfigEvents = new ArrayList<>();
      if (log.isTraceEnabled()) {
        log.trace(element.getConfigId() + " Applying configuration element with sequence id " + element.getSequenceId());
      }

      if (element.getAction() == null || element.getEntity() == null || element.getEntityId() == null) {
        elementReport.setFailure("Parameter missing in configuration line with sequence id " + element.getSequenceId());
        return null;
      }
      switch (element.getAction()) {
        case CREATE :
          KotlinAPIs.applyNotNull(
            configHandlerActor.doWithHandler(element.getEntity(), handler -> handler.create(element)),
            daqConfigEvents::addAll
          );
          if (element.getEntity() == ConfigConstants.Entity.PROCESS) {
            element.setDaqStatus(Status.RESTART);
          }
  //        default : elementReport.setFailure("Unrecognized reconfiguration entity: " + element.getEntity());
  //          log.warn("Unrecognized reconfiguration entity: {} - see reconfiguration report for details.", element.getEntity());
  //        }
          break;
        case UPDATE :
          KotlinAPIs.applyNotNull(
            configHandlerActor.doWithHandler(
              element.getEntity(),
              handler -> handler.update(element.getEntityId(), element.getElementProperties())
            ),
            daqConfigEvents::addAll
          );
          break;
        case REMOVE :
          KotlinAPIs.applyNotNull(
            configHandlerActor.doWithHandler(
              element.getEntity(),
              handler -> handler.remove(element.getEntityId(), elementReport)
            ),
            daqConfigEvents::addAll
          );
          break;
        default : elementReport.setFailure("Unrecognized reconfiguration action: " + element.getAction());
      log.warn("Unrecognized reconfiguration action: {} - see reconfiguration report for details.", element.getAction());
      }

      //set *unique* change id (single element may trigger many changes e.g. rule removal)
      if (!daqConfigEvents.isEmpty()) {
        for (ProcessChange processChange : daqConfigEvents) {
          if (processChange.processActionRequired()) {
            processChange.getChangeEvent().setChangeId(changeId.getAndIncrement());
          }
        }
      }

    return daqConfigEvents;
  }


  /**
   * Save the report to disk.
   * @param configId id of the config
   * @param xmlReport the XML report in String format
   */
  private void archiveReport(String configId, String xmlReport) {
    new File(reportDirectory).mkdirs();

    try {
      File outFile = new File(reportDirectory, "report_" + configId + "_" + System.currentTimeMillis() + ".xml");
      FileWriter fileWriter;
      fileWriter = new FileWriter(outFile);
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write(xmlReport);
      bufferedWriter.close();
    } catch (Exception e) {
      log.error("Exception caught while writing configuration report to directory: {}", reportDirectory, e);
    }
  }

  /**
   * @param daqConfigEnabled the daqConfigEnabled to set
   */
  public void setDaqConfigEnabled(final boolean daqConfigEnabled) {
    this.daqConfigEnabled = daqConfigEnabled;
  }

  /**
   * Set the (absolute) directory where the config reports should be saved.
   * @param reportDirectory report directory
   */
  public void setReportDirectory(final String reportDirectory) {
    this.reportDirectory = reportDirectory;
  }

  @Override
  public void cancelCurrentConfiguration() {
    cancelRequested = true;
  }

  @Override
  public ConfigurationReport applyConfiguration(int configId) {
    return applyConfiguration(configId, null);
  }

  @Override
  public List<ConfigurationReportHeader> getConfigurationReports() {
    List<ConfigurationReportHeader> reports = new ArrayList<>();

    // Read all report files and deserialise them
    try {
      ArrayList<File> files = new ArrayList<>(Arrays.asList(new File(reportDirectory).listFiles(new ConfigurationReportFileFilter())));
      Serializer serializer = getSerializer();

      for (File file : files) {
        ConfigurationReportHeader report = serializer.read(ConfigurationReportHeader.class, file);
        log.debug("Deserialised configuration report {}", report.getId());
        reports.add(report);
      }

    } catch (Exception e) {
      log.error("Error deserialising configuration report", e);
    }

    return reports;
  }

  @Override
  public List<ConfigurationReport> getConfigurationReports(String id) {
    List<ConfigurationReport> reports = new ArrayList<>();

    try {
      ArrayList<File> files = new ArrayList<>(Arrays.asList(new File(reportDirectory).listFiles(new ConfigurationReportFileFilter(id))));
      Serializer serializer = getSerializer();

      for (File file : files) {
        ConfigurationReport report = serializer.read(ConfigurationReport.class, file);
        log.debug("Deserialised configuration report {}", report.getId());
        reports.add(report);
      }

    } catch (Exception e) {
      log.error("Error deserialising configuration report", e);
    }

    return reports;
  }

  /**
   * Retrieve a {@link Serializer} instance suitable for deserialising a
   * {@link ConfigurationReport}.
   *
   * @return a new {@link Serializer} instance
   */
  private Serializer getSerializer() {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    RegistryMatcher matcher = new RegistryMatcher();
    matcher.bind(Timestamp.class, new DateFormatConverter(format));
    Strategy strategy = new AnnotationStrategy();
    Serializer serializer = new Persister(strategy, matcher);
    return serializer;
  }
}
