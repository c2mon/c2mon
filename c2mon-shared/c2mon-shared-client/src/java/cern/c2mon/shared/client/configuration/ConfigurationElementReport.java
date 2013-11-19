/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.client.configuration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import cern.c2mon.shared.client.configuration.ConfigConstants.Status;

/**
 * A report with details of the success/failure of applying
 * a single {@link ConfigurationElement} or changes triggered
 * by a parent ConfigurationElement. 
 * 
 * @author Mark Brightwell
 *
 */
public class ConfigurationElementReport {

  /**
   * The action of the {@link ConfigurationElement}.
   */
  private ConfigConstants.Action action = null;
  
  /**
   * The entity reconfigured.
   */
  private ConfigConstants.Entity entity = null;
  
  /**
   * The id of the entity being reconfigured.
   */
  private Long id = null;
  
  /**
   * The status set during reconfiguration (success, failure, ...) 
   */
  private ConfigConstants.Status status = null;

  /**
   * Additional status textual information.
   */
  private String statusMessage = null;
  
  /**
   * A list of subreports if this {@link ConfigurationElement}
   * triggered other changes (not associated to a ConfigurationElement
   * themselves).
   */
  private ArrayList<ConfigurationElementReport> subreports = new ArrayList<ConfigurationElementReport>();

 
  /**
   * Constructor for a ConfigurationReport representing a successful operation.
   * @param pAction ConfigurationAction.CREATE, ConfigurationAction.REMOVE or ConfigurationAction.UPDATE
   * @param pEntity ConfigurationEntity.DATATAG, ConfigurationEntity.COMMANDTAG etc.
   * @param pId     unique identifier of the entity to be configured (e.g. tag id)
   */
  public ConfigurationElementReport(final ConfigConstants.Action pAction, final ConfigConstants.Entity pEntity, final Long pId) {
    this(pAction, pEntity, pId, ConfigConstants.Status.OK, null);
  }
  
  /**
   * Constructor for a ConfigurationReport representing a FAILED operation.
   * @param pAction ConfigurationAction.CREATE, ConfigurationAction.REMOVE or ConfigurationAction.UPDATE
   * @param pEntity ConfigurationEntity.DATATAG, ConfigurationEntity.COMMANDTAG etc.
   * @param pId     unique identifier of the entity to be configured (e.g. tag id)
   * @param pStatus result of the ConfigurationAction
   * @param pStatusMessage free-text message indicating WHY the configuration operation failed
   */
  public ConfigurationElementReport(final ConfigConstants.Action pAction, final ConfigConstants.Entity pEntity, final Long pId, final ConfigConstants.Status pStatus, final String pStatusMessage) {
    this.action = pAction;
    this.entity = pEntity;
    this.id = pId;
    this.status = pStatus;
    this.statusMessage = pStatusMessage;
  }
  
  /**
   * Check whether the configuration operation was successful or not.
   * @return true if the configuration operation was successful.
   */
  public boolean isSuccess() {
    return this.status != null && this.status.equals(ConfigConstants.Status.OK);
  }
  
  /**
   * Returns true if the status of the report is RESTART.
   * @return true if reboot required
   */
  public boolean requiresReboot() {
    return this.status != null && this.status.equals(ConfigConstants.Status.RESTART);
  }
  
  /**
   * Returns true if an error occurred during the configuration
   * (should not be used if the system recognises that a DAQ
   * restart is required; use the status RESTART instead).
   * @return true if an error occurred
   */
  public boolean isFailure() {
    return this.status != null && this.status.equals(ConfigConstants.Status.FAILURE);
  }
  
  /**
   * Get a message describing why the configuration operation failed.
   * This method will return null if the configuration exception was successful.
   * @return a message describing why the configuration operation failed.
   */
  public String getStatusMessage() {
    return this.statusMessage;
  }
  
