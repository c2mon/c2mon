package cern.c2mon.driver.jec.config;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.driver.jec.IJECRestarter;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
import cern.c2mon.shared.daq.command.SourceCommandTag;
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
