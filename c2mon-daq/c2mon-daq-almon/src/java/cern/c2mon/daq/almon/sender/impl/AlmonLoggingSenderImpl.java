/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.sender.impl;

import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.almon.address.AlarmTripplet;
import cern.c2mon.daq.almon.sender.AlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;

/**
 * This alarm sender implementation does not send alarms to LASER. It's goal is to log all alarm
 * activations/terminations/updates into a dedicated logger file
 * 
 * @author wbuczak
 */
public class AlmonLoggingSenderImpl implements AlmonSender {

    private static final Logger LOG = LoggerFactory.getLogger("almonLog");

    private static String userPropsToString(Properties userProperties) {
        StringBuilder strBuilder = new StringBuilder();

        int i = 0;
        if (userProperties != null) {
            for (Entry<Object, Object> e : userProperties.entrySet()) {
                strBuilder.append(e.getKey().toString()).append("=").append(userProperties.get(e.getKey()));
                if (i++ < userProperties.size() - 1)
                    strBuilder.append(",");
            }
        }

        return strBuilder.toString();
    }

    @Override
    public void activate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet,
            long userTimestamp, Properties userProperties) {
        LOG.info("{} {} {} ACTIVATE {}", new Object[] { sdt.getId(), userTimestamp, alarmTripplet.toString(),
                userPropsToString(userProperties) });

    }

    @Override
    public void terminate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet,
            long userTimestamp) {
        LOG.info("{} {} {} TERMINATE", new Object[] { sdt.getId(), userTimestamp, alarmTripplet.toString() });

    }

    @Override
    public void update(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTripplet alarmTripplet,
            long userTimestamp, Properties userProperties) {
        LOG.info("{} {} {} UPDATE {}", new Object[] { sdt.getId(), userTimestamp, alarmTripplet.toString(),
                userPropsToString(userProperties) });

    }

}
