/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.sender.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.almon.AlarmRecord;
import cern.c2mon.daq.almon.AlarmState;
import cern.c2mon.daq.almon.address.AlarmTriplet;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.almon.sender.TestAlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;

/**
 * This alarm sender implementation is used for test purposes only. It records all alarm
 * activations/terminations/updates in its internal cache, which can later be used for validation in the tests
 *
 * @author wbuczak
 */
public class DummyAlmonSenderImpl implements TestAlmonSender {

    private static final Logger LOG = LoggerFactory.getLogger(DummyAlmonSenderImpl.class);

    private Map<AlarmTriplet, List<AlarmRecord>> alarms = new ConcurrentHashMap<AlarmTriplet, List<AlarmRecord>>();

    @Override
    public synchronized List<AlarmRecord> getAlarmsSequence(AlarmTriplet alarmTriplet) {
        if (alarms.get(alarmTriplet) != null)
            return Collections.unmodifiableList(alarms.get(alarmTriplet));
        else {
            return new ArrayList<AlarmRecord>();
        }
    }

    public void activate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet,
            long userTimestamp, UserProperties userProperties) {
        LOG.info("activating alarm: {}", alarmTriplet);
        if (!alarms.containsKey(alarmTriplet)) {
            alarms.put(alarmTriplet, new ArrayList<AlarmRecord>());
        }
        alarms.get(alarmTriplet).add(new AlarmRecord(AlarmState.ACTIVE, userTimestamp, userProperties));
        if (ems != null) {
            ems.sendTagFiltered(sdt, Boolean.TRUE, System.currentTimeMillis(), userProperties.toJson());
        }
    }

    public void terminate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet,
            long userTimestamp) {
        LOG.info("terminating alarm: {}", alarmTriplet);
        if (!alarms.containsKey(alarmTriplet)) {
            alarms.put(alarmTriplet, new ArrayList<AlarmRecord>());
        }
        alarms.get(alarmTriplet).add(new AlarmRecord(AlarmState.TERMINATED, userTimestamp));
        if (ems != null) {
            ems.sendTagFiltered(sdt, Boolean.FALSE, System.currentTimeMillis());
        }
    }

    public void update(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet,
            long userTimestamp, UserProperties userProperties) {
        LOG.info("updating alarm: {}", alarmTriplet);
        if (!alarms.containsKey(alarmTriplet)) {
            LOG.warn("trying to update alarm which is not active. skipping");
        }

        List<AlarmRecord> records = alarms.get(alarmTriplet);

        AlarmRecord lastRecord = records.get(records.size() - 1);
        if (lastRecord.getAlarmState().equals(AlarmState.ACTIVE)) {
            alarms.get(alarmTriplet).add(new AlarmRecord(AlarmState.ACTIVE, userTimestamp, userProperties));
            if (ems != null) {
                ems.sendTagFiltered(sdt, true, System.currentTimeMillis(), userProperties.toJson());
            }
        } else {
            LOG.warn("trying to update alarm which is not active. skipping");
        }
    }

}