/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.ext.history.alarm;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Predicate;

import java.sql.Timestamp;

/**
 * This class is a simple builder for creating queries on historical alarms.
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

  public HistoricAlarmQuery operational() {
    builder.and(QAlarm.alarm.info.isNull().or(QAlarm.alarm.info.contains("[T]").not()));
    return this;
  }

  public Predicate getPredicate() {
    return builder.getValue();
  }

}
