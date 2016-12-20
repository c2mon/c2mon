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
package cern.c2mon.shared.client.configuration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.client.configuration.converter.ProcessListConverter;
import cern.c2mon.shared.client.request.ClientRequestReport;

/**
 * Top level report for a single C2MON (re-)configuration. Contains a list of
 * {@link ConfigurationElementReport}s, one for each
 * {@link ConfigurationElement} linked to this configuration.
 *
 * @author Mark Brightwell
 *
 */
@Root(name = "ConfigurationReport")
public class ConfigurationReport extends ClientRequestReport implements Comparable<ConfigurationReport> {

  /**
   * The configuration id.
   */
  @Element
  private long id = -1;

  /**
   * The configuration name.
   */
  @Element
  private String name = null;

  /**
   * The user who ran this configuration.
   */
  @Element(required = false)
  private String user = null;

  /**
   * The overall status of the configuration after attempting to apply it (is a
   * success only if all elements were successfully applied.
   */
  @Element
  private ConfigConstants.Status status = null;

  /**
   * Optional additional description.
   */
  @Element(name = "status-description")
  private String statusDescription = null;

  /**
   * Time the configuration was applied.
   */
  @Element
  private Timestamp timestamp = null;

  /**
   * List of subreports for each element.
   */
  @ElementList(name = "ConfigurationElementReports")
  private List<ConfigurationElementReport> elementReports = new CopyOnWriteArrayList<>();

  /**
   * A list of the DAQs that need restarting for the configuration to take
   * effect (a single configuration can apply changes across many DAQs).
   */
  @Element(name = "daq-reboot")
  @Convert(ProcessListConverter.class)
  private Set<String> processesToReboot = new HashSet<>();

  /**
   * Default constructor. Only for Json deserialization.
   */
  public ConfigurationReport() {
    super();
  }

  /**
   * Constructor.
   *
   * @param pTotalOperations How many operations to expect for this progress
   *          report.
   * @param pCurrentOperation The current operation.
   * @param pTotalProgressParts How many parts to expect for this progress
   *          report.
   * @param pCurrentProgressPart The current progress
   * @param pDescriptionMessage a description of what is happening
   */
  public ConfigurationReport(final int pTotalOperations, final int pCurrentOperation, final int pTotalProgressParts, final int pCurrentProgressPart,
      final String pDescriptionMessage) {

    super(pTotalOperations, pCurrentOperation, pTotalProgressParts, pCurrentProgressPart, pDescriptionMessage);
  }

  /**
   * Constructor. Needs specifying whether the request executed successfully or
   * not.
   *
   * @param pExecutedSuccessfully True if the client request was executed
   *          successfully, false otherwise.
   * @param pErrorMessage Describes the error that occured in the server side.
   *          In case the execution was successfull, the error message can be
   *          left null.
   * @see RequestExecutionStatus
   */
  public ConfigurationReport(final boolean pExecutedSuccessfully, final String pErrorMessage) {

    super(pExecutedSuccessfully, pErrorMessage);
  }

  /**
   * Constructor.
   *
   * @param pId id
   * @param pName name
   * @param pUser user
   * @param pConfigurationStatus status
   * @param pStatusDescription text description
   */
  public ConfigurationReport(final long pId, final String pName, final String pUser, final ConfigConstants.Status pConfigurationStatus,
      final String pStatusDescription) {

    super();

    this.id = pId;
    this.status = pConfigurationStatus;
    this.statusDescription = pStatusDescription;
    this.timestamp = new Timestamp(System.currentTimeMillis());

    if (pName != null) {
      this.name = pName;
    }
    else {
      this.name = "Configuration " + pId;
    }

    if (pUser != null) {
      this.user = pUser;
    }
    else {
      this.user = "UNKNOWN";
    }
  }

  /**
   * Constructor creating successful successful report.
   *
   * @param pId id
   * @param pName name
   * @param pUser user
   */
  public ConfigurationReport(final long pId, final String pName, final String pUser) {
    this(pId, pName, pUser, ConfigConstants.Status.OK, "Configuration applied successfully");
  }

  /**
   * Getter.
   *
   * @return the configuration id
   */
  public long getId() {
    return this.id;
  }

  /**
   * Getter.
   *
   * @return the configuration name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Getter.
   *
   * @return the user who applied the configuration
   */
  public String getUser() {
    return this.user;
  }

  /**
   * Getter.
   *
   * @return the status
   */
  public ConfigConstants.Status getStatus() {
    return this.status;
  }

  /**
   * Setter method.
   *
   * @param pStatus the status
   */
  public void setStatus(final ConfigConstants.Status pStatus) {
    this.status = pStatus;
  }

  /**
   * Adjusts the top-level status of the report according to the passed status.
   * If the passed status is more severe than the current one, the top-level
   * status will be overwritten (e.g. failure overrides restart).
   *
   * @param status
   */
  public void addStatus(final ConfigConstants.Status pStatus) {
    if (pStatus.severity > status.severity) {
      status = pStatus;
    }
  }

  /**
   * Getter.
   *
   * @return the status description
   */
  public String getStatusDescription() {
    return this.statusDescription;
  }

  /**
   * Setter.
   *
   * @param pStatusDescription the status description
   */
  public void setStatusDescription(final String pStatusDescription) {
    this.statusDescription = pStatusDescription;
  }

  /**
   * @return the timestamp
   */
  public Timestamp getTimestamp() {
    return timestamp;
  }

