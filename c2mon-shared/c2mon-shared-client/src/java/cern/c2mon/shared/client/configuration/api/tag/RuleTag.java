package cern.c2mon.shared.client.configuration.api.tag;

import java.util.List;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.common.metadata.Metadata;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

/**
 * Configuration object for a RuleTag.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a RuleTag.
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
public class RuleTag extends Tag {
  /**
   * The rule as a String. Should never be null for a RuleTag (set as empty
   * String if necessary).
   */
  private String ruleText;

  /**
   * DIP address for tags published on DIP
   */
  private String dipAddress;

  /**
   * JAPC address for tags published on JAPC
   */
  private String japcAddress;

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

  /**
   * Constructor for building a RuleTag with all fields.
   * To build a RuleTag with arbitrary fields use the builder pattern.
   *
   * @param id          Unique id of the tag.
   * @param name        Unique name the tag.
   * @param description Describes the propose of the tag.
   * @param mode        define the mode in which the tag is running.
   * @param alarms      List of configuration PObjects for this tag. If the argument is null the field will be an empty List as default.
   * @param isLogged    Defines if the tag which belongs to this configuration should be logged.
   * @param metadata    Arbitrary metadata attached to his tag configuration.
   * @param deleted     Determine if this object apply as deletion.
   * @param dataType    Determine the data type of the DataTag which belongs to this configuration.
   * @param dipAddress  Defines the dipAddress of the DataTag which belongs to this configuration.
   * @param japcAddress Defines the japcAddress of the DataTag which belongs to this configuration.
   * @param ruleText    The rule which will be set to the rule through this configuration.
   */
  @Builder
  protected RuleTag(boolean deleted, Long id, String name, String description, DataType dataType, TagMode mode, @Singular List<Alarm> alarms, Boolean isLogged,
                    String ruleText, String dipAddress, String japcAddress, Metadata metadata) {
    super(deleted, id, name, description, mode, alarms, metadata);
    this.dataType = dataType;
    this.ruleText = ruleText;
    this.dipAddress = dipAddress;
    this.japcAddress = japcAddress;
    this.isLogged = isLogged;
  }

  /**
   * empty default constructor
   */
  public RuleTag() {
  }

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven() && (getDataType() != null);
  }

}
