/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.sender.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.daq.almon.address.AlarmTriplet;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.almon.sender.AlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;

/**
 * The alarm monitor sender proxy keeps a list of alarm monitor senders which are called to forward alarms. The proxy
 * allows injecting more than one sender and thus forwarding alarms activations/terminations/updates to multiple
 * destinations. In practice on production system we use one LASER sender + one logging sender (which logs every alarm
 * activation/termination/update record into a dedicated LOG file). Operations have synchronized critical blocks
 * disallowing different threads to activate/terminate the same alarm triplet concurrently. Proxy also caches active
 * alarms list. It will discard calls to underlying senders when attempting to activate a triplet which is already
 * active. Similarly, it will not forward the call to terminate an alarm if it has already been terminated. In addition
 * the proxy sender exposes a set of monitoring JMX metrics.
 * 
 * @author wbuczak
 */
@ManagedResource(objectName = "cern.c2mon.daq.almon.sender:name=AlarmMonitorServerProxy", description = "alarm montor sender proxy")
public class AlarmMonitorSenderProxy implements AlmonSender {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmMonitorSenderProxy.class);

    private List<AlmonSender> alarmSenders;

    private Map<AlarmTriplet, Properties> activeAlarms = new HashMap<AlarmTriplet, Properties>();

    @Resource(name = "alarmSenders")
    public void setAlarmSenders(List<AlmonSender> alarmSenders) {
        this.alarmSenders = alarmSenders;
    }

    @Override
    public void activate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet,
            long userTimestamp, UserProperties userProperties) {
        Properties props = userProperties == null ? new Properties() : userProperties;

        boolean activate = false;
        synchronized (activeAlarms) {
            if (!activeAlarms.containsKey(alarmTriplet)) {
                activate = true;
                activeAlarms.put(alarmTriplet, props);
            }
        }

        if (activate) {
            for (AlmonSender sender : alarmSenders) {
                try {
                    sender.activate(sdt, ems, alarmTriplet, userTimestamp, userProperties);
                } catch (Exception ex) {
                    LOG.error("exception caught while calling activate() on alarm sender: {}", sender.getClass()
                            .getName());
                    LOG.debug("exception trace", ex);
                }
            }
        }// if
    }

    @Override
    public void update(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet,
            long userTimestamp, UserProperties userProperties) {
        Properties props = userProperties == null ? new Properties() : userProperties;

        boolean update = false;
        synchronized (activeAlarms) {
            if (activeAlarms.containsKey(alarmTriplet)) {
                // update only if alarm is currently active and properties have changed
                if (!props.equals(activeAlarms.get(alarmTriplet))) {
                    update = true;
                    activeAlarms.put(alarmTriplet, props);
                }
            }
        }

        if (update) {
            for (AlmonSender sender : alarmSenders) {
                try {
                    sender.update(sdt, ems, alarmTriplet, userTimestamp, userProperties);
                } catch (Exception ex) {
                    LOG.error("exception caught while calling update() on alarm sender: {}", sender.getClass()
                            .getName());
                    LOG.debug("exception trace", ex);
                }
            }
        }// if
    }

    @Override
    public void terminate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet,
            long userTimestamp) {
        boolean terminate = false;
        synchronized (activeAlarms) {
            if (activeAlarms.containsKey(alarmTriplet)) {
                terminate = true;
                activeAlarms.remove(alarmTriplet);
            }
        }

        if (terminate) {
            for (AlmonSender sender : alarmSenders) {
                try {
                    sender.terminate(sdt, ems, alarmTriplet, userTimestamp);
                } catch (Exception ex) {
                    LOG.error("exception caught while calling terminate() on alarm sender: {}", sender.getClass()
                            .getName());
                    LOG.debug("exception trace", ex);
                }
            }
            activeAlarms.remove(alarmTriplet);
        }// if
    }

    @ManagedAttribute
    public int getNumberOfConfiguredSenders() {
        return this.alarmSenders.size();
    }

    @ManagedAttribute
    public int getActiveAlarmsCount() {
        return this.activeAlarms.size();
    }

    @ManagedAttribute
    public List<String> getActiveTriplets() {
        Set<AlarmTriplet> activeTriplets = this.activeAlarms.keySet();
        List<String> result = new ArrayList<String>();
        for (AlarmTriplet t : activeTriplets) {
            result.add(t.toString());
        }

        return result;
    }

    @ManagedOperation
    public Properties getUsersPropertiesOfActiveAlarm(String triplet) {
        return activeAlarms.get(AlarmTriplet.fromString(triplet));
    }

}