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
package cern.c2mon.shared.client.configuration.api;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Configuration instances serves as POJO object for all configurations on the serve side of C2MON.
 * The given fields of configuration includes all information to extract a list of {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * for the ConfigurationLoader of C2MON.
 * <p/>
 * Instances of this class can be built via the provided builder pattern.
 * <p/>
 *
 * @author Justin Lewis Salmon
 * @author Franz Ritter
 */
@Data
public class Configuration {

  /**
   * Configuration name which appears in the final report.
   */
  private String name;

  /**
   * Name of the application, from where the configuration was sent.
   */
  private String application;

  /**
   * Name of the user who is sending the configuration to the server.
   */
  private String user;

  /**
   * Unique Id of the configuration which appears in the report.
   */
  private Long configurationId;

  /**
   * Contains all configuration information for Processes
   */
  @Singular
  private List<Process> processes = new ArrayList<>();

  /**
   * Contains all configuration information for RuleTags
   */
  @Singular
  private List<RuleTag> rules = new ArrayList<>();

  /**
   * Contains all configuration items (can be CREATE, UPDATE or DELETE)
   */
  @Singular
  private List<? extends ConfigurationObject> configurationItems = new ArrayList<>();

  @Builder
  public Configuration(String name, String application, String user, @Singular List<Process> processes, @Singular List<RuleTag> rules, Long confId) {
    super();
    // Default values are set here because of the lombok behavior to overwrite all instance values with defaults.
    this.name = name;
    this.application = application;
    this.user = user;
    this.processes = processes == null ? new ArrayList<Process>() : processes;
    this.rules = rules == null ? new ArrayList<RuleTag>() : rules;
    this.configurationId = confId;
    this.configurationItems = new ArrayList<>();
  }

  public Configuration() {}
}
