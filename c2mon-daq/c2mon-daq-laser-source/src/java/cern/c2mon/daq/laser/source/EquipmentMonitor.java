/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;

/**
 * Implementation of the JMX bean exposing data collected by the DAQ
 * 
 * TODO check how this is supposed to work from a JavaConsole as it works only on one tag at a time
 *      for a certain number of methods.
 * 
 * @author mbuttner
 */
public class EquipmentMonitor implements EquipmentMonitorMBean {

    private static final Logger LOG = LoggerFactory.getLogger(EquipmentMonitor.class);
    
    private IEquipmentConfiguration equipment;
    private ISourceDataTag dataTag;
    private LaserNativeMessageHandler handler;
    
    //
    // --- CONSTRUCTION ---------------------------------------------------------------------------
    //
    public EquipmentMonitor(IEquipmentConfiguration equipment, LaserNativeMessageHandler handler) {
        this.equipment = equipment;
        this.handler = handler;
    }
    
    //
    // --- Implements EquipmentMonitorMBean -------------------------------------------------------
    //
    @Override
    public Long getDataTag() {
        return dataTag.getId();
    }

    @Override
    public  void setDataTag(Long id) {
        this.dataTag = equipment.getSourceDataTag(id);
    }

    @Override
    public boolean getValue() {
        return (boolean) dataTag.getCurrentValue().getValue();
    }

    /**
     * Due to API restrictions on the C2MON side, the set will succeeed only on initialized 
     * tag values.
     */
    @Override
    public boolean setValue(boolean value) {
        if (dataTag.getCurrentValue() != null) {
            this.dataTag.getCurrentValue().setValue(value);
            return true;
        } 
        // this will happen during test with sender mock (which does not update tag values in DAQ cache)
        LOG.warn("Attempt to assign new value to uninitialized datatag " + dataTag.getName());
        return false;
    }
    
    /**
     * @return <code>int</code> the count of active alarms in this DAQ
     */
    @Override
    public int getAlarmsOn() {
        int i = 0;
        for (ISourceDataTag data : equipment.getSourceDataTags().values()) {
            if (data.getCurrentValue() != null && data.getCurrentValue().getValue().equals(true)) {
                i++;
            }
        }
        return i;
    }

    /**
     * @return <code>int</code> number of configured alarms - 1 because we do not take care of the heartbeat tag
     */
    @Override
    public int getNumberOfAlarms() {
        return equipment.getSourceDataTags().size() - 1;
    }
    
    /**
     * @return the activation status of the alarm identified by the triplet passed as parameter
     */
    @Override
    public boolean getDataTagStatus(String faultFamily, String faultMember, int faultCode) {
        String alarmId = faultFamily + ":" + faultMember + ":" + faultCode;
        ISourceDataTag tag = handler.getTagByAlarmId(alarmId);
        if (tag != null && tag.getCurrentValue() != null) {
            return (boolean) tag.getCurrentValue().getValue();
        }
        throw new IllegalStateException("DataTag for " + alarmId + " not found, please check parameters.");
    }

}