 /**
  * XML representation of the report, used for sending and displaying
  * in a browser.
  * @return the XML as String
  */
  public String toXML() {
    StringBuffer str = new StringBuffer(50);
    str.append("  <ConfigurationElementReport>\n");

    str.append("    <action>");
    str.append(this.action);
    str.append("</action>\n");

    str.append("    <entity>");
    str.append(this.entity);
    str.append("</entity>\n");
    
    str.append("    <id>");
    str.append(this.id);
    str.append("</id>\n");

    str.append("    <status>");
    str.append(this.status);
    str.append("</status>\n");
    
    if (this.statusMessage != null) {
      str.append("    <status-message><![CDATA[");
      str.append(this.statusMessage);
      str.append("]]></status-message>\n");
    }
    
    int numSubReports = this.subreports.size();
    if (numSubReports > 0) {
      str.append("<sub-reports>\n");
      for (int i = 0; i != numSubReports; i++) {
        str.append(((ConfigurationElementReport) this.subreports.get(i)).toXML());
      }
      str.append("</sub-reports>\n");
    }
  
    str.append("  </ConfigurationElementReport>\n");
    return str.toString();
  }
  
  /**
   * Sets a message with a warning status flag.
   * @param pWarningMessage the message
   */
  public void setWarning(final String pWarningMessage) {
    this.status = ConfigConstants.Status.WARNING;
    this.statusMessage = pWarningMessage;
  }

  /**
   * Sets a message with a failure status flag. If the message is already
   * non-null, appends the new message to the existing one.
   * @param pFailureMessage the message
   */
  public void setFailure(final String pFailureMessage) {
    this.status = ConfigConstants.Status.FAILURE;
    if (statusMessage != null) {
      statusMessage = statusMessage + "; " + pFailureMessage;
    } else {
      this.statusMessage = pFailureMessage;
    }    
  }

  /**
   * Sets a message with a failure status flag and an exception
   * trace.
   * @param pFailureMessage the message
   * @param pException the Exception
   */
  public void setFailure(final String pFailureMessage, final Exception pException) {
    this.status = ConfigConstants.Status.FAILURE;
    this.statusMessage = pFailureMessage;
    setExceptionTrace(pException);
  }

  /**
   * Adds the exception trace to the status message. 
   * @param e the Exception
   */
  public void setExceptionTrace(final Exception e) {
    if (e != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      if (this.statusMessage != null) {
        this.statusMessage = this.statusMessage + "\n" + sw.getBuffer().toString();
      }
      else {
        this.statusMessage = sw.getBuffer().toString();
      }
    }
  }

  /**
   * Calls toXML().
   * @return an XML description
   */
  public String toString() {
    return toXML();
  }
  
  /**
   * Adds a report to the list of subreports of this report and adjusts the success flag
   * and message (message is appended).
   * @param pReport the subreport
   */
  public void addSubReport(final ConfigurationElementReport pReport) {
    this.subreports.add(pReport);
    if (pReport.isFailure()) {
      setFailure(pReport.getStatusMessage());
    } else if (pReport.requiresReboot()) {
      status = Status.RESTART;
    }
  }

  /**
   * Getter method.
   * @return the action
   */
  public ConfigConstants.Action getAction() {
    return action;
  }

  /**
   * Getter method.
   * @return the entity
   */
  public ConfigConstants.Entity getEntity() {
    return entity;
  }

  /**
   * Getter method.
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * Getter method.
   * @return the subreports
   */
  public ArrayList<ConfigurationElementReport> getSubreports() {
    return subreports;
  }  
  
  /**
   * For testing only
   * @param args none needed
   */
  public static void main(final String[] args) {
    ConfigurationElementReport report = 
     new ConfigurationElementReport(
       ConfigConstants.Action.CREATE, 
       ConfigConstants.Entity.ALARM, 
       new Long(100000), ConfigConstants.Status.WARNING,
       "DOn't know why"
      );
    report.addSubReport(
      new ConfigurationElementReport(
        ConfigConstants.Action.CREATE,
        ConfigConstants.Entity.DATATAG,
        new Long(400)
      )
    );
    System.out.println(report.toXML());
  }
}
