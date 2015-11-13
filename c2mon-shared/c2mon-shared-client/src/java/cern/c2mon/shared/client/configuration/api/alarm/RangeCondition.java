package cern.c2mon.shared.client.configuration.api.alarm;

import cern.c2mon.shared.client.configuration.api.util.DataType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Range condition to a {@link Alarm}.
 *
 * Attention the getXMLCondition Method returns a hard coded xmml string.
 * The related class path in the xml String is witten in the code itself
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RangeCondition extends AlarmCondition {

  /**
   * Lower boundary of the alarm range. May be null. Please note that the
   * maxValue MUST be of the same type as the associated data tag.
   */
  private Integer minValue;
  
  /**
   * Upper boundary of the alarm range. May be null. Please note that the
   * maxValue MUST be of the same type as the associated data tag.
   */
  private Integer maxValue;
  
  @Builder
  public RangeCondition(DataType dataType, Integer minValue, Integer maxValue){
    super(dataType);
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override
  public String getXMLCondition() {
    String result = "";
    result += "<AlarmCondition class=\"cern.c2mon.server.common.alarm.RangeAlarmCondition\">\n";
    result += minValue != null ? "<min-value type=\""+getDataType()+"\">"+minValue.toString()+"</min-value>\n" : "";
    result += minValue != null ? "<max-value type=\""+getDataType()+"\">"+maxValue.toString()+"</max-value>\n" : "";
    result += "</AlarmCondition>";
    return result;
  }

}
