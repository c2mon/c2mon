package cern.c2mon.shared.client.configuration.api.tag;

import java.util.List;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.metaData.MetaData;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

/**
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

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven() && (getDataType() != null);
  }

  @Builder
  public RuleTag(boolean deleted, Long id, String name, String description, DataType dataType, TagMode mode, @Singular List<Alarm> alarms, Boolean isLogged, String ruleText, String dipAddress, String japcAddress, MetaData metaData) {
    super(deleted, id, name, description, mode, alarms, metaData);
    this.dataType = dataType;
    this.ruleText = ruleText;
    this.dipAddress = dipAddress;
    this.japcAddress = japcAddress;
    this.isLogged = isLogged;
  }

  /**
   * empty default constructor
   */
  public RuleTag(){

  }

}
