/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.sender.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.almon.AlarmRecord;
import cern.c2mon.daq.almon.AlarmState;
import cern.c2mon.daq.almon.address.AlarmTripplet;
import cern.c2mon.daq.almon.sender.TestAlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;

/**
 * This alarm sender implementation is used for test purposes only. It records all alarm
 * activations/terminations/updates in its internal cache, which can later be used for validation in the tests
 * 
 * @author wbuczak
 */
public class DummyAlmonSenderImpl implements TestAlmonSender {

    private static final Logger LOG = LoggerFactory.getLogger(DummyAlmonSenderImpl.class);

    private Map<AlarmTripplet, List<AlarmRecord>> alarms = new ConcurrentHashMap<AlarmTripplet, List<AlarmRecord>>();

    @Override
    public synchronized List<AlarmRecord> getAlarmsSequence(AlarmTripplet alarmTripplet) {
        if (alarms.get(alarmTripplet) != null)
            return Collections.unmodifiableList(alarms.get(alarmTripplet));
        else {
            return new ArrayList<AlarmRecord>();
        }
    }

    @Override
    public void activate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet,
            long userTimestamp, Properties userProperties) {
        LOG.info("activating alarm: {}", alarmTripplet);
        if (!alarms.containsKey(alarmTripplet)) {
            alarms.put(alarmTripplet, new ArrayList<AlarmRecord>());
        }
        alarms.get(alarmTripplet).add(new AlarmRecord(AlarmState.ACTIVE, userTimestamp, userProperties));
    }

    @Override
    public void terminate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet,
            long userTimestamp) {
        LOG.info("terminating alarm: {}", alarmTripplet);
        if (!alarms.containsKey(alarmTripplet)) {
            alarms.put(alarmTripplet, new ArrayList<AlarmRecord>());
        }
        alarms.get(alarmTripplet).add(new AlarmRecord(AlarmState.TERMINATED, userTimestamp));

    }

    @Override
    public void update(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet,
            long userTimestamp, Properties userProperties) {
        LOG.info("updating alarm: {}", alarmTripplet);
        if (!alarms.containsKey(alarmTripplet)) {
            LOG.warn("trying to update alarm which is not active. skipping");
        }

        List<AlarmRecord> records = alarms.get(alarmTripplet);

        AlarmRecord lastRecord = records.get(records.size() - 1);
        if (lastRecord.getAlarmState().equals(AlarmState.ACTIVE)) {
            alarms.get(alarmTripplet).add(new AlarmRecord(AlarmState.ACTIVE, userTimestamp, userProperties));
        } else {
            LOG.warn("trying to update alarm which is not active. skipping");
        }
    }

}