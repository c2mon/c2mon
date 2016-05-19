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

import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.common.metadata.Metadata;
import lombok.Builder;
import lombok.Data;

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
public class Alarm implements ConfigurationObject {

  @IgnoreProperty
  private boolean update;

  @IgnoreProperty
  private boolean create;

  @IgnoreProperty
  private boolean deleted;

  /**
   * The id of the overlying Tag. This field should never set by the user directly.
   */
  @IgnoreProperty
  private Long parentTagId;

  /**
   * The name of the overlying Tag. This field should never set by the user directly.
   */
  @IgnoreProperty
  private String parentTagName;

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

  /**
   * Constructor for building a Alarm with all fields.
   * To build a Alarm with arbitrary fields use the builder pattern.
   *
   * @param deleted        Determine if this object apply as deletion.
   * @param id             Unique id of the alarm.
   * @param valueType      Determine the data type of the alarm which belongs to this configuration.
   * @param dataTagId      Determine the id of the tag which this alarm is attached to.
   * @param faultFamily    LASER fault family of the alarm.
   * @param faultMember    LASER fault member of the alarm.
   * @param faultCode      LASER fault code of the alarm.
   * @param alarmCondition Determine the alarm condition of this alarm.
   * @param metadata       Arbitrary metadata attached to this alarm configuration.
   */
  @Builder
  public Alarm(boolean deleted, Long id, String faultFamily, String faultMember, Integer faultCode,
               AlarmCondition alarmCondition, Metadata metadata) {
    super();
    this.deleted = deleted;
    this.id = id;
    this.faultFamily = faultFamily;
    this.faultMember = faultMember;
    this.faultCode = faultCode;
    this.alarmCondition = alarmCondition;
    this.metadata = metadata;
  }

  public Alarm() {
  }

  @Override
  public boolean requiredFieldsGiven() {
    boolean result = (getId() != null) && (getFaultMember() != null) && (getFaultFamily() != null) && (getFaultCode() != null);

    return result;
  }

  public static CreateBuilder create(String faultFamily, String faultMember, Integer faultCode, AlarmCondition alarmCondition) {

    Alarm iniAlarm = Alarm.builder()
        .faultFamily(faultFamily)
        .faultMember(faultMember)
        .faultCode(faultCode)
        .alarmCondition(alarmCondition).build();

    return iniAlarm.toCreateBuilder(iniAlarm);
  }

  public static UpdateBuilder update(Long id) {

    Alarm iniAlarm = Alarm.builder().id(id).build();

    return iniAlarm.toUpdateBuilder(iniAlarm);
  }

  private CreateBuilder toCreateBuilder(Alarm initializationAlarm) {
    return new CreateBuilder(initializationAlarm);
  }

  private UpdateBuilder toUpdateBuilder(Alarm initializationAlarm) {
    return new Alarm.UpdateBuilder(initializationAlarm);
  }

  public static class CreateBuilder {

    private Alarm alarmBuild;

    CreateBuilder(Alarm initializationAlarm) {
      this.alarmBuild = initializationAlarm;
    }

    public Alarm.CreateBuilder id(Long id) {
      this.alarmBuild.setId(id);
      return this;
    }

    public Alarm.CreateBuilder metadata(Metadata metadata) {
      this.alarmBuild.setMetadata(metadata);
      return this;
    }

    public Alarm build() {

      alarmBuild.setCreate(true);
      return this.alarmBuild;
    }
  }

  public static class UpdateBuilder {

    private Alarm builderAlarm;

    UpdateBuilder(Alarm initializationAlarm) {
      this.builderAlarm = initializationAlarm;
    }

    public Alarm.UpdateBuilder alarmCondition(AlarmCondition alarmCondition) {
      this.builderAlarm.setAlarmCondition(alarmCondition);
      return this;
    }

    public Alarm.UpdateBuilder metadata(Metadata metadata) {
      this.builderAlarm.setMetadata(metadata);
      return this;
    }

    public Alarm build() {

      this.builderAlarm.setUpdate(true);
      return this.builderAlarm;
    }
  }
}
