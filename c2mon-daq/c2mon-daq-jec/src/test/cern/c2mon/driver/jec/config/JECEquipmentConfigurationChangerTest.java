package cern.c2mon.driver.jec.config;



import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.driver.jec.IJECRestarter;
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
