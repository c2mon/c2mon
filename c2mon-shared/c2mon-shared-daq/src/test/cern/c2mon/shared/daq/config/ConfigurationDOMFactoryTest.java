package cern.c2mon.shared.daq.config;

import static org.junit.Assert.assertFalse;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;

public class ConfigurationDOMFactoryTest extends AbstractXMLTst {
    
    private ConfigurationDOMFactory xmlFactory = new ConfigurationDOMFactory();
    
    private static final String CONFIGURATION_EVENT_XSD = "http://timweb.cern.ch/schemas/c2mon-daq/ConfigurationChangeEvent.xsd";

    private static final String CONFIGURATION_REPORT_XSD = "http://timweb.cern.ch/schemas/c2mon-daq/ConfigurationChangeEventReport.xsd";

    private class DataTagUpdateExtended extends DataTagUpdate {
        private int testNumber;

        public DataTagUpdateExtended(long changeId, long dataTagId, long equipmentId) {
            super(changeId, dataTagId, equipmentId);
        }

        public void setTestNumber(int testNumber) {
            this.testNumber = testNumber;
        }

        public int getTestNumber() {
            return testNumber;
        }
    }
    
    @Test
    public void testDataTagUpdateElement() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, SAXException, IOException {
        DataTagUpdate dataTagUpdate = createDataTagUpdate();
        Document document = xmlFactory.createDocument();
        Element configurationElement = xmlFactory.createConfigurationChangeEventElement(document);
        document.appendChild(configurationElement);
        Node node = xmlFactory.createDataTagUpdateElement(document, dataTagUpdate);
        configurationElement.appendChild(node);
        validateDocument(document, CONFIGURATION_EVENT_XSD);
    }
    
    @Test
    public void testDataTagUpdateElementExtended() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, SAXException, IOException {
        DataTagUpdate dataTagUpdate = createDataTagUpdateExtended();
        Document document = xmlFactory.createDocument();
        Element configurationElement = xmlFactory.createConfigurationChangeEventElement(document);
        document.appendChild(configurationElement);
        Node node = xmlFactory.createDataTagUpdateElement(document, new DataTagUpdate(dataTagUpdate));
        configurationElement.appendChild(node);
        validateDocument(document, CONFIGURATION_EVENT_XSD);
        assertFalse(getDocumentString(document).contains("test-number"));
    }
    
    @Test
    public void testDataTagAddNode() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, SAXException, IOException {
        DataTagAdd dataTagAdd = createDataTagAdd();
        DataTagAdd anotherDataTagAdd = createDataTagAdd();
        Document document = xmlFactory.createDocument();
        Element configurationElement = xmlFactory.createConfigurationChangeEventElement(document);
        document.appendChild(configurationElement);
        Element dataTagAddElement = xmlFactory.createDataTagAddElement(document, dataTagAdd);
        Element anotherDataTagAddElement = xmlFactory.createDataTagAddElement(document, anotherDataTagAdd);
        configurationElement.appendChild(dataTagAddElement);
        configurationElement.appendChild(anotherDataTagAddElement);
        validateDocument(document, CONFIGURATION_EVENT_XSD);
//        printDocument(document);
    }
    
    @Test
    public void testRemoveElement() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, SAXException, IOException {
        CommandTagRemove commandTagRemove = new CommandTagRemove(25L, 23L, 234L);
        DataTagRemove dataTagRemove = new DataTagRemove(24L, 22L, 324L);
        
        Document document = xmlFactory.createDocument();
        Element configElement = xmlFactory.createConfigurationChangeEventElement(document);
        document.appendChild(configElement);
        
        Element commandTagRemoveElement = xmlFactory.createCommandTagRemoveElement(document, commandTagRemove);
        Element dataTagRemoveElement = xmlFactory.createDataTagRemoveElement(document, dataTagRemove);
        configElement.appendChild(commandTagRemoveElement);
        configElement.appendChild(dataTagRemoveElement);
        
        validateDocument(document, CONFIGURATION_EVENT_XSD);
//        printDocument(document);
    }
    
    @Test
    public void testMultipleElements() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, SAXException, IOException {
        CommandTagRemove commandTagRemove = new CommandTagRemove(25L, 234L, 23L);
        DataTagRemove dataTagRemove = new DataTagRemove(24L, 22L, 324L);
        DataTagAdd dataTagAdd = createDataTagAdd();
        DataTagAdd anotherDataTagAdd = createDataTagAdd();
        DataTagUpdate dataTagUpdate = createDataTagUpdate();
        EquipmentConfigurationUpdate equipmentConfigurationUpdate = new EquipmentConfigurationUpdate(8324L, 38432L);
        equipmentConfigurationUpdate.setAliveInterval(1000L);
        
        Document document = xmlFactory.createDocument();
        Element configElement = xmlFactory.createConfigurationChangeEventElement(document);
        document.appendChild(configElement);
        
        Element commandTagRemoveElement = xmlFactory.createCommandTagRemoveElement(document, commandTagRemove);
        Element dataTagRemoveElement = xmlFactory.createDataTagRemoveElement(document, dataTagRemove);
        Element dataTagAddElement = xmlFactory.createDataTagAddElement(document, dataTagAdd);
        Element anotherDataTagAddElement = xmlFactory.createDataTagAddElement(document, anotherDataTagAdd);
        Element dataTagUpdateElement = xmlFactory.createDataTagUpdateElement(document, dataTagUpdate);
        Element equipmenUnitUpdateElement = xmlFactory.createEquipmentUnitUpdateElement(document, equipmentConfigurationUpdate);
        
        configElement.appendChild(commandTagRemoveElement);
        configElement.appendChild(dataTagRemoveElement);
        configElement.appendChild(dataTagAddElement);
        configElement.appendChild(anotherDataTagAddElement);
        configElement.appendChild(dataTagUpdateElement);
        configElement.appendChild(equipmenUnitUpdateElement);
        
        validateDocument(document, CONFIGURATION_EVENT_XSD);
//        printDocument(document);
    }
    
