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

import java.sql.Timestamp;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import cern.c2mon.shared.client.request.ClientRequestReport;

/**
 * Simple bean to represent partial information about a configuration report
 * (id, name, status etc.).
 *
 * @author Justin Lewis Salmon
 */
@Root(name = "ConfigurationReport", strict = false)
public class ConfigurationReportHeader extends ClientRequestReport implements Comparable<ConfigurationReportHeader> {

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
   * Constructor. Used when manually instantiating an instance of this class.
   *
   * @param id the configuration id
   * @param name the configuration name
   * @param user the user who ran this configuration
   * @param status the overall status of the configuration
   * @param statusDescription optional additional description
   * @param timestamp time the configuration was applied
   */
  public ConfigurationReportHeader(final Long id,
                                   final String name,
                                   final String user,
                                   final ConfigConstants.Status status,
                                   final String statusDescription,
                                   final Timestamp timestamp) {
    this.id = id;
    this.name = name;
    this.user = user;
    this.status = status;
    this.statusDescription = statusDescription;
    this.timestamp = timestamp;
  }

  /**
   * No-arg constructor (required for deserialisation).
   */
  public ConfigurationReportHeader() {
  }

  /**
   * @return the id
   */
  public long getId() {
    return id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * @return the status
   */
  public ConfigConstants.Status getStatus() {
    return status;
  }

  /**
   * @return the statusDescription
   */
  public String getStatusDescription() {
    return statusDescription;
  }

  /**
   * @return the timestamp
   */
  public Timestamp getTimestamp() {
    return timestamp;
  }

  @Override
  public int compareTo(ConfigurationReportHeader o) {
    return timestamp.compareTo(o.getTimestamp());
  };
}
