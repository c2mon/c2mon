/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.web.configviewer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   */
  private static Logger logger = LoggerFactory.getLogger(ReportHandler.class);
  
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
