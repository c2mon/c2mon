package cern.c2mon.shared.client.configuration.api.tag;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.common.metadata.Metadata;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import lombok.*;

import java.util.List;

/**
 * Configuration object for a StatusTag.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a StatusTag.
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
public class StatusTag extends ControlTag {

  @DefaultValue("String")
  private DataType dataType = null;

  @DefaultValue("true")
  private Boolean isLogged = true;

  /**
   * Constructor for building a StatusTag with all fields.
   * To build a StatusTag with arbitrary fields use the builder pattern.
   *
   * @param id          Unique id of the tag.
   * @param name        Unique name the tag.
   * @param description Describes the propose of the tag.
   * @param mode        define the mode in which the tag is running.
   * @param alarms      List of configuration PObjects for this tag. If the argument is null the field will be an empty List as default.
   * @param metadata    Arbitrary metadata attached to his tag configuration.
   * @param isLogged    Defines if the tag which belongs to this configuration should be logged.
   * @param metadata    Arbitrary metadata attached to his tag configuration.
   */
  @Builder
  public StatusTag(Long id, String name, String description, TagMode mode, @Singular List<Alarm> alarms,
                   Boolean isLogged, Metadata metadata) {

    super(id, name, description, mode, alarms, metadata);

    this.isLogged = isLogged;
  }

  public StatusTag() {
  }

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven();
  }
}
