/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import cern.c2mon.shared.common.datatag.ISourceDataTag;
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

}
