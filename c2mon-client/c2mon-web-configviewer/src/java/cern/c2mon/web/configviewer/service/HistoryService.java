package cern.c2mon.web.configviewer.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
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

import cern.c2mon.client.common.history.HistoryLoadingConfiguration;
import cern.c2mon.client.common.history.HistoryLoadingManager;
import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.exception.HistoryProviderException;
import cern.c2mon.client.common.history.exception.LoadingParameterException;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.tag.ClientCommandTagImpl;
import cern.c2mon.client.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.web.configviewer.service.ServiceGateway;
import cern.c2mon.web.configviewer.service.TagIdException;

/**
 * Alarm service providing the XML representation of a given alarm
 * */
@Service
public class HistoryService {

  /**
   * HistoryService logger
   * */
  private static Logger logger = Logger.getLogger(HistoryService.class);

  /** the path to the xslt document */
  private static final String XSLT_PATH = "/history_xslt.xsl";

  /**
   * Gateway to C2monService 
   * */
  @Autowired
  private ServiceGateway gateway;

  /**
   * Gets the XML representation of the configuration of a command
   * @param dataTagId id of the datatag
   * @param numberOfRecords number of records to look back in the history
   * @return XML representation of tag's history 
   * @throws Exception if tag was not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   * */
  public String getHistoryXml(final String dataTagId, final int numberOfRecords) throws Exception {

    return requestHistoryData(dataTagId, numberOfRecords);
  }
  
  
//  if (numberOfDays != null) {
//    // compute start date and set end date to now
//    final Calendar startCalendar = Calendar.getInstance();
//    startCalendar.add(Calendar.DATE, -numberOfDays);
//    startDate = startCalendar.getTime();
//  }
//
//  startTime = new Timestamp(startDate.getTime());
//  if (endDate == null)
//    endDate = new Date();
//  endTime = new Timestamp(endDate.getTime());
//
//  configuration.setStartTime(startTime);
//  configuration.setEndTime(endTime);

  /**
   * Initializes the chart's data.
   */
  private String requestHistoryData(final String dataTagId, final int numberOfRecords) {

    // other values that can be used //
    Integer numberOfDays;
    Date startDate;
    Date endDate;
    Timestamp startTime = null;
    Timestamp endTime = null;
    // other values that can be used //

    HistoryProvider historyProvider;
    try {
      historyProvider = C2monServiceGateway.getHistoryManager().getHistoryProviderFactory().createHistoryProvider();
    }
    catch (HistoryProviderException e) {
      logger.error("Can't load any history because a HistoryProvider cannot be created.", e);
      throw new RuntimeException("Cannot retrieve the data from the Short term log because no history provider is accessible.");
    }

    Collection<Long> dataTagIds = new ArrayList<Long>();
    dataTagIds.add(Long.parseLong(dataTagId));
    final HistoryLoadingManager loadingManager = C2monServiceGateway.getHistoryManager().createHistoryLoadingManager(historyProvider, dataTagIds);

    final HistoryLoadingConfiguration configuration = new HistoryLoadingConfiguration();
    configuration.setLoadInitialValues(true);
    configuration.setMaximumRecords(numberOfRecords);

    loadingManager.setConfiguration(configuration);
    try {
      loadingManager.beginLoading(false);
    }
    catch (LoadingParameterException e) {
      logger.error("The configurations is invalid.", e);
      throw new RuntimeException("The configuration is invalid", e);
    }

    final List<HistoryTagValueUpdate> historyValues = new ArrayList<HistoryTagValueUpdate>();
    for (final Long tagId : dataTagIds) {
      historyValues.addAll(loadingManager.getAllHistoryConverted(tagId));
    }
    
    String historyXml = "<history>";
    for (HistoryTagValueUpdate h : historyValues) {

      HistoryTagValueUpdateImpl q = (HistoryTagValueUpdateImpl) h;
      historyXml += q.getXml();
    }
    historyXml += "</history>";
    
    return historyXml;
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


  public String generateHtmlResponse(final String dataTagId, final int numberOfRecords) throws TagIdException {
    
    String xml;
    try {
      xml = getHistoryXml(dataTagId, numberOfRecords);
    } catch (Exception e) {
      throw new TagIdException(e.getMessage());
    }
    
    return transformToHtml(xml);
  }  
}
