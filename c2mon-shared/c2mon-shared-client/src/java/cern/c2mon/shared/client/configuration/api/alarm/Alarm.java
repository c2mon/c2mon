package cern.c2mon.shared.client.configuration.api.alarm;

import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

/**
 * Alarm which holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to Alarms.
 *
 * The class is a lombok class which uses the Builder annotation.
 *
 * @author Franz Ritter
 */
@Data
public class Alarm implements ConfigurationObject {

  @IgnoreProperty
  private boolean deleted;

  /**
   * Internal identifier of the AlarmCacheObject.
   */
  @IgnoreProperty
  private Long id;

  private DataType valueType;

  /**
   * Unique identifier of the DataTagCacheObject to which the alarm is attached.
   * The Alarm is activated or terminated depending on the current value of this
   * data tag.
   * TODO set the id depending of the Object which holds the Alarm...
   */
  private Long dataTagId;

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


  // TODO check if alarm condition is mandatory
  @Setter(AccessLevel.NONE)
  private String alarmCondition;

  public void setAlarmCondition(AlarmCondition privateAlarmCondition) {
    alarmCondition = privateAlarmCondition.getXMLCondition();
  }

  @Override
  public boolean requiredFieldsGiven() {
    boolean result = (getId() != null) && (getFaultMember() != null)
        && (getFaultFamily() != null) && (getFaultCode() != null) && (getValueType() != null);

    return result;
  }

  @Builder
  public Alarm(boolean deleted, Long id, DataType valueType, Long dataTagId, String faultFamily, String faultMember, Integer faultCode,
               AlarmCondition alarmCondition) {
    super();
    this.deleted = deleted;
    this.id = id;
    this.valueType = valueType;
    this.dataTagId = dataTagId;
    this.faultFamily = faultFamily;
    this.faultMember = faultMember;
    this.faultCode = faultCode;
    this.alarmCondition = alarmCondition != null ? alarmCondition.getXMLCondition() : null;
  }

}