  /**
   * Adds the Exception trace to the description.
   *
   * @param e the Exception
   */
  public void setExceptionTrace(final Exception e) {
    if (e != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      if (this.statusDescription != null) {
        this.statusDescription = this.statusDescription + "\n" + sw.getBuffer().toString();
      }
      else {
        this.statusDescription = sw.getBuffer().toString();
      }
    }
  }

  /**
   * Adds the element report to the global report and adjusts the overall
   * success flag.
   *
   * @param pReport the report to add
   */
  public void addElementReport(final ConfigurationElementReport pReport) {
    if (pReport != null) {
      this.elementReports.add(pReport);
    }
    // adjust success (only set to restart if not already set to failure
    if (pReport.isFailure()) {
      this.status = Status.FAILURE;
      this.statusDescription = "Configuration failed: see detailed report below.";
    }
    else if (this.status != Status.FAILURE && pReport.requiresReboot()) {
      this.status = Status.RESTART;
      this.statusDescription = "Some DAQs need restarting for the configuration to take full effect: see details below.";
    }
  }

  /**
   * Adds this process to those the list of those that should be rebooted
   * (manually).
   *
   * @param processName the name of the process
   */
  public void addProcessToReboot(final String processName) {
    processesToReboot.add(processName);
  }

  /**
   * Getter.
   *
   * @return a list of element reports
   */
  public List<ConfigurationElementReport> getElementReports() {
    return this.elementReports;
  }

  public Set<String> getProcessesToReboot() {
    return processesToReboot;
  }

  /**
   * Encodes the report in XML, ready for sending and displaying in a web
   * browser on the client application (use provided xsl).
   *
   * @return the XML as String
   */
  public String toXML() {
    if (isResult()) {
      StringBuffer str = new StringBuffer(1000);
      str.append("<ConfigurationReport>\n");

      str.append("<id>");
      str.append(this.id);
      str.append("</id>\n");

      str.append("<name>");
      str.append(this.name);
      str.append("</name>\n");

      str.append("<user>");
      str.append(this.user);
      str.append("</user>\n");

      str.append("<status>");
      str.append(this.status);
      str.append("</status>\n");

      str.append("<status-description><![CDATA[");
      str.append(this.statusDescription);
      str.append("]]></status-description>\n");

      str.append("<timestamp>");
      str.append(this.timestamp);
      str.append("</timestamp>\n");

      str.append("<daq-reboot>");
      str.append(this.processesToReboot);
      str.append("</daq-reboot>\n");

      str.append("<ConfigurationElementReports>\n");
      Iterator<ConfigurationElementReport> it = this.elementReports.iterator();
      while (it.hasNext()) {
        str.append(it.next().toXML());
      }
      str.append("</ConfigurationElementReports>\n");

      str.append("</ConfigurationReport>\n");
      return str.toString();
    }
    else {
      throw new UnsupportedOperationException("No XML report can be generated: this message is an intermediate response!");
    }
  }

  @Override
  public int compareTo(ConfigurationReport o) {
    return timestamp.compareTo(o.getTimestamp());
  }

  /**
   * This method will recursively analyse the attached
   * {@link ConfigurationElementReport} list and will remove
   * all nested sub reports that equal to their parent. This can
   * occur when receiving a DAQ response back.
   */
  public void normalize() {
    for (ConfigurationElementReport report : elementReports) {
      normalizeReport(report);
    }
  }

  /**
   * Recursive method to remove double reports.
   * @param parentReport The parent report
   */
  private void normalizeReport(ConfigurationElementReport parentReport) {
    int subReportSize = parentReport.getSubreports().size();

    if (subReportSize > 1) {
      for (ConfigurationElementReport subReport : parentReport.getSubreports()) {
        normalizeReport(subReport);
      }
    }
    else if (subReportSize == 1) {
      ConfigurationElementReport subReport = parentReport.getSubreports().get(0);

      if (   parentReport.getAction().equals(subReport.getAction())
          && parentReport.getEntity().equals(subReport.getEntity())
          && parentReport.getId().equals(subReport.getId())
          && parentReport.getStatus().equals(subReport.getStatus())) {

          parentReport.setStatusMessage(subReport.getStatusMessage());
          parentReport.getSubreports().clear();
      }

      subReport = null;
    }
  }

  /**
   * For testing.
   *
   * @param args not used
   */
  public static void main(String[] args) {
    ConfigurationReport report = new ConfigurationReport(12, "my.name", "any_user");
    try {
      for (int i = 0; i != 15; i++) {
        if (i % 3 == 0) {
          report.addElementReport(new ConfigurationElementReport(ConfigConstants.Action.CREATE, ConfigConstants.Entity.ALARM, new Long(i)));
        }
        else if (i % 3 == 1) {
          report.addElementReport(new ConfigurationElementReport(ConfigConstants.Action.CREATE, ConfigConstants.Entity.ALARM, new Long(i),
              ConfigConstants.Status.WARNING, "Just a little warning"));
        }
        else {
          report.addElementReport(new ConfigurationElementReport(ConfigConstants.Action.CREATE, ConfigConstants.Entity.ALARM, new Long(i),
              ConfigConstants.Status.FAILURE, "That sucks"));
        }
      }
      throw new IllegalArgumentException("nonsense argument in configuration");
    }
    catch (Exception e) {
      report.setExceptionTrace(e);
    }
    System.out.println(report.getStatusDescription());
    System.out.println(report.toXML());
  }
}
