package cern.c2mon.web.configviewer.service;


import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;

import javax.validation.constraints.AssertTrue;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.web.configviewer.service.AlarmService;
import cern.c2mon.web.configviewer.service.CommandService;
import cern.c2mon.web.configviewer.service.TagService;
import cern.tim.shared.common.datatag.TagQualityStatus;


@ContextConfiguration(locations = {"test-context.xml"} )
@RunWith(SpringJUnit4ClassRunner.class)
public class TagServiceTest {

  @Autowired
  TagService service;
  @Autowired
  AlarmService serviceA;
  @Autowired
  CommandService serviceC;
  @Autowired
  ConfigLoaderService serviceL;
  @Autowired
  ProcessService serviceP;
  @Autowired
  HistoryService serviceH;

  final String diamondTag = "1026868"; /* only against diamond */
  final String complexTag = "90887"; /* only in production */
  final String dataTagWithUnits = "44906";
  final String dataTagWithAlarms = "146047";
  final String dataTag = "145800";
  final String historyTag = "107202";
  final String tagConfig = "100023";

  final String configurationId_FAIL = "666666";
  final String configurationId_SUCCESS = "10000";

  final String processName = "P_GTCCHILLSU6";
  final String reallyBigProcessXml2 = "P_VENTSUI8";
  final String reallyBigProcessXml = "P_CVRVLHC02_SNT";

  @Test
  public void test() {
  }
  
  
  /**
   * This tests runs only against the production server.
   * 
  */
  @Test
  public void testDataTagHtml() throws Exception {

    String xmlConfig = service.getDataTagConfigXml(complexTag);
    System.out.println(xmlConfig);
    Assert.assertTrue(xmlConfig.contains("TagConfig"));
    String htmlConfig = service.generateDataTagConfigHtmlResponse(complexTag);
    System.out.println(htmlConfig);
    
    String xmlValue = service.getDataTagValueXml(complexTag);
    System.out.println(xmlValue);
    Assert.assertTrue(xmlValue.contains("ClientDataTag"));
    String htmlValue = service.generateDataTagValueHtmlResponse(complexTag);
    System.out.println(htmlValue);
  }  
  
  /**
   * this test only runs against the diamond server
   * @throws TagIdException 
   * @throws TransformerException 
  @Test
  public void testDiamond() throws TagIdException, TransformerException {
    
    ClientDataTagValue dataTag = service.getDataTagValue(Long.parseLong(diamondTag));
    System.out.println(dataTag.isValid());
    assertTrue(dataTag.isValid());
    
    String xmlValue = service.getDataTagValueXml(diamondTag);
    System.out.println(xmlValue);
    Assert.assertTrue(xmlValue.contains("ClientDataTag"));
    String htmlValue = service.generateDataTagValueHtmlResponse(diamondTag);
    System.out.println(htmlValue);
  }
  */
  
  @Test
  public void AlarmTagNotEmpty() {
      String tagXml;
      try {
          tagXml = serviceA.getAlarmTagXml(dataTagWithAlarms);
          System.out.println(tagXml);
          Assert.assertNotNull(tagXml);
          Assert.assertTrue(tagXml.contains("<faultCode"));
      } catch (Exception e) {
          Assert.assertTrue(false);
      }
  }

