package cern.c2mon.shared.daq.datatag;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import cern.c2mon.util.parser.ParserException;
import cern.c2mon.util.parser.SimpleXMLParser;

/**
 * Unit test of DataTagValueUpdate class.
 * 
 * @author Mark Brightwell
 * @author Nacho Vilches
 * 
 */
public class DataTagValueUpdateTest {
  
  /**
   * Log4j Logger for the DataTagValueUpdateTest class.
   */
  protected static final Logger LOGGER = Logger.getLogger(DataTagValueUpdateTest.class);
  
  /**
   * Test Type enum for testing different configurations (PIK, NO_PIK,...)
   */
  public enum TestType {
    NO_PIK(-1L), PIK(12345L), WRONG_PIK(67890L);
    
    /**
     * Test pik
     */
    private Long pik;

    /**
     * Set test type pik
     * 
     * @param pik Test type pik
     */
    TestType(final Long pik) {
      this.pik = pik;
    }

    /**
     * Get test type pik
     * 
     * @return Test type pik
     */
    public final Long getPik() {
      return this.pik;
    }
  }
  
  /**
   * Test Type for the current tests
   */
  TestType testType = null;
  
  /**
   * DataTagValueUpdate to turn into XML
   * 
   */
  DataTagValueUpdate dataTagValueUpdateTO = null;
  
  /**
   * DataTagValueUpdate to be filled out by info read from XML
   */
  DataTagValueUpdate dataTagValueUpdateFROM = null;
  
  /**
   * XML to be filled with data from the DataTagValueUpdate and
   * to be read by DataTagValueUpdate 
   */
  String xml = null;
  
  /**
   * DocumentBuilder
   */
  DocumentBuilder builder = null;

  /**
   * Tests parsing of an XML update message succeeds.
   * 
   * @throws ParserConfigurationException
   * @throws ParserException 
   */
  @Test
  public void testFromXml() throws ParserConfigurationException, ParserException {
    SimpleXMLParser parser = new SimpleXMLParser();
    String xmlString = "<DataTagValueUpdate process-id=\"5\">"
        + "<DataTag id=\"189981\" name=\"CLIC:CS-CCR-DIAM1:SYS.KERN.IOWAIT\" control=\"false\"><value data-type=\"Integer\">4846937</value><value-description><![CDATA[]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186655</daq-timestamp></DataTag>"
        + "<DataTag id=\"189967\" name=\"CLIC:CS-CCR-DIAM1:SYS.LOADAVG\" control=\"false\"><value data-type=\"Double\">0.66</value><value-description><![CDATA[]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186658</daq-timestamp></DataTag>"
        + "<DataTag id=\"189968\" name=\"CLIC:CS-CCR-DIAM1:SYS.KERN.LOAD\" control=\"false\"><value data-type=\"Double\">9.929612870789342</value><value-description><![CDATA[]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186660</daq-timestamp></DataTag>"
        + "<DataTag id=\"189966\" name=\"CLIC:CS-CCR-DIAM1:SYS.PID.CPU.*\" control=\"false\"><value data-type=\"Double\">166538.15987933634</value><value-description><![CDATA[/tmp/oc4j-diam, pid=15675]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186661</daq-timestamp></DataTag>"
        + "<DataTag id=\"189969\" name=\"CLIC:CS-CCR-DIAM1:SYS.MEM.INACTPCT\" control=\"false\"><value data-type=\"Double\">48.58859406515347</value><value-description><![CDATA[]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186661</daq-timestamp></DataTag>"
        + "<DataTag id=\"189980\" name=\"CLIC:CS-CCR-DIAM1:NTP.AVG\" control=\"false\"><value data-type=\"Double\">1.2715657552083334E-5</value><value-description><![CDATA[Result is based on 3 NTP servers)]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186662</daq-timestamp></DataTag>"
        + "<DataTag id=\"189975\" name=\"CLIC:CS-CCR-DIAM1:SYS.PID.CPU.{!DMNCLIC}\" control=\"false\"><value data-type=\"Double\">166538.15987933634</value><value-description><![CDATA[/tmp/oc4j-diam, pid=15675]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186662</daq-timestamp></DataTag></DataTagValueUpdate>";
    DataTagValueUpdate update = DataTagValueUpdate.fromXML(parser.parse(xmlString).getDocumentElement());
    Assert.assertNotNull(update);
    Assert.assertNotNull(update.getValues());
    Assert.assertEquals(7, update.getValues().size());
  }
  
