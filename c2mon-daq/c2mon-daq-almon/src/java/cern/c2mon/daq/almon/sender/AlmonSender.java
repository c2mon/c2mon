/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.sender;

import java.util.Properties;

import cern.c2mon.daq.almon.address.AlarmTripplet;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;

/**
 * This interface defines the operations all alarm senders must implement
 * 
 * @author wbuczak
 */
public interface AlmonSender {

    void activate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet, long userTimestamp,
            Properties userProperties);

    void terminate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet, long userTimestamp);

    void update(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet, long userTimestamp,
            Properties userProperties);

}