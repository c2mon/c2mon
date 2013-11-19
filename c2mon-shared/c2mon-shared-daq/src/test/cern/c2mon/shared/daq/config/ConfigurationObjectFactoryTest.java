package cern.c2mon.shared.daq.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cern.c2mon.shared.common.DOMFactory;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.daq.command.SourceCommandTag;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;

public class ConfigurationObjectFactoryTest {
    private static final String TEST_UPDATE_XML = "/TestUpdateConfigurationChangeEvent.xml";
    private static final String TEST_REPORT_XML = "/TestConfigurationChangeEventReport.xml";
    private static final String TEST_EXTENDED_UPDATE_XML = "/TestExtendedDataTagUpdate.xml";
    private static final String TEST_EQUIPMENT_UNIT_ADD_XML = "/TestEquipmentUnitAdd.xml";
    private static final String TEST_EQUIPMENT_UNIT_REM_XML = "/TestEquipmentUnitRem.xml";
    
    private ConfigurationObjectFactory objectFactory = new ConfigurationObjectFactory();
    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    
    private class DataTagUpdateExtended extends DataTagUpdate {
        private int testNumber;

        public void setTestNumber(int testNumber) {
            this.testNumber = testNumber;
        }

        public int getTestNumber() {
            return testNumber;
        }
    }
    
    @Test
    public void testDataTagUpdate() throws ParserConfigurationException, SAXException, IOException, SecurityException, DOMException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, NoSimpleValueParseException {
        Document document = getDocument(TEST_UPDATE_XML);
        NodeList list = document.getElementsByTagName("DataTagUpdate");
        DataTagUpdate dataTagUpdate = objectFactory.createDataTagUpdate((Element) list.item(0));
        DataTagAddressUpdate dataTagAddressUpdate = dataTagUpdate.getDataTagAddressUpdate();
        HardwareAddressUpdate hardwareAddressUpdate = dataTagAddressUpdate.getHardwareAddressUpdate();
        assertEquals("Integer", dataTagUpdate.getDataType());
        assertTrue(dataTagUpdate.getMinValue().compareTo(0) > 0);
        assertTrue(dataTagUpdate.getMaxValue().compareTo(9) < 0);
        assertEquals(Integer.valueOf(7), dataTagAddressUpdate.getPriority());
        assertEquals("asd", hardwareAddressUpdate.getChangedValues().get("opcItemName"));
        assertEquals("mode", dataTagUpdate.getFieldsToRemove().get(0));
    }
    
    @Test
    public void testDataTagUpdateFill() throws NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, NoSimpleValueParseException, ParserConfigurationException, SAXException, IOException {
        Document document = getDocument(TEST_EXTENDED_UPDATE_XML);
        NodeList list = document.getElementsByTagName("DataTagUpdate");
        DataTagUpdateExtended dataTagUpdate = new DataTagUpdateExtended();
        objectFactory.fillDataTagUpdate((Element) list.item(0), dataTagUpdate);
        DataTagAddressUpdate dataTagAddressUpdate = dataTagUpdate.getDataTagAddressUpdate();
        assertNull(dataTagAddressUpdate);
        assertEquals(12, dataTagUpdate.getTestNumber());
        assertEquals("Integer", dataTagUpdate.getDataType());
        assertTrue(dataTagUpdate.getMinValue().compareTo(0) > 0);
        assertTrue(dataTagUpdate.getMaxValue().compareTo(9) < 0);
        assertEquals("mode", dataTagUpdate.getFieldsToRemove().get(0));
    }
    
    @Test
    public void testCommandTagUpdate() throws ParserConfigurationException, SAXException, IOException, SecurityException, DOMException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, NoSimpleValueParseException {
        Document document = getDocument(TEST_UPDATE_XML);
        NodeList list = document.getElementsByTagName("CommandTagUpdate");
        CommandTagUpdate commandTagUpdate = objectFactory.createCommandTagUpdate((Element) list.item(0));
        HardwareAddressUpdate hardwareAddressUpdate = commandTagUpdate.getHardwareAddressUpdate();
        assertEquals(Integer.valueOf(10), commandTagUpdate.getSourceRetries());
        assertEquals("asd", hardwareAddressUpdate.getChangedValues().get("opcItemName"));
        assertEquals(commandTagUpdate.getFieldsToRemove().get(0), "sourceTimeout");
    }
    
    @Test
    public void testTagRemove() throws NoSuchFieldException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, NoSimpleValueParseException {
        Document document = getDocument(TEST_UPDATE_XML);
        Element commandRemove = (Element) document.getElementsByTagName("CommandTagRemove").item(0);
        Element dataRemove = (Element) document.getElementsByTagName("DataTagRemove").item(0);
        CommandTagRemove commandTagRemove = objectFactory.createCommandTagRemove(commandRemove);
        DataTagRemove dataTagRemove = objectFactory.createDataTagRemove(dataRemove);
        assertEquals(1L, commandTagRemove.getChangeId());
        assertEquals(23L, commandTagRemove.getCommandTagId());
        assertEquals(2L, dataTagRemove.getChangeId());
        assertEquals(34L, dataTagRemove.getDataTagId());
    }
    
