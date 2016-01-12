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

package cern.c2mon.daq.almon.sender.impl;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.daq.almon.address.AlarmTriplet;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.almon.sender.AlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;

/**
 * @author wbuczak
 */
@ManagedResource(objectName = "cern.c2mon.daq.almon.sender:name=AlmonDiamonSender", 
    description = "diamon alarms montor sender")
public class AlmonDiamonSenderImpl implements AlmonSender {

    private static final Logger LOG = LoggerFactory.getLogger(AlmonDiamonSenderImpl.class);

    @PostConstruct
    public void init() {
        LOG.info("Initializing alarms sender..");

        LOG.info("alarm monitor sender's initialization done");
    }

    @Override
    public void activate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet,
            long userTimestamp, UserProperties userProperties) {

        try {
            ems.sendTagFiltered(sdt, Boolean.TRUE, System.currentTimeMillis(), userProperties.toJson());
        } catch (Exception ex2) {
            LOG.error("exception caught when trying to send tag update", ex2);
        }

    }

    @Override
    public void update(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet,
            long userTimestamp, UserProperties userProperties) {

        // in fact we're just re-sending the same value (true) - only user properties may have changed..
        this.activate(sdt, ems, alarmTriplet, userTimestamp, userProperties);
    }

    @Override
    public void terminate(ISourceDataTag sdt, IEquipmentMessageSender ems, AlarmTriplet alarmTriplet,
            long userTimestamp) {
        try {
            ems.sendTagFiltered(sdt, Boolean.FALSE, System.currentTimeMillis());
        } catch (Exception ex2) {
            LOG.error("exception caught when trying to send tag update", ex2);
        }
    }

}
