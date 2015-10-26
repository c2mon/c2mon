package cern.c2mon.client.ext.history.alarm;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Predicate;

import java.sql.Timestamp;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class HistoricAlarmQuery {

  BooleanBuilder builder = new BooleanBuilder();

  public HistoricAlarmQuery id(Long id) {
    builder.and(QAlarm.alarm.id.eq(id));
    return this;
  }

  public HistoricAlarmQuery tagId(Long tagId) {
    builder.and(QAlarm.alarm.tagId.eq(tagId));
    return this;
  }

  public HistoricAlarmQuery faultCode(int faultCode) {
    builder.and(QAlarm.alarm.faultCode.eq(faultCode));
    return this;
  }

  public HistoricAlarmQuery faultFamily(String faultFamily) {
    builder.and(QAlarm.alarm.faultFamily.eq(faultFamily));
    return this;
  }

  public HistoricAlarmQuery faultMember(String faultMember) {
    builder.and(QAlarm.alarm.faultMember.eq(faultMember));
    return this;
  }

  public HistoricAlarmQuery timestamp(Timestamp timestamp) {
    builder.and(QAlarm.alarm.timestamp.eq(timestamp));
    return this;
  }

  public HistoricAlarmQuery active(boolean active) {
    builder.and(QAlarm.alarm.active.eq(active));
    return this;
  }

  public HistoricAlarmQuery info(String info) {
    builder.and(QAlarm.alarm.info.eq(info));
    return this;
  }

  public HistoricAlarmQuery between(Timestamp start, Timestamp end) {
    builder.and(QAlarm.alarm.timestamp.between(start, end));
    return this;
  }

  public Predicate toPredicate() {
    return builder;
  }

}
