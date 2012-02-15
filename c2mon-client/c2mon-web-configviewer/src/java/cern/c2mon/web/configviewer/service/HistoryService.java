package cern.c2mon.web.configviewer.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

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
import cern.c2mon.client.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.web.configviewer.util.XsltTransformUtility;

/**
 * HistoryService providing the XML representation for the history of a given tag.
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

  public String generateHtmlResponse(final String dataTagId, final int numberOfRecords) 
    throws TagIdException, TransformerException {

    String xml;
    try {
      xml = getHistoryXml(dataTagId, numberOfRecords);
    } catch (Exception e) {
      throw new TagIdException(e.getMessage());
    }

    String html = null;

    try {
      html = XsltTransformUtility.performXsltTransformation(xml, XSLT_PATH);
    } catch (TransformerException e) {
      logger.error("Error while performing xslt transformation.");
      throw new TransformerException("Error while performing xslt transformation.");
    }

    return html;
  }  
}
