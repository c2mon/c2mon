package ch.cern.tim.driver.jec.config;

import static org.easymock.classextension.EasyMock.createMock;

import org.junit.Before;
import org.junit.Test;
import static org.easymock.classextension.EasyMock.*;
import static junit.framework.Assert.*;

import cern.tim.driver.common.conf.core.EquipmentConfiguration;
import cern.tim.shared.daq.config.ChangeReport;
import ch.cern.tim.driver.jec.IJECRestarter;

public class JECEquipmentConfigurationChangerTest {

    private JECEquipmentConfigurationChanger jecEquipmentConfigurationChanger;
    private IJECRestarter jecRestarter;
    private EquipmentConfiguration equipmentConfiguration;
    private ChangeReport changeReport;
    
    @Before
    public void setUp() {
        jecRestarter = createMock(IJECRestarter.class);
        jecEquipmentConfigurationChanger = new JECEquipmentConfigurationChanger(jecRestarter);
        equipmentConfiguration = new EquipmentConfiguration();
    }
    
    @Test
    public void testOnEquipmentUpdateNoRestart() {
        changeReport = new ChangeReport(1L);
        EquipmentConfiguration oldEquipmentConfiguration = new EquipmentConfiguration();
        oldEquipmentConfiguration.setAliveTagId(1L);
        
        // nothing should happen
        
        replay(jecRestarter);
        jecEquipmentConfigurationChanger.onUpdateEquipmentConfiguration(equipmentConfiguration, oldEquipmentConfiguration, changeReport);
        verify(jecRestarter);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnEquipmentUpdateAdressChange() {
        changeReport = new ChangeReport(1L);
        EquipmentConfiguration oldEquipmentConfiguration = new EquipmentConfiguration();
        oldEquipmentConfiguration.setAddress("old");
        
        // restart should happen
        jecRestarter.triggerRestart();
        
        replay(jecRestarter);
        jecEquipmentConfigurationChanger.onUpdateEquipmentConfiguration(equipmentConfiguration, oldEquipmentConfiguration, changeReport);
        verify(jecRestarter);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnEquipmentUpdateAliveIntervalChange() {
        changeReport = new ChangeReport(1L);
        EquipmentConfiguration oldEquipmentConfiguration = new EquipmentConfiguration();
        oldEquipmentConfiguration.setAliveTagInterval(10000);
        
        // restart should happen
        jecRestarter.triggerRestart();
        
        replay(jecRestarter);
        jecEquipmentConfigurationChanger.onUpdateEquipmentConfiguration(equipmentConfiguration, oldEquipmentConfiguration, changeReport);
        verify(jecRestarter);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
}
