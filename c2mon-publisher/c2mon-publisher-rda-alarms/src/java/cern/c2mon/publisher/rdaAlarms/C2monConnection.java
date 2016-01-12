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

package cern.c2mon.publisher.rdaAlarms;

import java.util.Collection;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

/**
 * Implementation of C2monConnectionIntf for the real C2MON connection. This is the class
 * to be used in the Spring configuration for production purpose.
 * 
 * @author mbuttner
 */
public class C2monConnection implements C2monConnectionIntf {

    private static final Logger LOG = LoggerFactory.getLogger(C2monConnection.class);
    private AlarmListener listener;
    private volatile boolean cont;
    
    //
    // --- Implements C2monConnectionInterface ----------------------------------
    //
    @Override
    public void setListener(AlarmListener listener) {
        this.listener = listener;                       
    }
    
    @Override
    public void start() throws Exception {
        cont = true;
        C2monServiceGateway.startC2monClient();
        C2monConnectionMonitor.start();
        while (!C2monServiceGateway.getSupervisionManager().isServerConnectionWorking() && cont) {
            LOG.info("Awaiting connection ...");
            Thread.sleep(1000);
        }
    }
    
    @Override
    public void connectListener() throws JMSException {
        LOG.info("Connecting alarm listener ...");
        C2monServiceGateway.getTagManager().addAlarmListener(listener);        
    }
    
    @Override
    public void stop() {
        cont = false;
        LOG.debug("Stopping the C2MON client...");
        try {
            C2monServiceGateway.getTagManager().removeAlarmListener(listener);
        } catch (JMSException e) {
            LOG.warn("?", e);
        }
        LOG.info("C2MON client stopped.");

    }

    @Override
    public Collection<AlarmValue> getActiveAlarms() {
        return C2monServiceGateway.getTagManager().getAllActiveAlarms();
    }

    @Override
    public int getQuality(long alarmTagId) {
        int qual = 0;
        ClientDataTagValue cdt = C2monServiceGateway.getTagManager().getDataTag(alarmTagId);
        if (cdt != null) {
            if (cdt.getDataTagQuality().isValid()) {
                qual = qual | Quality.VALID;
            }
            if (cdt.getDataTagQuality().isExistingTag()) {
                qual = qual | Quality.EXISTING;
            }
        }
        return qual;
    }
}
