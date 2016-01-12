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
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;

public class JECEquipmentConfigurationChangerTest {

    private JECEquipmentConfigurationChanger jecEquipmentConfigurationChanger;
    private IJECRestarter jecRestarter;
    private EquipmentConfiguration equipmentConfiguration;
    private ChangeReport changeReport;
    
    @Before
    public void setUp() {
        jecRestarter = EasyMock.createMock(IJECRestarter.class);
        jecEquipmentConfigurationChanger = new JECEquipmentConfigurationChanger(jecRestarter);
        equipmentConfiguration = new EquipmentConfiguration();
    }
    
    @Test
    public void testOnEquipmentUpdateNoRestart() {
        changeReport = new ChangeReport(1L);
        EquipmentConfiguration oldEquipmentConfiguration = new EquipmentConfiguration();
        oldEquipmentConfiguration.setAliveTagId(1L);
        
        // nothing should happen
        
        EasyMock.replay(jecRestarter);
        jecEquipmentConfigurationChanger.onUpdateEquipmentConfiguration(equipmentConfiguration, oldEquipmentConfiguration, changeReport);
        EasyMock.verify(jecRestarter);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnEquipmentUpdateAdressChange() {
        changeReport = new ChangeReport(1L);
        EquipmentConfiguration oldEquipmentConfiguration = new EquipmentConfiguration();
        oldEquipmentConfiguration.setAddress("old");
        
        // restart should happen
        jecRestarter.triggerRestart();
        
        EasyMock.replay(jecRestarter);
        jecEquipmentConfigurationChanger.onUpdateEquipmentConfiguration(equipmentConfiguration, oldEquipmentConfiguration, changeReport);
        EasyMock.verify(jecRestarter);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnEquipmentUpdateAliveIntervalChange() {
        changeReport = new ChangeReport(1L);
        EquipmentConfiguration oldEquipmentConfiguration = new EquipmentConfiguration();
        oldEquipmentConfiguration.setAliveTagInterval(10000);
        
        // restart should happen
        jecRestarter.triggerRestart();
        
        EasyMock.replay(jecRestarter);
        jecEquipmentConfigurationChanger.onUpdateEquipmentConfiguration(equipmentConfiguration, oldEquipmentConfiguration, changeReport);
        EasyMock.verify(jecRestarter);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
}
