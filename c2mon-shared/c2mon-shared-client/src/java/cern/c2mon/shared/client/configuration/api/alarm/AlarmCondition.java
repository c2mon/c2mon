package cern.c2mon.shared.client.configuration.api.alarm;

import cern.c2mon.shared.client.configuration.api.util.DataType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Alarm condition to a {@link Alarm}
 *
 * All new conditions which are added to C2MON must also be added here
 */
@Data
@AllArgsConstructor
public abstract class AlarmCondition {

  /**
   * Datatype of the alarm condition
   */
  private DataType dataType;
  
  abstract public String getXMLCondition();

}
