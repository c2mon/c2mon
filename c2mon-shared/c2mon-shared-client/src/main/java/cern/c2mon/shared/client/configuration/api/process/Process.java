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
package cern.c2mon.shared.client.configuration.api.process;

import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import lombok.Data;
import org.springframework.util.Assert;

/**
 * Configuration object for a Process.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a Process.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 *
 * @author Franz Ritter
 */
@Data
public class Process implements ConfigurationEntity {

  @IgnoreProperty
  private boolean updated = false;

  @IgnoreProperty
  private boolean created = false;

  /**
   * Determine if the instance of this class defines a DELETE command
   */
  @IgnoreProperty
  private boolean deleted = false;

  /**
   * Unique identifier of the equipment.
   */
  @IgnoreProperty
  private Long id;

  /**
   * Unique name of the equipment.
   * <p/>
   * Note: Consider that an update of the name is not provided on the server side.
   */
  private String name;

  /**
   * Interval in milliseconds at which the alive tag is expected to change.
   */
  @DefaultValue("10000")
  private Integer aliveInterval;

  /**
   * A description of the process.
   */
  @DefaultValue("<no description provided>")
  private String description;

  /**
   * Max number of updates in a single message from the DAQ process.
   */
  @DefaultValue("100")
  private Integer maxMessageSize;

  /**
   * Max delay between reception of update by a DAQ and sending it to the
   * server.
   */
  @DefaultValue("1000")
  private Integer maxMessageDelay;

  @IgnoreProperty
  private AliveTag aliveTag;

  @IgnoreProperty
  private StatusTag statusTag;

  public static CreateBuilder create(String name) {
    Assert.hasText(name, "Process name is required!");
    return new CreateBuilder(name);
  }

  public static UpdateBuilder update(Long id) {
    return new UpdateBuilder(id);
  }

  public static UpdateBuilder update(String name) {
    return new UpdateBuilder(name);
  }

  public static class CreateBuilder {

    private Process processToBuild = new Process();

    private CreateBuilder(String name) {
      processToBuild.setName(name);
      processToBuild.setCreated(true);
    }

    public Process.CreateBuilder id(Long id) {
      this.processToBuild.setId(id);
      return this;
    }

    public Process.CreateBuilder aliveTag(AliveTag aliveTag, Integer aliveInterval) {

      this.processToBuild.setAliveInterval(aliveInterval);
      this.processToBuild.setAliveTag(aliveTag);

      if (!aliveTag.isCreated()) {
        processToBuild.setCreated(false);
      }

      return this;
    }

    public Process.CreateBuilder statusTag(StatusTag statusTag) {
      this.processToBuild.setStatusTag(statusTag);

      if (!statusTag.isCreated()) {
        processToBuild.setCreated(false);
      }

      return this;
    }

    public Process.CreateBuilder description(String description) {
      this.processToBuild.setDescription(description);
      return this;
    }

    public Process.CreateBuilder maxMessageSize(Integer maxMessageSize) {
      this.processToBuild.setMaxMessageSize(maxMessageSize);
      return this;
    }

    public Process.CreateBuilder maxMessageDelay(Integer maxMessageDelay) {
      this.processToBuild.setMaxMessageDelay(maxMessageDelay);
      return this;
    }

    public Process build() {
      return this.processToBuild;
    }
  }

  public static class UpdateBuilder {

    private Process processToBuild = new Process();

    private UpdateBuilder(String name) {
      this.processToBuild.setName(name);
    }

    private UpdateBuilder(Long id) {
      this.processToBuild.setId(id);
    }

    public Process.UpdateBuilder aliveInterval(Integer aliveInterval) {
      this.processToBuild.setAliveInterval(aliveInterval);
      return this;
    }

    public Process.UpdateBuilder description(String description) {
      this.processToBuild.setDescription(description);
      return this;
    }

    public Process.UpdateBuilder maxMessageSize(Integer maxMessageSize) {
      this.processToBuild.setMaxMessageSize(maxMessageSize);
      return this;
    }

    public Process.UpdateBuilder maxMessageDelay(Integer maxMessageDelay) {
      this.processToBuild.setMaxMessageDelay(maxMessageDelay);
      return this;
    }

    public Process build() {
      processToBuild.setUpdated(true);
      return this.processToBuild;
    }
  }
}
