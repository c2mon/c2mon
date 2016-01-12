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

package cern.c2mon.daq.almon.sender;

import cern.c2mon.daq.almon.address.AlarmTriplet;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;

/**
 * This interface defines a set of operations common to all alarm senders
 *
 * @author wbuczak
 */
public interface AlmonSender {

    void activate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet, long userTimestamp,
            UserProperties userProperties);

    void terminate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet, long userTimestamp);

    void update(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet, long userTimestamp,
            UserProperties userProperties);

}