    @Test
    public void testChangeEventReport() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, SAXException, IOException {
        ConfigurationChangeEventReport changeEventReport = new ConfigurationChangeEventReport();
        ChangeReport changeReport = new ChangeReport(1L);
        changeReport.appendError("<asd");
        changeEventReport.appendChangeReport(changeReport);
        
//        xmlFactory.setDefaultNamespace("http://timweb.cern.ch/schemas/c2mon-daq/ConfigurationReport");
        Document document = xmlFactory.createConfigurationChangeEventReportDocument(changeEventReport);
        validateDocument(document, CONFIGURATION_REPORT_XSD);
//        printDocument(document);
    }
    
    
    @Test 
    public void testEquipmentUnitAdd() throws Exception {
        EquipmentUnitAdd eqUnitAdd = createEquipmentUnitAdd();
        Document document = xmlFactory.createDocument();
        Element configurationElement = xmlFactory.createConfigurationChangeEventElement(document);
        document.appendChild(configurationElement);
        Node node = xmlFactory.createEquipmentUnitAddElement(document, eqUnitAdd);
        configurationElement.appendChild(node);
     
//        Transformer transformer = TransformerFactory.newInstance().newTransformer();
//        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//
//        //initialize StreamResult with File object to save to file
//        StreamResult result = new StreamResult(new StringWriter());
//        DOMSource source = new DOMSource(document);
//        transformer.transform(source, result);
//
//        String xmlString = result.getWriter().toString();
//        System.out.println(xmlString);
        
        
        validateDocument(document, CONFIGURATION_EVENT_XSD);
    }            
    
    
    private DataTagAdd createDataTagAdd() {
        DataTagAdd dataTagAdd = new DataTagAdd(2L, 5L, createSourceDataTag());
        return dataTagAdd;
    }

    private SourceDataTag createSourceDataTag() {
        SourceDataTag dataTag = new SourceDataTag(27L, "Test", false);
        dataTag.setDataType("Integer");
        dataTag.setMaxValue(10);
        try {
            DataTagAddress dataTagAddress = new DataTagAddress();
            dataTagAddress.setPriority(5);
            OPCHardwareAddressImpl opcHardwareAddressImpl = new OPCHardwareAddressImpl("lala", 12);
            dataTagAddress.setHardwareAddress(opcHardwareAddressImpl);
            dataTag.setAddress(dataTagAddress);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return dataTag;
    }

    private DataTagUpdate createDataTagUpdate() {
        DataTagUpdate dataTagUpdate = new DataTagUpdate(324234L, 2L, 5L);
        DataTagAddressUpdate dataTagAddressUpdate = createDataTagAddressUpdate();
        dataTagUpdate.setDataType("Integer");
        dataTagUpdate.setDataTagAddressUpdate(dataTagAddressUpdate);
        dataTagUpdate.setMaxValue(5);
        dataTagUpdate.addFieldToRemove("minValue");
        return dataTagUpdate;
    }
    
    private DataTagUpdate createDataTagUpdateExtended() {
        DataTagUpdate dataTagUpdate = new DataTagUpdateExtended(324234L, 2L, 5L);
        DataTagAddressUpdate dataTagAddressUpdate = createDataTagAddressUpdate();
        dataTagUpdate.setDataType("Integer");
        dataTagUpdate.setDataTagAddressUpdate(dataTagAddressUpdate);
        dataTagUpdate.setMaxValue(5);
        dataTagUpdate.addFieldToRemove("minValue");
        return dataTagUpdate;
    }
    
    private DataTagAddressUpdate createDataTagAddressUpdate() {
        DataTagAddressUpdate dataTagAddressUpdate = new DataTagAddressUpdate();
        dataTagAddressUpdate.setPriority(2);
        HardwareAddressUpdate hardwareAddressUpdate = createHardwareAddressUpdate();
        dataTagAddressUpdate.setHardwareAddressUpdate(hardwareAddressUpdate);
        return dataTagAddressUpdate;
    }

    private HardwareAddressUpdate createHardwareAddressUpdate() {
        HardwareAddressUpdate hardwareAddressUpdate = new HardwareAddressUpdate();
        hardwareAddressUpdate.getChangedValues().put("long", 123L);
        hardwareAddressUpdate.getChangedValues().put("string", "asd");
        return hardwareAddressUpdate;
    }
    
    private EquipmentUnitAdd createEquipmentUnitAdd() {
        EquipmentUnitAdd eqUnitAdd = new EquipmentUnitAdd();
        eqUnitAdd.setChangeId(123L);
        eqUnitAdd.setEquipmentId(100L);
        eqUnitAdd.setEquipmentUnitXml("<EquipmentUnit>test</EquipmentUnit>");
        return eqUnitAdd;        
    }

}
