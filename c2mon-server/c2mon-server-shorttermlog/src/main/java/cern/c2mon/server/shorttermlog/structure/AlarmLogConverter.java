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

package cern.c2mon.server.shorttermlog.structure;

import cern.c2mon.server.common.alarm.Alarm;

/**
 * This class is in charge of all objects transformations that may involved the
 * DataTagShortTermLog class It is aware of the DataShortTermLog java bean
 * structure and knows how its information has to be transfered into/from other
 * objects
 * 
 * @author Felix Ehm
 * 
 */
public final class AlarmLogConverter implements LoggerConverter<Alarm> {
    
    @Override
    public Loggable convertToLogged(Alarm alarm) {
      AlarmLog alarmLog = new AlarmLog();
      
      alarmLog.setTagId(alarm.getTagId());
      alarmLog.setAlarmId(alarm.getId());
      
      alarmLog.setActive(alarm.isActive());
      
      alarmLog.setFaultFamily(alarm.getFaultFamily());
      alarmLog.setFaultMember(alarm.getFaultMember());
      alarmLog.setFaultCode(alarm.getFaultCode());
      
      alarmLog.setServerTimestamp(alarm.getTimestamp());
      
      alarmLog.setInfo(alarm.getInfo());
      return alarmLog;
      
    }

}
