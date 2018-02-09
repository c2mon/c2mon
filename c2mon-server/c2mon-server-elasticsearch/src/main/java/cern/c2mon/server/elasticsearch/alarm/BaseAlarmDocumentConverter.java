package cern.c2mon.server.elasticsearch.alarm;

import cern.c2mon.server.common.alarm.Alarm;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts {@link Alarm} to {@link AlarmDocument} excluding values.
 */
public class BaseAlarmDocumentConverter implements Converter<Alarm, AlarmDocument> {

  /**
   * Convert given {@link Alarm} to {@link AlarmDocument}.
   *
   * @param alarm the alarm.
   *
   * @return the alarm document.
   */
  @Override
  public AlarmDocument convert(final Alarm alarm) {
    AlarmDocument document = new AlarmDocument();

    document.put("id", alarm.getId());
    document.put("faultFamily", alarm.getFaultFamily());
    document.put("faultMember", alarm.getFaultMember());
    document.put("faultCode", alarm.getFaultCode());
    document.put("metadata", alarm.getMetadata().toMap());

    return document;
  }
}
