package cern.c2mon.web.configviewer.util;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;

/**
 * In case of long running requests,
 * progress reports are received about the current status of the request.
 * 
 * These reports are stored here.
 * @author ekoufaki
 */
public class ReportHandler implements ClientRequestReportListener {

  /** The id of this request */
  private final Long id;
  
  /** The most update-to-date Progress Report received for this request. */
  private ClientRequestProgressReport lastProgressReportReceived;
  
  /** If an error report is received for this request */
  private ClientRequestErrorReport errorReport;
  
  /**
   * ReportHandler logger
   * */
  private static Logger logger = Logger.getLogger(ReportHandler.class);
  
  public ReportHandler(final Long id) {
    
    this.id = id;
  }
  
  @Override
  public void onErrorReportReceived(final ClientRequestErrorReport report) {
    
    logger.info("onErrorReportReceived for Request with id:" + id);
    errorReport = report;
  }

  @Override
  public void onProgressReportReceived(final ClientRequestProgressReport progressReport) {
    
    logger.info("onProgressReportReceived for Request with id:" + id);
    lastProgressReportReceived = progressReport;
  }  
  
  /**
   * @return The most update-to-date 
   * Progress Report received for this request
   */
  public ClientRequestProgressReport getProgressReport() {
    return lastProgressReportReceived;
  }
  
  /**
   * @return an error report, if one 
   * was received for this request
   */
  public ClientRequestErrorReport getErrorReport() {
    return errorReport;
  }
}
