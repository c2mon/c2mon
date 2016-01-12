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

package cern.c2mon.publisher.mobicall;

import java.util.Collection;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

/**
 * Implementation of C2monConnectionIntf with the subset of really used C2MON functions. 
 * This reduction to the minimum needed stuff simplifies the mock of the C2MON connection
 * for testing purposes.
 * 
 * Actions here are:
 * - register our app as alarm listener on start, and unregister on stop
 * - reduce the data tag quality to a simple boolean (false if the underlying data tag is
 *   not valid or not existing)
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
        while (!C2monServiceGateway.getSupervisionService().isServerConnectionWorking() && cont) {
            LOG.info("Awaiting connection ...");
            Thread.sleep(1000);
        }
    }
    
    @Override
    public void connectListener() throws JMSException {
        LOG.info("Connecting alarm listener ...");
        C2monServiceGateway.getAlarmService().addAlarmListener(listener);        
    }
    
    @Override
    public void stop() {
        cont = false;
        LOG.debug("Stopping the C2MON client...");
        try {
            C2monServiceGateway.getAlarmService().removeAlarmListener(listener);
        } catch (JMSException e) {
            LOG.warn("?", e);
        }
        LOG.info("C2MON client stopped.");

    }

    @Override
    public Collection<AlarmValue> getActiveAlarms() {
        return C2monServiceGateway.getAlarmService().getAllActiveAlarms();
    }

    @Override
    public boolean isTagValid(Long tagId) {
        Tag tag = C2monServiceGateway.getTagService().get(tagId);
        return (tag.getDataTagQuality().isValid() && tag.getDataTagQuality().isExistingTag());
    }

}
