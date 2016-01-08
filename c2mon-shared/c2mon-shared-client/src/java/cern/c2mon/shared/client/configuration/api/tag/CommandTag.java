package cern.c2mon.shared.client.configuration.api.tag;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.common.metadata.Metadata;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import lombok.*;

import java.util.List;

/**
 * Configuration object for a CommandTag.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a CommandTag.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 * The class uses the lombok builder annotation.
 * Therefore to create instances of this class you need to use the builder pattern which is provided by lombok.
 *
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

  // TODO remove CERN specific rbac fields;
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

  @Builder
  protected CommandTag(boolean deleted, Long id, String name, String description, DataType dataType, TagMode mode, @Singular List<Alarm> alarms
      , Integer clientTimeout, Integer execTimeout, Integer sourceTimeout, Integer sourceRetries, String rbacClass, String rbacDevice, String rbacProperty,
                    HardwareAddress hardwareAddress, Metadata metadata) {
    super(deleted, id, name, description, mode, alarms, metadata);
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
   * Empty default constructor
   */
  public CommandTag() {
  }

  @Override
  public boolean requiredFieldsGiven() {
    return (getId() != null) && (getName() != null) && (getDescription() != null)
        && (getClientTimeout() != null) && (getExecTimeout() != null) && (getSourceTimeout() != null)
        && (getSourceRetries() != null) && (getRbacClass() != null) && (getRbacDevice() != null) && (getRbacProperty() != null);
  }
}