  /**
   * Tests parsing of an XML update message succeeds with PIK
   * 
   * @throws ParserConfigurationException
   * @throws ParserException 
   */
  @Test
  public void testFromXmlWithPIK() throws ParserConfigurationException, ParserException {
    SimpleXMLParser parser = new SimpleXMLParser();
    String xmlString = "<DataTagValueUpdate process-id=\"5\" process-pik=\"" + TestType.PIK.getPik() + "\">"
        + "<DataTag id=\"189981\" name=\"CLIC:CS-CCR-DIAM1:SYS.KERN.IOWAIT\" control=\"false\"><value data-type=\"Integer\">4846937</value><value-description><![CDATA[]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186655</daq-timestamp></DataTag>"
        + "<DataTag id=\"189967\" name=\"CLIC:CS-CCR-DIAM1:SYS.LOADAVG\" control=\"false\"><value data-type=\"Double\">0.66</value><value-description><![CDATA[]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186658</daq-timestamp></DataTag>"
        + "<DataTag id=\"189968\" name=\"CLIC:CS-CCR-DIAM1:SYS.KERN.LOAD\" control=\"false\"><value data-type=\"Double\">9.929612870789342</value><value-description><![CDATA[]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186660</daq-timestamp></DataTag>"
        + "<DataTag id=\"189966\" name=\"CLIC:CS-CCR-DIAM1:SYS.PID.CPU.*\" control=\"false\"><value data-type=\"Double\">166538.15987933634</value><value-description><![CDATA[/tmp/oc4j-diam, pid=15675]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186661</daq-timestamp></DataTag>"
        + "<DataTag id=\"189969\" name=\"CLIC:CS-CCR-DIAM1:SYS.MEM.INACTPCT\" control=\"false\"><value data-type=\"Double\">48.58859406515347</value><value-description><![CDATA[]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186661</daq-timestamp></DataTag>"
        + "<DataTag id=\"189980\" name=\"CLIC:CS-CCR-DIAM1:NTP.AVG\" control=\"false\"><value data-type=\"Double\">1.2715657552083334E-5</value><value-description><![CDATA[Result is based on 3 NTP servers)]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186662</daq-timestamp></DataTag>"
        + "<DataTag id=\"189975\" name=\"CLIC:CS-CCR-DIAM1:SYS.PID.CPU.{!DMNCLIC}\" control=\"false\"><value data-type=\"Double\">166538.15987933634</value><value-description><![CDATA[/tmp/oc4j-diam, pid=15675]]></value-description><timestamp>1315378186653</timestamp><daq-timestamp>1315378186662</daq-timestamp></DataTag></DataTagValueUpdate>";
    DataTagValueUpdate update = DataTagValueUpdate.fromXML(parser.parse(xmlString).getDocumentElement());
    Assert.assertNotNull(update);
    Assert.assertNotNull(update.getValues());
    Assert.assertEquals(7, update.getValues().size());
    Assert.assertEquals(TestType.PIK.getPik(), update.processPIK);
  }
  
  /**
   * Checks whether the parser handles correctly updates with empty quality descriptions.
   * @throws ParserConfigurationException
   */
  @Test
  public void testFromXMLWithEmptyMessage() throws ParserConfigurationException {
    SimpleXMLParser parser = new SimpleXMLParser();
    String xmlString = "<DataTagValueUpdate process-id=\"4077\"><DataTag id=\"126329\" name=\"FW.L08.FDED-00072_8.1_VMA8211/1:DEF_TPS_FERM\" control=\"false\"><quality><code>4</code><desc><![CDATA[]]></desc></quality><timestamp>1350761384924</timestamp><daq-timestamp>1350761384924</daq-timestamp></DataTag></DataTagValueUpdate>";
    
    DataTagValueUpdate update = DataTagValueUpdate.fromXML(parser.parse(xmlString).getDocumentElement());
    Assert.assertNotNull(update);
    
    for (SourceDataTagValue sdt : update.getValues()) {
      Assert.assertNotNull(sdt.getQuality().getDescription());
      Assert.assertEquals("", sdt.getQuality().getDescription());
      Assert.assertEquals(4, sdt.getQuality().getQualityCode());
    }
  }
  
