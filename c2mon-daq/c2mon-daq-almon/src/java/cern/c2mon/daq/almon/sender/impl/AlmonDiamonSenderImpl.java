/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.sender.impl;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.daq.almon.address.AlarmTripplet;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.almon.sender.AlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;

/**
 * @author wbuczak
 */
@ManagedResource(objectName = "cern.c2mon.daq.almon.sender:name=AlmonDiamonSender", description = "diamon alarms montor sender")
public class AlmonDiamonSenderImpl implements AlmonSender {

    private static final Logger LOG = LoggerFactory.getLogger(AlmonDiamonSenderImpl.class);

    @PostConstruct
    public void init() {
        LOG.info("Initializing alarms sender..");
        /*
         * try { manager = SourceManager.get();
         * 
         * SourceManagerConfiguration config = new SourceManagerConfiguration();
         * 
         * if (destinationRoot != null) { config.setDestRoot(destinationRoot); }
         * 
         * manager.init(config);
         * 
         * alarmSource = manager.createSource(conf.getAccelerator());
         * 
         * alarmSource.setBackupFreq(conf.getBackupPeriod()); alarmSource.setKeepAliveFreq(keepAliveFrequency);
         * alarmSource.setLatency(latency); alarmSource.setQueueSize(queueSize);
         * 
         * alarmSource.start();
         * 
         * } catch (Exception e) { LOG.error("Alarms Source Manager initialization failed!", e); }
         */

        LOG.info("alarm monitor sender's initialization done");
    }

    @Override
    public void activate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet,
            long userTimestamp, Properties userProperties) {

        try {
            ems.sendTagFiltered(sdt, "true", System.currentTimeMillis(), new UserProperties(userProperties).toJson());
        } catch (Exception ex2) {
            LOG.error("exception caught when trying to send tag update", ex2);
        }

        // AlarmHandle alarm = alarmSource.getHandle(alarmTripplet.getFaultFamily(), alarmTripplet.getFaultMember(),
        // alarmTripplet.getFaultCode());
        //
        // if (userProperties != null) {
        // for (Entry<Object, Object> e : userProperties.entrySet()) {
        // try {
        // alarm.getStatus().setProperty(e.getKey().toString(), userProperties.get(e.getKey()).toString());
        // } catch (Exception e1) {
        // LOG.warn("could not set user priperty: {} value: {} for alarm: {}", e.getKey().toString(),
        // userProperties.get(e.getKey()).toString(), alarmTripplet.toString());
        // }
        // }
        // }
        //
        // if (!alarmSource.activate(alarm)) {
        // LOG.warn("activating alarm: {} failed", alarmTripplet.toString());
        // }
    }

    @Override
    public void update(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet,
            long userTimestamp, Properties userProperties) {
        // AlarmHandle alarm = alarmSource.getHandle(alarmTripplet.getFaultFamily(), alarmTripplet.getFaultMember(),
        // alarmTripplet.getFaultCode());
        //
        // if (userProperties != null) {
        // for (Entry<Object, Object> e : userProperties.entrySet()) {
        // try {
        // alarm.getStatus().setProperty(e.getKey().toString(), userProperties.get(e.getKey()).toString());
        // } catch (Exception e1) {
        // LOG.warn("could not set user priperty: {} value: {} for alarm: {}", e.getKey().toString(),
        // userProperties.get(e.getKey()).toString(), alarmTripplet.toString());
        // }
        // }
        // }
        //
        // if (!alarmSource.update(alarm)) {
        // LOG.warn("updating alarm: {} failed", alarmTripplet.toString());
        // }
    }

    @Override
    public void terminate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet,
            long userTimestamp) {
        // AlarmHandle alarm = alarmSource.getHandle(alarmTripplet.getFaultFamily(), alarmTripplet.getFaultMember(),
        // alarmTripplet.getFaultCode());
        //
        // if (!alarmSource.terminate(alarm)) {
        // LOG.warn("terminating alarm: {} failed", alarmTripplet.toString());
        // }
    }

}