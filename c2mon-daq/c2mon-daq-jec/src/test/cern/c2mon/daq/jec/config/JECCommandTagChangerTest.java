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

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.jec.IJECRestarter;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.command.SourceCommandTag;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
import cern.c2mon.shared.daq.config.ChangeReport;

public class JECCommandTagChangerTest {

    private JECCommandTagChanger jecCommandTagChanger;
    private IJECTagConfigurationController plcTagController;
    private IJECRestarter jecRestarter;
    private SourceCommandTag sourceCommandTag;
    private ChangeReport changeReport;
    private PLCHardwareAddressImpl hwAddress;
    
    @Before
    public void setUp() throws ConfigurationException {
        changeReport = new ChangeReport(1L);
        sourceCommandTag = new SourceCommandTag(1L, "asd");
        hwAddress = new PLCHardwareAddressImpl(1, 10, 5, 0, 10, 100, "asd", 1000);
        sourceCommandTag.setHardwareAddress(hwAddress);
        plcTagController = EasyMock.createMock(IJECTagConfigurationController.class);
        jecRestarter = EasyMock.createMock(IJECRestarter.class);
        jecCommandTagChanger = new JECCommandTagChanger(plcTagController, jecRestarter);
    }
    
    @Test
    public void testOnUpdateCommandTagNoChanges() throws ConfigurationException {
        SourceCommandTag oldSourceCommandTag = new SourceCommandTag(2L, "asd2");
        HardwareAddress oldHwAddress = new PLCHardwareAddressImpl(1, 10, 5, 0, 10, 100, "asd", 1000);
        oldSourceCommandTag.setHardwareAddress(oldHwAddress);
        
        // nothing should happen
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onUpdateCommandTag(sourceCommandTag, oldSourceCommandTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnUpdateCommandTagNoResartChanges() throws ConfigurationException {
        SourceCommandTag oldSourceCommandTag = new SourceCommandTag(2L, "asd2");
        HardwareAddress oldHwAddress = new PLCHardwareAddressImpl(1, 9, 5, 0, 10, 100, "asd", 1000);
        oldSourceCommandTag.setHardwareAddress(oldHwAddress);
        
        // in address range
        EasyMock.expect(plcTagController.isInAddressRange(hwAddress)).andReturn(true);
        plcTagController.configureCommandTag(sourceCommandTag);
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onUpdateCommandTag(sourceCommandTag, oldSourceCommandTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnUpdateCommandTagRestartChanges() throws ConfigurationException {
        SourceCommandTag oldSourceCommandTag = new SourceCommandTag(2L, "asd2");
        HardwareAddress oldHwAddress = new PLCHardwareAddressImpl(1, 9, 5, 0, 10, 100, "asd", 1000);
        oldSourceCommandTag.setHardwareAddress(oldHwAddress);
        
        // not in address range
        EasyMock.expect(plcTagController.isInAddressRange(hwAddress)).andReturn(false);
        jecRestarter.triggerRestart();
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onUpdateCommandTag(sourceCommandTag, oldSourceCommandTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnAddCommandTagNoResartChanges() {
        // in address range
        EasyMock.expect(plcTagController.isInAddressRange(hwAddress)).andReturn(true);
        plcTagController.configureCommandTag(sourceCommandTag);
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onAddCommandTag(sourceCommandTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnAddCommandTagRestartChanges() {
        // not in address range
        EasyMock.expect(plcTagController.isInAddressRange(hwAddress)).andReturn(false);
        jecRestarter.triggerRestart();
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onAddCommandTag(sourceCommandTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void onRemoveCommandTag() {
        plcTagController.removeCommandTag(sourceCommandTag);
        
        EasyMock.replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onRemoveCommandTag(sourceCommandTag, changeReport);
        EasyMock.verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
}
