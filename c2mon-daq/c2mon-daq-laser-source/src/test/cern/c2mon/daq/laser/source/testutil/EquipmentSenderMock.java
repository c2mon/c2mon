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

package cern.c2mon.daq.laser.source.testutil;

import java.sql.Timestamp;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;

/***
 * Partial implementation of the C2MON DAQ equipment sender interface. It is used in the 
 * unit tests to collect actions triggered by our message handler. When the tests are
 * prepared, an instance of this class must be used as parameter to the connectToDataSource()
 * of the MessageHandler. Updates are than no longer transmitted to the C2MON framework,
 * but trapped here.
 * 
 * @author mbuttner
 */
public class EquipmentSenderMock implements IEquipmentMessageSender {

    private static final Logger LOG = LoggerFactory.getLogger(EquipmentSenderMock.class);
    private String name;
    private int activations;
    private int terminations;
    
    private HashMap<String, Long> lastAlarmUpdates = new HashMap<>();
    
    //
    // --- CONSTRUCTION ------------------------------------------------------------
    //
    public EquipmentSenderMock(String name) {
        this.name = name;
    }
    
    //
    // --- Implements IEquipmentMessageSender --------------------------------------
    //
    // --- LOG equipment state ---
    //
    @Override
    public void confirmEquipmentStateIncorrect() {
        LOG.warn("{}: Equipment state INCORRECT", name);
    }

    @Override
    public void confirmEquipmentStateIncorrect(String arg0) {
        LOG.warn("{}: Equipment state INCORRECT ({})", name, arg0);
    }

    @Override
    public void confirmEquipmentStateOK() {
        LOG.info("{}: Equipment state CORRECT", name);
    }

    @Override
    public void confirmEquipmentStateOK(String arg0) {
        LOG.info("{}: Equipment state CORRECT ({})", arg0);
    }
    
    //
    // --- Check updates sent ---
    //
    private void count(Object arg) {
        if (arg != null) {
            Boolean b = (Boolean) arg;
            if (b == Boolean.TRUE) {
                activations++;
            } else {
                terminations++;
            }
        }        
    }

    private void validateTs(String alarmId, long ts) {
        Long prevTs = this.lastAlarmUpdates.get(alarmId);
        if (prevTs != null) {
            if (ts < prevTs) {
                throw new RuntimeException(alarmId + " has older ts than previous update for it");
            }
        }
        lastAlarmUpdates.put(alarmId, ts);
    }
    
    public int getActivations() {
        return this.activations;
    }
    
    public int getTerminations() {
        return this.terminations;
    }
    
    @Override
    public boolean sendTagFiltered(ISourceDataTag arg0, Object arg1, long arg2) {
        LOG.info("{}: {} {} -> {} ", name, arg0.getName(), arg2, arg1);
        count(arg1);
        validateTs(arg0.getName(), arg2);                
        return true;
    }

    @Override
    public boolean sendTagFiltered(ISourceDataTag arg0, Object arg1, long arg2, String arg3) {
        LOG.info("{}: {} {} -> {} ({})", name, arg0.getName(), arg2, arg1, arg3);
        count(arg1);
        validateTs(arg0.getName(), arg2);                
        return true;
    }

    @Override
    public boolean sendTagFiltered(ISourceDataTag arg0, Object arg1, long arg2, String arg3, boolean arg4) {
        throw new UnsupportedOperationException();
    }

    //
    // --- All the things that should never happen ---
    //
    
    @Override
    public void confirmSubEquipmentStateIncorrect(Long arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void confirmSubEquipmentStateIncorrect(Long arg0, String arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void confirmSubEquipmentStateOK(Long arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void confirmSubEquipmentStateOK(Long arg0, String arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendDelayedTimeDeadbandValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendInvalidTag(ISourceDataTag arg0, short arg1, String arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendInvalidTag(ISourceDataTag arg0, short arg1, String arg2, Timestamp arg3) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendSupervisionAlive() {
        LOG.info("{}: Supervision alive", name);
    }


}
