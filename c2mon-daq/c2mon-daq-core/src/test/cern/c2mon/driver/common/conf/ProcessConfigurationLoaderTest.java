package cern.c2mon.driver.common.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import cern.c2mon.driver.common.conf.core.EquipmentConfiguration;
import cern.c2mon.driver.common.conf.core.EquipmentConfigurationFactory;
import cern.c2mon.driver.common.conf.core.ProcessConfiguration;
import cern.c2mon.driver.common.conf.core.ProcessConfigurationLoader;
import cern.c2mon.driver.tools.processexceptions.ConfRejectedTypeException;
import cern.c2mon.driver.tools.processexceptions.ConfUnknownTypeException;
import cern.tim.shared.daq.datatag.SourceDataTag;
import ch.cern.tim.shared.datatag.address.impl.PLCHardwareAddressImpl;


public class ProcessConfigurationLoaderTest {
    
    private ProcessConfigurationLoader processConfigurationLoader;
    
    private static final String PROCESS_CONFIGURATION_XML = "ProcessConfiguration.xml";
    
    private static final String PROCESS_CONFIGURATION_UNKNOWN_TYPE_XML = "UnknownTypeProcessConfiguration.xml";
    
    private static final String PROCESS_CONFIGURATION_REJECTED_XML = "RejectedProcessConfiguration.xml";
    
    private static final Long PROCESS_PIK = 12345L;
    
    private String processHostName;
    
    @Before
    public void setUp() {
        processConfigurationLoader = new ProcessConfigurationLoader();
        processConfigurationLoader.setEquipmentCononfigurationFactory(EquipmentConfigurationFactory.getInstance());
        
        try {
          this.processHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
          this.processHostName = "NOHOST";
        }
    }
    
    @Test
    public void testCreateProcessConfiguration() 
            throws ConfUnknownTypeException, ConfRejectedTypeException {
        ProcessConfiguration processConfiguration = getProcessConfiguration(PROCESS_CONFIGURATION_XML);
        
        assertEquals(4092L, processConfiguration.getProcessID().longValue());
        assertEquals("P_AIRH4STP887", processConfiguration.getJMSUser());
        assertEquals("AIRH4STP887_P", processConfiguration.getJMSPassword());
        assertEquals("jms/process/factories/QCF", processConfiguration.getJMSQueueConFactJNDIName());
        assertEquals("jms/process/destinations/queues/processmessage/P_AIRH4STP887", processConfiguration.getJMSQueueJNDIName());
//        assertEquals("tim.process.*.P_AIRH4STP887.*", processConfiguration.getListenerTopic());
        assertEquals("tim.process.command."+this.processHostName+".P_AIRH4STP887."+PROCESS_PIK, processConfiguration.getListenerTopic());
        assertEquals(100730, processConfiguration.getAliveTagID());
        assertEquals(60000, processConfiguration.getAliveInterval());
        assertEquals(100, processConfiguration.getMaxMessageSize());
        assertEquals(1000, processConfiguration.getMaxMessageDelay());
        
        Map<Long, EquipmentConfiguration> equipmentMap = 
            processConfiguration.getEquipmentConfigurations();
        assertEquals(2, equipmentMap.values().size());
        
        EquipmentConfiguration equipmentConfiguration1 = equipmentMap.get(1L);
        assertEquals("ch.cern.tim.driver.testhandler.TestMessageHandler", 
                equipmentConfiguration1.getHandlerClassName());
        assertEquals(47014L, equipmentConfiguration1.getCommFaultTagId());
        assertFalse(equipmentConfiguration1.getCommFaultTagValue());
        assertEquals(47321L, equipmentConfiguration1.getAliveTagId());
        assertEquals(120000L, equipmentConfiguration1.getAliveTagInterval());
        assertEquals("interval=100;eventProb=0.8;inRangeProb=1.0;outDeadBandProb=0.0;outDeadBandProb=0.0;switchProb=0.5;startIn=0.01;aliveInterval=30000", 
                equipmentConfiguration1.getAddress());
        
        Map<Long, Boolean> subEq1 = equipmentConfiguration1.getSubEqCommFaultValues();
        assertEquals(2, subEq1.values().size());
        assertTrue(subEq1.get(1L));
        assertFalse(subEq1.get(2L));
        
        Map<Long, SourceDataTag> sourceDataTagMap1 = equipmentConfiguration1.getDataTags();
        assertEquals(2, sourceDataTagMap1.size());
        SourceDataTag sourceDataTag1 = sourceDataTagMap1.get(1L);
        assertEquals("CP.PRE.AIRH4STP887:DEFAUT_PROCESSEUR", sourceDataTag1.getName());
        assertFalse(sourceDataTag1.isControlTag());
        assertEquals("Boolean", sourceDataTag1.getDataType());
        assertEquals(9999999, sourceDataTag1.getAddress().getTimeToLive());
        assertEquals(7, sourceDataTag1.getAddress().getPriority());
        assertTrue(sourceDataTag1.getAddress().isGuaranteedDelivery());
        
        PLCHardwareAddressImpl hardwareAddress = 
            (PLCHardwareAddressImpl)sourceDataTag1.getHardwareAddress();
        assertEquals(5, hardwareAddress.getBlockType());
        assertEquals(0, hardwareAddress.getWordId());
        assertEquals(1, hardwareAddress.getBitId());
        assertTrue(0.0 - hardwareAddress.getPhysicalMinVal() < 0.0000000001);
        assertTrue(0.0 - hardwareAddress.getPhysicMaxVal() < 0.0000000001);
        assertEquals(1, hardwareAddress.getResolutionFactor());
        assertEquals(0, hardwareAddress.getCommandPulseLength());
        assertEquals("INT999", hardwareAddress.getNativeAddress());
        
    }
    
    @Test
    public void testConfigUnknownException() throws ConfRejectedTypeException {
        try {
            getProcessConfiguration(PROCESS_CONFIGURATION_UNKNOWN_TYPE_XML);
            fail("No ConfUnknownException thrown.");
        } catch (ConfUnknownTypeException e) {
//            e.printStackTrace();
        }
        
    }
    
    @Test
    public void testConfigRejectedException() throws ConfUnknownTypeException {
        try {
            getProcessConfiguration(PROCESS_CONFIGURATION_REJECTED_XML);
            fail("No ConfRejectedTypeException thrown.");
        } catch (ConfRejectedTypeException e) {
//            e.printStackTrace();
        }
    }
    
    private ProcessConfiguration getProcessConfiguration(String name) throws ConfUnknownTypeException, ConfRejectedTypeException {
        String path = ProcessConfigurationLoaderTest.class.getResource(name).getPath();
        Document pconfDocument = processConfigurationLoader.fromFiletoDOC(path);
        ProcessConfiguration processConfiguration = processConfigurationLoader.createProcessConfiguration("asd", PROCESS_PIK, pconfDocument, true);
        return processConfiguration;
    }
}
