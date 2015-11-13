package cern.c2mon.shared.client.configuration.api.alarm;

import cern.c2mon.shared.client.configuration.api.util.DataType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Value condition to a {@link Alarm}.
 *
 * Attention the getXMLCondition Method returns a hard coded xmml string.
 * The related class path in the xml String is witten in the code itself
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ValueCondition extends AlarmCondition {

  private Object value;
  
  @Builder
  public ValueCondition(DataType dataType, Object value){
    super(dataType);
    this.value = value;
  }

  @Override
  public String getXMLCondition() {
    String result = "";
    result += "<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\">\n";
    result += "<alarm-value type=\""+getDataType()+"\">"+value.toString()+"</alarm-value>\n";
    result += "</AlarmCondition>";
    return result;
  }

}
