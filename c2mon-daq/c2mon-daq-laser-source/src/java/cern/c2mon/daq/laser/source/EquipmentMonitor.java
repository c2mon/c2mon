/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.LASERHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;

public class EquipmentMonitor implements EquipmentMonitorMBean {

    private IEquipmentConfiguration equipment;
    private ISourceDataTag dataTag;
    
    public EquipmentMonitor(IEquipmentConfiguration equipmentConfiguration) {
        this.equipment=equipmentConfiguration;
    }
    
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

    @Override
    public void setValue(boolean value) {
        this.dataTag.getCurrentValue().setValue(value);
    }
    
    
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

    @Override
    public int getNumberOfAlarms() {
        return equipment.getSourceDataTags().size();
    }
    
    @Override
    public boolean getDataTagStatus(String faultFamily, String faultMember, int faultCode) {
        
        for (ISourceDataTag sourceDataTag : equipment.getSourceDataTags().values()) {
            if(sourceDataTag.getHardwareAddress() instanceof LASERHardwareAddress) {
                LASERHardwareAddress address = (LASERHardwareAddress) sourceDataTag.getHardwareAddress();
                if(address.getFaultFamily().equalsIgnoreCase(faultFamily) && address.getFaultMember().equalsIgnoreCase(faultMember) && address.getFalutCode() == faultCode) {
                    if(sourceDataTag.getCurrentValue() != null) {
                        return (boolean) sourceDataTag.getCurrentValue().getValue();
                    }
                    break;
                }
            }
        }
        throw new NullPointerException("DataTag not found, please check parameters.");
    }

}
