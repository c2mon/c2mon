/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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