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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import javax.jms.JMSException;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;

/**
 * Emulates the C2monConnection behavior. The class provides methods to simulate alarm events 
 * coming from C2MON and obtain possible situations on the C2MON (like invalid datatags)
 * 
 * @author mbuttner
 */
public class C2monConnectionMock implements C2monConnectionIntf {

    AlarmListener listener;

    /**
     * Inject an alarm activation into the system. The "valid"-boolean is translated into a fake
     * tag id for the underlying datatag. If valid is false the tag id is set to a value which will
     * be interpreted by the getQuality call as invalid.
     * @param ff <code>String</code> the fault family or system name
     * @param fm <code>String</code> the fault member or device name
     * @param fc <code>int</code> the fault code
     * @param valid <code>boolean</code> validity of the underlying datatag (to simulate invalid in tests)
     */
    public void activateAlarm(String ff, String fm, int fc, boolean valid) {
        long tagId = 3;
        if (valid) {
            tagId = 1;
        }
        AlarmValue av = new AlarmValueImpl(1L, fc, fm, ff, "Activation", tagId, getSystemTs(), true);
        listener.onAlarmUpdate(av);
    }

    /**
     * Inject an alarm termination into the system. Same behavior as activation, see activateAlarm()
     */
    @SuppressWarnings("javadoc")
    public void terminateAlarm(String ff, String fm, int fc, boolean valid) {
        long tagId = 2;
        if (valid) {
            tagId = 1;
        }
        AlarmValue av = new AlarmValueImpl(1L, fc, fm, ff, "Activation", tagId, getSystemTs(), false);
        listener.onAlarmUpdate(av);
    }

    //
    // --- Implements C2monConnectionIntf ------------------------------------------------------------
    //

    @Override
    public void setListener(AlarmListener listener) {
        this.listener = listener;
    }

    /**
     * Return initial active list. To check the behavior of the startup procedure, one alarm is considered 
     * to become active "now", and another one two minutes ago: the second one is too old to be notified,
     * the tests validate that no notification is sent for this one. 
     */
    @Override
    public Collection<AlarmValue> getActiveAlarms() {
        ArrayList<AlarmValue> activeAlarms = new ArrayList<AlarmValue>();

        AlarmValue av = new AlarmValueImpl(1L, 1, "FM", "FF", "Info", 1L, new Timestamp(System.currentTimeMillis()),
                true);
        activeAlarms.add(av);

        AlarmValue av2 = new AlarmValueImpl(2L, 2, "FM", "FF", "Info", 2L, new Timestamp(System.currentTimeMillis()
                - (120 * 1000)), true);
        activeAlarms.add(av2);

        return activeAlarms;
    }

    @Override
    public void connectListener() throws JMSException {
        // not needed for mock
    }

    @Override
    public void start() throws Exception {
        // not needed for mock
    }

    @Override
    public void stop() {
        // not needed for mock
    }

    /**
     * Simulate valid and invalid datatags: if the tag id is greater than 2, the method will reply that the tag is not
     * valid (allows to check the behavior if the datatag for a given alarm is not valid (see activate and terminate
     * above).
     */
    @Override
    public boolean isTagValid(Long tagId) {
        if (tagId > 2) {
            return false;
        }
        return true;
    }

    //
    // --- PRIVATE METHODS -------------------------------------------------------------------------
    //
    private Timestamp getSystemTs() {
        return new Timestamp(System.currentTimeMillis());
    }

}
