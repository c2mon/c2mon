package cern.c2mon.shared.client.configuration.api.tag;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.metaData.MetaData;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import lombok.*;

import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 * @author Franz Ritter
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommandTag extends Tag {
  /**
   * Client timeout of the CommandTag
   */
  private Integer clientTimeout;

  /**
   * Execution timeout of the CommandTag
   */
  private Integer execTimeout;

  /**
   * Source timeout of the CommandTag
   */
  private Integer sourceTimeout;

  /**
   * Number of times a data source should retry to execute a command in case an attempted execution fails.
   */
  private Integer sourceRetries;

  /**
   * RBAC class.
   */
  private String rbacClass;

  /**
   * RBAC device.
   */
  private String rbacDevice;

  /**
   * RBAC property.
   */
  private String rbacProperty;

  /**
   * Expected data type for the tag's value
   */
  private DataType dataType;

  /**
   * Hardware address of the CommandTag. The Hardware address is required by the data source to actually execute the
   * command.
   * Saved as String to make the property simpler
   */
  private HardwareAddress hardwareAddress;

  @Override
  public boolean requiredFieldsGiven() {
    return (getId() != null) && (getName() != null) && (getDescription() != null)
        && (getClientTimeout() != null) && (getExecTimeout() != null) && (getSourceTimeout() != null)
        && (getSourceRetries() != null) && (getRbacClass() != null) && (getRbacDevice() != null) && (getRbacProperty() != null);
  }

  @Builder
  public CommandTag(boolean deleted, Long id, String name, String description, DataType dataType, TagMode mode, @Singular List<Alarm> alarms
      , Integer clientTimeout, Integer execTimeout, Integer sourceTimeout, Integer sourceRetries, String rbacClass, String rbacDevice, String rbacProperty, HardwareAddress hardwareAddress, MetaData metaData) {
    super(deleted, id, name, description, mode, alarms, metaData);
    this.dataType = dataType;
    this.clientTimeout = clientTimeout;
    this.execTimeout = execTimeout;
    this.sourceTimeout = sourceTimeout;
    this.sourceRetries = sourceRetries;
    this.rbacClass = rbacClass;
    this.rbacDevice = rbacDevice;
    this.rbacProperty = rbacProperty;
    this.hardwareAddress = hardwareAddress;
  }

  /**
   * empty default constructor
   */
  public CommandTag(){

  }
}
