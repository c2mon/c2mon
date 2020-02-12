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

import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.configuration.ConfigProgressMonitor;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.dao.ConfigurationDAO;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.client.configuration.*;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.converter.DateFormatConverter;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.transform.RegistryMatcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 */
@Slf4j
@Named
@Singleton
public class ConfigurationLoaderImpl implements ConfigurationLoader {

  //TODO element & element report status always both need updating - redesign this part

  private final ConfigurationDAO configurationDAO;

  private final SequenceDAO sequenceDAO;

  private final ConfigurationApplier configApplier;
  /**
   * The directory name in C2MON home where the configreports will be saved.
   */
  private String reportDirectory;

  /**
   * singelton helper-object for parsing POJO Configuration objects into ConfigurationElements
   */
  private ConfigurationParser configParser;

  @Inject
  public ConfigurationLoaderImpl(
    ConfigurationDAO configurationDAO,
    ConfigurationParser configParser,
    SequenceDAO sequenceDAO,
    ConfigurationApplier configApplier,
    ServerProperties serverProperties) {

    this.configurationDAO = configurationDAO;
    this.configParser = configParser;
    this.sequenceDAO = sequenceDAO;
    this.configApplier = configApplier;
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

      report = configApplier.applyList(configId.intValue(), configuration.getName(), configurationElements, null, false);

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
        // Gets caught by the wrapping block
        throw e;
      }

      report = configApplier.applyList(configId, configName, configElements, configProgressMonitor, true);

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
   * Save the report to disk.
   *
   * @param configId  id of the config
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
    configApplier.daqConfigEnabled = daqConfigEnabled;
  }

  /**
   * Set the (absolute) directory where the config reports should be saved.
   *
   * @param reportDirectory report directory
   */
  public void setReportDirectory(final String reportDirectory) {
    this.reportDirectory = reportDirectory;
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
    return new Persister(strategy, matcher);
  }
}
