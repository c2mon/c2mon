package cern.c2mon.shared.client.configuration.api.alarm;

import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonTypeInfo;

//import static cern.c2mon.server.configuration.parser.util.ConfigurationAllTogetherUtil.buildAllMandatory;

/**
 * Alarm condition to a {@link Alarm}
 * <p/>
 * All new conditions which are added to C2MON must also be added here
 */
@Data
@AllArgsConstructor
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class AlarmCondition {

  /**
   * Datatype of the alarm condition
   */
  private DataType dataType;

  abstract public String getXMLCondition();

  public AlarmCondition() {
  }
}