  @Test
  public void testHistory() {

    String xml = null;
    try {
      xml = serviceH.getHistoryXml(dataTagWithAlarms, 10);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Assert.assertTrue(xml.contains("HistoryTag"));
    System.out.println(xml);
    try {
      String html = serviceH.generateHtmlResponse(dataTagWithAlarms, 10);
    } catch (TagIdException e) {
      e.printStackTrace();
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }  
  
  //  @Test
  //  public void testProcessHtml() {
  //
  //    try {
  //
  //      String response = serviceP.generateHtmlResponse("P_GTCCHILLSU6");
  //    } catch (Exception e) {
  //      System.out.println(e.getMessage());
  //      e.printStackTrace();
  //      Assert.assertTrue(false);
  //    }
  //  }
  //  
  //

//  @Test
//  public void testTagQualityIsIncludedInXml() throws Exception {
//
//    ClientDataTagImpl cdt = new ClientDataTagImpl(1234L);
//    cdt.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_OUT_OF_BOUNDS, "Value is over 9000!");
//    cdt.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE, "It's down!");
//
//    cdt.toString().contains("tagQuality");
//  }  

//  @Test
//  public void DataTagValueNotEmpty() {
//    String tagXml;
//    try {
//      tagXml = service.getDataTagValueXml(dataTagWithAlarms);
//      System.out.println(tagXml);
//      Assert.assertNotNull(tagXml);
//      Assert.assertTrue(tagXml.contains("<tagValue"));
//    } catch (Exception e) {
//      Assert.assertTrue(false);
//    }
//  }

//  @Test
//  public void testProcessNames() {
//
//    Collection names;
//
//    try {
//
//      names = serviceP.getProcessNames();
//      Assert.assertNotNull(names);
//    } catch (Exception e) {
//      System.out.println(e.getMessage());
//      e.printStackTrace();
//      Assert.assertTrue(false);
//    }
//  }

  //
  //    @Test
  //    public void testProcessXmlService() {
  //        String pXml;
  //        try {
  //            pXml = serviceP.getProcessXml(processName);
  ////            System.out.println(pXml);
  //            
  //            FileWriter fstream = new FileWriter("out"+processName+".txt");
  //            BufferedWriter out = new BufferedWriter(fstream);
  //            out.write(pXml);
  //            //Close the output stream
  //            out.close();
  //            
  ////            Assert.assertNotNull(tagXml);
  ////            Assert.assertTrue(tagXml.contains("<ConfigurationReport"));
  ////            Assert.assertTrue(tagXml.contains("<UNKNOWN"));
  //        } catch (Exception e) {
  //            System.out.println(e.getMessage());
  //            e.printStackTrace();
  //            Assert.assertTrue(false);
  //        }
  //    }
  //
  //    @Test
  //    public void testReallyBigProcessXml() {
  //        String pXml;
  //        try {
  //            pXml = serviceP.getProcessXml(reallyBigProcessXml);
  //            
  //            FileWriter fstream = new FileWriter("out"+reallyBigProcessXml+".txt");
  //            BufferedWriter out = new BufferedWriter(fstream);
  //            out.write(pXml);
  ////            Assert.assertNotNull(tagXml);
  ////            Assert.assertTrue(tagXml.contains("<ConfigurationReport"));
  ////            Assert.assertTrue(tagXml.contains("<UNKNOWN"));
  //        } catch (Exception e) {
  //            System.out.println(e.getMessage());
  //            e.printStackTrace();
  //            Assert.assertTrue(false);
  //        }
  //    }

  /*
    @Test
    public void testConfigLoaderService() {
        String tagXml;
        try {
            tagXml = serviceL.getConfigurationReportXml(configurationId_SUCCESS);
            System.out.println(tagXml);
            Assert.assertNotNull(tagXml);
            Assert.assertTrue(tagXml.contains("<ConfigurationReport"));
            Assert.assertTrue(tagXml.contains("<ConfigurationElementReport"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testConfigLoaderServiceFail() {
        String tagXml;
        try {
            tagXml = serviceL.getConfigurationReportXml(configurationId_FAIL);
            System.out.println(tagXml);
            Assert.assertNotNull(tagXml);
            Assert.assertTrue(tagXml.contains("<ConfigurationReport"));
            Assert.assertTrue(tagXml.contains("<UNKNOWN"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }


    @Test
    public void DataTagConfigNotEmpty() {
        String tagXml;
        try {
            tagXml = service.getDataTagConfigXml("145800");
            System.out.println(tagXml);
            Assert.assertNotNull(tagXml);
            Assert.assertTrue(tagXml.contains("<hardwareAddress"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void CommandTagNotEmpty() {
        String tagXml;
        try {
            tagXml = serviceC.getCommandTagXml("10000");
            Assert.assertNotNull(tagXml);
            System.out.println(tagXml);
            Assert.assertTrue(tagXml.contains("<clientCommandTagImpl"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }


    @Test(expected=TagIdException.class)
    public void DataTagValueEmpty() throws Exception {
        service.getDataTagValueXml("23422200");
    }

    @Test(expected=TagIdException.class)
    public void DataTagConfigEmpty() throws Exception {
        service.getDataTagConfigXml("23422200");
    }

    @Test(expected=TagIdException.class)
    public void AlarmTagEmpty() throws Exception {
        serviceA.getAlarmTagXml("23422200");
    }

    @Test(expected=TagIdException.class)
    public void CommandTagEmpty() throws Exception {
        serviceC.getCommandTagXml("23422200");
    }

    @Test(expected=TagIdException.class)
    public void DataTagValueInvalid() throws Exception {
        service.getDataTagValueXml("xyz");
    }

    @Test(expected=TagIdException.class)
    public void DataTagConfigInvalid() throws Exception {
        service.getDataTagConfigXml("xyz");
    }

    @Test(expected=TagIdException.class)
    public void AlarmTagInvalid() throws Exception {
        service.getDataTagConfigXml("xyz");
    }

    @Test(expected=TagIdException.class)
    public void CommandTagInvalid() throws Exception {
        serviceC.getCommandTagXml("xyz");
    }
   */
}
