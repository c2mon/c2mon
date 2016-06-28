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
package cern.c2mon.shared.client.configuration.api.alarm;

import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.common.metadata.Metadata;
import lombok.Data;
import org.springframework.util.Assert;

/**
 * Configuration object for a Alarm.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to an Alarm.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 *
 * @author Franz Ritter
 */
@Data
public class Alarm implements ConfigurationEntity {

  @IgnoreProperty
  private boolean updated;

  @IgnoreProperty
  private boolean created;

  @IgnoreProperty
  private boolean deleted;

  private Long dataTagId;

  @IgnoreProperty
  private String dataTagName;

  /**
   * Internal identifier of the AlarmCacheObject.
   */
  @IgnoreProperty
  private Long id;

  /**
   * LASER fault family of the alarm.
   **/
  private String faultFamily;

  /**
   * LASER fault member of the alarm.
   **/
  private String faultMember;

  /**
   * LASER fault code of the alarm.
   **/
  private Integer faultCode;

  /**
   * Meta data of the alarm object. Holds arbitrary data which are related to the given Alarm.
   */
  private Metadata metadata;

  private AlarmCondition alarmCondition;


  public Alarm() {
  }

  public static CreateBuilder create(String faultFamily, String faultMember, Integer faultCode, AlarmCondition alarmCondition) {
    Assert.hasText(faultMember, "Fault member is required!");
    Assert.hasText(faultFamily, "Fault family is required!");
    Assert.notNull(faultCode, "Fault code is required!");
    Assert.notNull(alarmCondition, "Alarm condition code is required!");
    return new CreateBuilder(faultFamily, faultMember, faultCode, alarmCondition);
  }

  public static UpdateBuilder update(Long id) {
    return new UpdateBuilder(id);
  }

  public static class CreateBuilder {

    private Alarm alarmToBuild = new Alarm();

    private CreateBuilder(String faultFamily, String faultMember, Integer faultCode, AlarmCondition alarmCondition) {
      alarmToBuild.setFaultFamily(faultFamily);
      alarmToBuild.setFaultMember(faultMember);
      alarmToBuild.setFaultCode(faultCode);
      alarmToBuild.setAlarmCondition(alarmCondition);
    }

    public Alarm.CreateBuilder id(Long id) {
      this.alarmToBuild.setId(id);
      return this;
    }

    public Alarm.CreateBuilder dataTagId(Long dataTagId) {
      this.alarmToBuild.setDataTagId(dataTagId);
      return this;
    }

    public Alarm.CreateBuilder metadata(Metadata metadata) {
      this.alarmToBuild.setMetadata(metadata);
      return this;
    }

    public Alarm build() {

      alarmToBuild.setCreated(true);
      return this.alarmToBuild;
    }
  }

  public static class UpdateBuilder {

    private Alarm alarmToBuild = new Alarm();

    private UpdateBuilder(Long id) {
      this.alarmToBuild.setId(id);
    }

    public Alarm.UpdateBuilder alarmCondition(AlarmCondition alarmCondition) {
      this.alarmToBuild.setAlarmCondition(alarmCondition);
      return this;
    }

    public Alarm.UpdateBuilder metadata(Metadata metadata) {
      this.alarmToBuild.setMetadata(metadata);
      return this;
    }

    public Alarm build() {

      this.alarmToBuild.setUpdated(true);
      return this.alarmToBuild;
    }
  }
}