    @Test
    public void testCommandTagAdd() throws NoSuchFieldException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, NoSimpleValueParseException {
        Document document = getDocument(TEST_UPDATE_XML);
        Element commandTagAddElement = (Element) document.getElementsByTagName("CommandTagAdd").item(0);
        CommandTagAdd commandTagAdd = objectFactory.createCommandTagAdd(commandTagAddElement);
        assertEquals(3, commandTagAdd.getChangeId());
        SourceCommandTag commandTag = commandTagAdd.getSourceCommandTag();
        assertEquals(100, commandTag.getSourceRetries());
        assertEquals(1000, commandTag.getSourceTimeout());
        OPCHardwareAddressImpl address = (OPCHardwareAddressImpl) commandTag.getHardwareAddress();
        assertEquals("asd", address.getOPCItemName());
    }
    
    @Test
    public void testGenerateChanges() throws NoSuchFieldException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
        Document document = getDocument(TEST_UPDATE_XML);
        List<Change> changes = objectFactory.generateChanges(document.getDocumentElement());
//        System.out.println("Changes total: " + changes.size());
        assertTrue(changes.size() == 12);
    }
    
    
    @Test
    public void testEquipmentUnitAdd() throws NoSuchFieldException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, NoSimpleValueParseException, InstantiationException {
        Document document = getDocument(TEST_EQUIPMENT_UNIT_ADD_XML);
        Element eqUnitAddElement = (Element) document.getElementsByTagName("EquipmentUnitAdd").item(0);
        EquipmentUnitAdd eqUnitAdd = objectFactory.createEquipmentUnitAdd(eqUnitAddElement);
        assertEquals(1,eqUnitAdd.getChangeId());
        assertEquals(2408,eqUnitAdd.getEquipmentId());
        assertNotNull(eqUnitAdd.getEquipmentUnitXml());
        
        
        
        // now try to recreate DOM from the EquipmentUnitAdd event object
        DOMFactory factory = new DOMFactory();
        Document document2 = factory.createDocument();
        
        Element element = factory.generateSimpleElement(document2, eqUnitAdd, new String[0]);        
        document2.appendChild(element);
        
        Element eqUnitAddElement2 = (Element) document2.getElementsByTagName("EquipmentUnitAdd").item(0);
        EquipmentUnitAdd eqUnitAdd2 = objectFactory.createEquipmentUnitAdd(eqUnitAddElement2);
        
        assertNotNull(eqUnitAdd2.getEquipmentUnitXml());
                  
        // both ids should be the same
        assertEquals(eqUnitAdd.getChangeId(), eqUnitAdd2.getChangeId());
        
        assertEquals(eqUnitAdd.getEquipmentId(), eqUnitAdd2.getEquipmentId());
        
        // both equipment xmls should be the same
        assertEquals(eqUnitAdd.getEquipmentUnitXml(), eqUnitAdd2.getEquipmentUnitXml());        
        
        
    }
        
    
    @Test
    public void testEquipmentUnitRemove() throws NoSuchFieldException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, NoSimpleValueParseException, InstantiationException {
        Document document = getDocument(TEST_EQUIPMENT_UNIT_REM_XML);
        Element eqUnitRemElement = (Element) document.getElementsByTagName("EquipmentUnitRemove").item(0);
        EquipmentUnitRemove eqUnitRem = objectFactory.createEquipmentUnitRemove(eqUnitRemElement);
        assertEquals(1,eqUnitRem.getChangeId());
        assertEquals(2,eqUnitRem.getEquipmentId());
        
        // now try to recreate DOM from the EquipmentUnitRemove event object
        DOMFactory factory = new DOMFactory();
        Document document2 = factory.createDocument();
                
        Element element = factory.generateSimpleElement(document2, eqUnitRem, new String[0]);
        document2.appendChild(element);
        
        Element eqUnitRemElement2 = (Element) document2.getElementsByTagName("EquipmentUnitRemove").item(0);
        EquipmentUnitRemove eqUnitRem2 = objectFactory.createEquipmentUnitRemove(eqUnitRemElement2);
                  
        // both ids should be the same
        assertEquals(eqUnitRem.getChangeId(), eqUnitRem2.getChangeId());
        // both equipment xmls should be the same
        assertEquals(eqUnitRem.getEquipmentId(),eqUnitRem2.getEquipmentId());       
    }    
    
    @Test
    public void testConfigurationReport() throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException, ParserConfigurationException, SAXException, IOException {
        Document document = getDocument(TEST_REPORT_XML);
        ConfigurationChangeEventReport report = objectFactory.createConfigurationChangeEventReport(document.getDocumentElement());
        
        assertEquals(5, report.getChangeReports().size());
        
        ChangeReport changeReport1 = report.getChangeReports().get(0);
        assertNull(changeReport1.getErrorMessage());
        assertNull(changeReport1.getWarnMessage());
        assertEquals("<info", changeReport1.getInfoMessage());
        assertEquals(CHANGE_STATE.SUCCESS, changeReport1.getState());
        
        ChangeReport changeReport2 = report.getChangeReports().get(3);
        assertNull(changeReport2.getInfoMessage());
        assertNull(changeReport2.getWarnMessage());
        assertEquals("error", changeReport2.getErrorMessage());
        assertEquals(CHANGE_STATE.FAIL, changeReport2.getState());
    }

    
    private Document getDocument(String resourceLocation) throws ParserConfigurationException, SAXException, IOException {
        URL documentURL = getClass().getResource(resourceLocation);
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        Document document = builder.parse(documentURL.openStream());
        return document;
    }

}
