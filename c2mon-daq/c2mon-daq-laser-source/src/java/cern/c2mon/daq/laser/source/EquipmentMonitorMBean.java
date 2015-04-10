/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import cern.c2mon.shared.common.datatag.ISourceDataTag;

public interface EquipmentMonitorMBean {

    
    public Long getDataTag();
    
    public void setDataTag(Long id);
    
    public boolean getValue();
    
    public void setValue(boolean value);
    
    public int getAlarmsOn();
    
    
}
