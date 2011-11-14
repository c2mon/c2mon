package cern.c2mon.web.configviewer.service;


import org.junit.Test;
import org.junit.Assert;

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
    
    @Test
    public void DataTagValueNotEmpty() {
        String tagXml;
        try {
            tagXml = service.getDataTagValueXml("145800");
            System.out.println(tagXml);
            Assert.assertNotNull(tagXml);
            Assert.assertTrue(tagXml.contains("<tagValue"));
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
    public void AlarmTagNotEmpty() {
        String tagXml;
        try {
            tagXml = serviceA.getAlarmTagXml("144849");
            System.out.println(tagXml);
            Assert.assertNotNull(tagXml);
            Assert.assertTrue(tagXml.contains("<faultCode"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void CommandTagNotEmpty() {
        String tagXml;
        try {
            tagXml = serviceC.getCommandTagXml("118329");
            Assert.assertNotNull(tagXml);
            System.out.println(tagXml);
            //Assert.assertTrue(tagXml.contains("<faultCode"));
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
    
}
