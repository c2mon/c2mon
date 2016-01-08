package cern.c2mon.shared.client.configuration.api.tag;

import java.util.List;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.common.metadata.Metadata;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import lombok.*;

/**
 * Configuration object for a CommFaultTag.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a CommFaultTag.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 *
 * @author Franz Ritter
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommFaultTag extends ControlTag {

  /**
   * Address configuration of the datatag (if any)
   */
  private DataTagAddress address;

  @DefaultValue("Boolean")
  private DataType dataType = null;

  @DefaultValue("true")
  private Boolean isLogged = true;

  /**
   * Constructor for building a CommFaultTag with all fields.
   * To build a CommFaultTag with arbitrary fields use the builder pattern.
   *
   * @param id Unique id of the tag.
   * @param name Unique name the tag.
   * @param description Describes the propose of the tag.
   * @param mode define the mode in which the tag is running.
   * @param alarms List of configuration PObjects for this tag. If the argument is null the field will be an empty List as default.
   * @param address DataTagAddress which belongs to this tag configuration.
   * @param metadata Arbitrary metadata attached to his tag configuration.
   * @param isLogged Defines if the tag which belongs to this configuration should be logged.
   * @param address DataTagAddress which belongs to this tag configuration.
   * @param metadata Arbitrary metadata attached to his tag configuration.
   */
  @Builder
  public CommFaultTag(Long id, String name, String description, TagMode mode, @Singular List<Alarm> alarms,
                      Boolean isLogged, DataTagAddress address, Metadata metadata) {
    super(id, name, description, mode, alarms, metadata);
    this.address = address;
    this.isLogged = isLogged;
  }

  /**
   * Empty default constructor
   */
  public CommFaultTag() {
  }

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven();
  }
}
