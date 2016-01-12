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


/**
 * Mobicall alarm POJO. Class has only getters and setters for the alarm attributes used by the alarm 
 * notification through Mobicall system.
 * 
 * A Mobicall-alarm is an alarm defined in our reference database WITH a non-null/non-0 value in the
 * notification id field. This indicates that events should be forwarded to Mobicall with some
 * alarm information and the notification id known by the Mobicall system.
 *  
 * @author mbuttner
 */
public class MobicallAlarm {
    
    private String alarmId;
    private String systemName;              
    private String identifier;              
    private int faultCode;                  
    private String notificationId;              
    private String problemDescription;      
        
    //
    // --- CONSTRUCTION ----------------------------------------------------------------
    //
    public MobicallAlarm(String alarmId) {
        this.alarmId = alarmId;
    }

    // 
    // --- GETTERS ---------------------------------------------------------------------
    //
    public String getAlarmId() {
        return this.alarmId;
    }

    public String getSystemName() {
        return this.systemName;
    }

    public Object getIdentifier() {
        return this.identifier;
    }

    public int getFaultCode() {
        return this.faultCode;
    }

    public String getMobicallId() {
        return this.notificationId;
    }

    public String getProblemDescription() {
        return this.problemDescription;
    }

    //
    // --- SETTERS ---------------------------------------------------------------------------------
    //
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setFaultCode(int faultCode) {
        this.faultCode = faultCode;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

}
