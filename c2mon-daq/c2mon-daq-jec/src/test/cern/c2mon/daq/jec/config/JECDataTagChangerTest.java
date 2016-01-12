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
package cern.c2mon.daq.jec.config;

import static org.junit.Assert.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.jec.IJECRestarter;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
import cern.c2mon.shared.daq.config.ChangeReport;

public class JECDataTagChangerTest {

    private JECDataTagChanger jecDataTagChanger;
    private IJECTagConfigurationController plcTagController;
    private IJECRestarter jecRestarter;
    private SourceDataTag sourceDataTag;
    private ChangeReport changeReport;
    private PLCHardwareAddressImpl hwAddress;
    
    @Before
    public void setUp() throws ConfigurationException {
        changeReport = new ChangeReport(1L);
        sourceDataTag = new SourceDataTag(1L, "asd", false);
        hwAddress = new PLCHardwareAddressImpl(1, 10, 5, 0, 10, 100, "asd", 1000);
        sourceDataTag.setAddress(new DataTagAddress(hwAddress));
        plcTagController = EasyMock.createMock(IJECTagConfigurationController.class);
        jecRestarter = EasyMock.createMock(IJECRestarter.class);
        // TODO: to add a LOGGER inestead of null 
        jecDataTagChanger = new JECDataTagChanger(plcTagController, jecRestarter, null);
    }
    
    @Test
    public void testOnUpdateDataTagNoChanges() throws ConfigurationException {
        SourceDataTag oldSourceDataTag = new SourceDataTag(2L, "asd2", false);
        HardwareAddress oldHwAddress = new PLCHardwareAddressImpl(1, 10, 5, 0, 10, 100, "asd", 1000);
        oldSourceDataTag.setAddress(new DataTagAddress(oldHwAddress));
        
        // nothing should happen
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecDataTagChanger.onUpdateDataTag(sourceDataTag, oldSourceDataTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnUpdateDataTagNoResartChanges() throws ConfigurationException {
        SourceDataTag oldSourceDataTag = new SourceDataTag(2L, "asd2", false);
        HardwareAddress oldHwAddress = new PLCHardwareAddressImpl(1, 9, 5, 0, 10, 100, "asd", 1000);
        oldSourceDataTag.setAddress(new DataTagAddress(oldHwAddress));
        
        // in address range
        EasyMock.expect(plcTagController.isInAddressRange(hwAddress)).andReturn(true);
        plcTagController.configureDataTag(sourceDataTag);
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecDataTagChanger.onUpdateDataTag(sourceDataTag, oldSourceDataTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnUpdateDataTagRestartChanges() throws ConfigurationException {
        SourceDataTag oldSourceDataTag = new SourceDataTag(2L, "asd2", false);
        HardwareAddress oldHwAddress = new PLCHardwareAddressImpl(1, 9, 5, 0, 10, 100, "asd", 1000);
        oldSourceDataTag.setAddress(new DataTagAddress(oldHwAddress));
        
        // not in address range
        EasyMock.expect(plcTagController.isInAddressRange(hwAddress)).andReturn(false);
        jecRestarter.triggerRestart();
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecDataTagChanger.onUpdateDataTag(sourceDataTag, oldSourceDataTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
        assertEquals(JECDataTagChanger.OUT_OF_RANGE_WARN_MESSAGE, changeReport.getWarnMessage());
    }
    
    @Test
    public void testOnAddDataTagNoResartChanges() {
        // in address range
        EasyMock.expect(plcTagController.isInAddressRange(hwAddress)).andReturn(true);
        plcTagController.configureDataTag(sourceDataTag);
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecDataTagChanger.onAddDataTag(sourceDataTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
        assertEquals(JECDataTagChanger.WITHIN_RANGE_INFO_MESSAGE, changeReport.getInfoMessage());
    }
    
    @Test
    public void testOnAddDataTagRestartChanges() {
        // not in address range
        EasyMock.expect(plcTagController.isInAddressRange(hwAddress)).andReturn(false);
        jecRestarter.triggerRestart();
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecDataTagChanger.onAddDataTag(sourceDataTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void onRemoveDataTag() {
        plcTagController.removeDataTag(sourceDataTag);
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecDataTagChanger.onRemoveDataTag(sourceDataTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
}
