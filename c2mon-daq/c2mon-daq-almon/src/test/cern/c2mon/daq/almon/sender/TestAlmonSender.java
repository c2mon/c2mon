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

import java.util.List;

import cern.c2mon.daq.almon.AlarmRecord;
import cern.c2mon.daq.almon.address.AlarmTriplet;

/**
 * The <code>TestAlmonSender</code> is used for test purposes only. It extends the <code>AlmonSender</code> interface
 * with a possibility to access recorded alarm records
 * 
 * @author wbuczak
 */
public interface TestAlmonSender extends AlmonSender {
    List<AlarmRecord> getAlarmsSequence(AlarmTriplet alarmTriplet);
}
