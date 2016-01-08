package cern.c2mon.shared.client.configuration.api.alarm;

import cern.c2mon.shared.common.metadata.Metadata;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
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

  /**
   * Meta data of the alarm object. Holds arbitrary data which are related to the given Alarm.
   */
  private Metadata metadata;

  // TODO check if alarm condition is mandatory
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
  public Alarm(boolean deleted, Long id, DataType valueType, Long dataTagId, String faultFamily, String faultMember, Integer faultCode,
               AlarmCondition alarmCondition, Metadata metadata) {
    super();
    this.deleted = deleted;
    this.id = id;
    this.valueType = valueType;
    this.dataTagId = dataTagId;
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
    boolean result = (getId() != null) && (getFaultMember() != null)
        && (getFaultFamily() != null) && (getFaultCode() != null) && (getValueType() != null);

    return result;
  }
}
