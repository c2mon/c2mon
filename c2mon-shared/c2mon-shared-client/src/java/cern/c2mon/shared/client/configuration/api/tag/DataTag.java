package cern.c2mon.shared.client.configuration.api.tag;

import java.util.List;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.alarm.AlarmCondition;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import lombok.*;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 * @author Franz Ritter
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DataTag<T extends Number> extends Tag {

  /**
   * Minimum value for range checks. If the system receives a tag value that is
   * less than the authorized minimum value, it will flag the new tag value as
   * invalid.
   */
  private T minValue = null;

  /**
   * Maximum value for range checks. If the system receives a tag value that is
   * less than the authorized minimum value, it will flag the new tag value as
   * invalid.
   */
  private T maxValue = null;

  /**
   * DIP address for tags published on DIP
   */
  private String dipAddress;

  /**
   * JAPC address for tags published on JAPC
   */
  private String japcAddress;

  /**
   * Address configuration of the datatag (if any)
   */
  // TODO: check if update is possible
  @Setter(AccessLevel.NONE)
  private String address;

  public void setAddress(DataTagAddress privateAlarmCondition) {
    address = privateAlarmCondition.toConfigXML();
  }

  /**
   * Unit of the tag's value. This parameter is defined at configuration time
   * and doesn't change during run-time. It is mainly used for analogue values
   * that may represent e.g. a flow in "m3", a voltage in "kV" etc.
   */
  private String unit;

  /**
   * Expected data type for the tag's value
   */
  private DataType dataType;

  /**
   * Indicates whether this tag's value changes shall be logged to the
   * short-term log.
   */
  @DefaultValue("true")
  private Boolean isLogged = true;

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven()  && (getDataType() != null);
  }

  @Builder
  public DataTag(boolean deleted, Long id, String name, String description, DataType dataType, TagMode mode, @Singular List<Alarm> alarms,
      Boolean isLogged, String unit, T minValue, T maxValue, DataTagAddress address, String dipAddress, String japcAddress ) {
    super(deleted, id, name, description, mode, alarms);
    this.dataType = dataType;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.address = address != null ? address.toConfigXML() : null;
    this.unit = unit;
    this.dipAddress = dipAddress;
    this.japcAddress = japcAddress;
    this.isLogged = isLogged;
  }

}