  @Test
  public void testToXml() throws ParserConfigurationException, ParserException {
    // We test with no PIK info
    this.testType = TestType.NO_PIK;
    
    // Send DataTagValueUpdate info to XML
    sendToXML();
    // Read DataTagValueUpdate from XML
    readFromXML();
    
    // Checks
    Assert.assertEquals(this.dataTagValueUpdateTO.processId, this.dataTagValueUpdateFROM.processId);
    Assert.assertEquals(this.dataTagValueUpdateFROM.processPIK, null);
  }
  
  @Test
  public void testToXmlWithPIK() {
    // We test with PIK info
    this.testType = TestType.PIK;
    
    // Send DataTagValueUpdate info to XML
    sendToXML();
    // Read DataTagValueUpdate from XML
    readFromXML();
    
    // Checks
    Assert.assertEquals(this.dataTagValueUpdateTO.processId, this.dataTagValueUpdateFROM.processId);
    Assert.assertEquals(this.dataTagValueUpdateTO.processPIK, this.dataTagValueUpdateFROM.processPIK);
    Assert.assertEquals(this.dataTagValueUpdateFROM.processPIK, TestType.PIK.getPik());
  }
  
  @Test
  public void testToXmlWithWrongPIK() {
    // We test with WRONG_PIK info
    this.testType = TestType.WRONG_PIK;
    
    // Send DataTagValueUpdate info to XML
    sendToXML();
    // Read DataTagValueUpdate from XML
    readFromXML();
    
    // Checks
    Assert.assertEquals(this.dataTagValueUpdateTO.processId, this.dataTagValueUpdateFROM.processId);
    Assert.assertEquals(this.dataTagValueUpdateTO.processPIK, this.dataTagValueUpdateFROM.processPIK);
    Assert.assertEquals(this.dataTagValueUpdateFROM.processPIK, TestType.WRONG_PIK.getPik());
  }
  
  /*
   * Function for sending the DataTagValueUpdate to a XMML file
   */
  public void sendToXML() {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactoryImpl.newInstance();
      factory.setIgnoringComments(true);
      factory.setValidating(false);
      this.builder = factory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      e.printStackTrace();
    }

    // Create the DataTagValueUpdate
    Long number = Long.valueOf(new Random().nextInt(10));
    switch (this.testType) {
      case NO_PIK:
        System.out.println("createXML - No PIK test with id=" + number);
        this.dataTagValueUpdateTO = new DataTagValueUpdate(number);
        break;
      case PIK:
        System.out.println("createXML - PIK test with id=" + number + " and PIK=" + TestType.PIK.getPik());
        this.dataTagValueUpdateTO = new DataTagValueUpdate(number, TestType.PIK.getPik());
        break;
      case WRONG_PIK:
        System.out.println("createXML - WRONG_PIK test with id=" + number + " and PIK=" + TestType.WRONG_PIK.getPik());
        this.dataTagValueUpdateTO = new DataTagValueUpdate(number, TestType.WRONG_PIK.getPik());
        break;
      default:
        LOGGER.error("createXML - No test type found");
        Assert.assertTrue(false);
        return;
    }
    
    // Create SourceDataTagValue values to fill the DataTagValueUpdate
    for (int j=0; j< 5; j++) {
      SourceDataTagValue sourceDataTagValue = new SourceDataTagValue(
          new Long(j), ("tag.name.of.tag." + j), false, new Boolean(false), 
          new SourceDataQuality((short)j, "everything rather OK" + j), System.currentTimeMillis(), 2, false, null,1000
          );
      sourceDataTagValue.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
      this.dataTagValueUpdateTO.addValue(sourceDataTagValue);
    }
    
    // Create XML from object
    this.xml = dataTagValueUpdateTO.toXML();
    System.out.println("createXML - xml\n:" + this.xml);
  }
  
  /*
   *  Function for reading the DataTagValueUpdate from a XMML file
   */
  public void readFromXML() {
    Document doc = null;
    
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes("US-ASCII"));
      InputSource inputSource2 = new InputSource(in);
      inputSource2.setEncoding("US-ASCII");
      inputSource2.setByteStream(in);
      doc= this.builder.parse(inputSource2);
      inputSource2= null;
      in.close();
      in=null;
    }
    catch (Exception e) {}
    
    // Read DataTagValueUpdate  back from XML
    this.dataTagValueUpdateFROM = DataTagValueUpdate.fromXML(doc.getDocumentElement());
    System.out.println(this.dataTagValueUpdateFROM.toXML());
  }
}
