package ch.cern.tim.driver.jec.config;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.address.HardwareAddress;
import cern.tim.shared.daq.command.SourceCommandTag;
import cern.tim.shared.daq.config.ChangeReport;
import ch.cern.tim.driver.jec.IJECRestarter;
import ch.cern.tim.shared.datatag.address.impl.PLCHardwareAddressImpl;
import static org.easymock.classextension.EasyMock.*;

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
        plcTagController = createMock(IJECTagConfigurationController.class);
        jecRestarter = createMock(IJECRestarter.class);
        jecCommandTagChanger = new JECCommandTagChanger(plcTagController, jecRestarter);
    }
    
    @Test
    public void testOnUpdateCommandTagNoChanges() throws ConfigurationException {
        SourceCommandTag oldSourceCommandTag = new SourceCommandTag(2L, "asd2");
        HardwareAddress oldHwAddress = new PLCHardwareAddressImpl(1, 10, 5, 0, 10, 100, "asd", 1000);
        oldSourceCommandTag.setHardwareAddress(oldHwAddress);
        
        // nothing should happen
        
        replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onUpdateCommandTag(sourceCommandTag, oldSourceCommandTag, changeReport);
        verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnUpdateCommandTagNoResartChanges() throws ConfigurationException {
        SourceCommandTag oldSourceCommandTag = new SourceCommandTag(2L, "asd2");
        HardwareAddress oldHwAddress = new PLCHardwareAddressImpl(1, 9, 5, 0, 10, 100, "asd", 1000);
        oldSourceCommandTag.setHardwareAddress(oldHwAddress);
        
        // in address range
        expect(plcTagController.isInAddressRange(hwAddress)).andReturn(true);
        plcTagController.configureCommandTag(sourceCommandTag);
        
        replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onUpdateCommandTag(sourceCommandTag, oldSourceCommandTag, changeReport);
        verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnUpdateCommandTagRestartChanges() throws ConfigurationException {
        SourceCommandTag oldSourceCommandTag = new SourceCommandTag(2L, "asd2");
        HardwareAddress oldHwAddress = new PLCHardwareAddressImpl(1, 9, 5, 0, 10, 100, "asd", 1000);
        oldSourceCommandTag.setHardwareAddress(oldHwAddress);
        
        // not in address range
        expect(plcTagController.isInAddressRange(hwAddress)).andReturn(false);
        jecRestarter.triggerRestart();
        
        replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onUpdateCommandTag(sourceCommandTag, oldSourceCommandTag, changeReport);
        verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnAddCommandTagNoResartChanges() {
        // in address range
        expect(plcTagController.isInAddressRange(hwAddress)).andReturn(true);
        plcTagController.configureCommandTag(sourceCommandTag);
        
        replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onAddCommandTag(sourceCommandTag, changeReport);
        verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void testOnAddCommandTagRestartChanges() {
        // not in address range
        expect(plcTagController.isInAddressRange(hwAddress)).andReturn(false);
        jecRestarter.triggerRestart();
        
        replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onAddCommandTag(sourceCommandTag, changeReport);
        verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
    
    @Test
    public void onRemoveCommandTag() {
        plcTagController.removeCommandTag(sourceCommandTag);
        
        replay(jecRestarter, plcTagController);
        jecCommandTagChanger.onRemoveCommandTag(sourceCommandTag, changeReport);
        verify(jecRestarter, plcTagController);
        assertEquals(ChangeReport.CHANGE_STATE.SUCCESS, changeReport.getState());
    }
}
