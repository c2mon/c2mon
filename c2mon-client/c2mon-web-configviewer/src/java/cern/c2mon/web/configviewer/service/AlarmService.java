package cern.c2mon.web.configviewer.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.web.configviewer.service.ServiceGateway;
import cern.c2mon.web.configviewer.service.TagIdException;

/**
 * Alarm service providing the XML representation of a given alarm
 * */
@Service
public class AlarmService {

  /**
   * AlarmService logger
   * */
  private static Logger logger = Logger.getLogger(AlarmService.class);

  /** the path to the xslt document */
  private static final String XSLT_PATH = "/optimised_tag.xsl";

  /**
   * Gateway to C2monService 
   * */
  @Autowired
  private ServiceGateway gateway;

  /**
   * Gets the XML representation of the current value and configuration of an alarm
   * @param alarmId id of the alarm
   * @return XML representation of alarm value and configuration 
   * @throws Exception if alarm was not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   * */
  public String getAlarmTagXml(final String alarmId) throws Exception {
    try { 
      AlarmValueImpl alarm = (AlarmValueImpl) getAlarmValue(Long.parseLong(alarmId));
      if (alarm != null)
        return alarm.getXml();
      else
        throw new TagIdException("No alarm found");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid alarm id");
    }
  }

  public String generateHtmlResponse(final String alarmId) throws TagIdException {

    String xml = null;
    
    try { 
      AlarmValueImpl alarm = (AlarmValueImpl) getAlarmValue(Long.parseLong(alarmId));
      if (alarm != null)
        xml = alarm.getXml();
      else
        throw new TagIdException("No alarm found");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid alarm id");
    }

    return transformToHtml(xml);
  }

  /**
   * Transforms the xml to Html using xslt.
   * @param xml the xml
   * @return the html
   */
  private String transformToHtml(final String xml) {

    OutputStream ostream = null;

    try {

      InputStream xsltResource = getClass().getResourceAsStream(XSLT_PATH);
      Source xsltSource = new StreamSource(xsltResource);
      TransformerFactory transFact;
      Transformer trans = null;

      transFact = TransformerFactory.newInstance();

      Source xmlSource = new StreamSource(new StringReader(xml));

      trans = transFact.newTransformer(xsltSource);

      ostream = new ByteArrayOutputStream();
      trans.transform(xmlSource, new StreamResult((ostream)));

    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String result = ostream.toString();

    // a little hack to make firefox happy!
    result = result.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",""); 

    return result;
  }

  /**
   * Retrieves a alarmValue object from the service gateway tagManager
   * @param alarmId id of the alarm
   * @return alarm value
   * */
  private AlarmValue getAlarmValue(final long alarmId) {
    AlarmValue av = null;
    List<Long> alarmIds = new ArrayList<Long>();
    alarmIds.add(alarmId);
    Collection<AlarmValue> alarms = gateway.getTagManager().getAlarms(alarmIds); 
    //tagManager.getAlarms(alarmIds);
    Iterator<AlarmValue> it = alarms.iterator();
    if (it.hasNext()) {
      av = it.next();
    } 
    logger.debug("Alarm fetch for alarm " + alarmId + ": " + (av == null ? "NULL" : "SUCCESS"));
    return av;
  }

}
