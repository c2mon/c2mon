/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;


/**
 * Mobicall alarm POJO. Class has only getters and setters for the alarm attributes used by the alarm 
 * notification through Mobicall system
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
