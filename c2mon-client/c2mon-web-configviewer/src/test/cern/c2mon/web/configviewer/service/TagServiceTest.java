package cern.c2mon.web.configviewer.service;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


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
  final String processXmlWithCommandIds = "P_SSHCTRL01";
  final String reallyBigProcessXml2 = "P_VENTSUI8";
  final String reallyBigProcessXml = "P_CVRVLHC02_SNT";

  @Test
  public void test() {
  }


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
}
