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
package cern.c2mon.client.common.listener;

import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;

/**
 * Receives <code>ClientRequestProgressReport</code> 
 * that are used to indicate the progress of a<code>ClientRequest</code>
 * and <code>ClientRequestErrorReport</code>
 * that are used to indicate whether the <code>ClientRequest</code>
 * was executed successfully or not in the server side).
 *
 * @author ekoufaki
 */
public interface ClientRequestReportListener  {

  /**
   * @param progressReport indicates the progress of the <code>ClientRequest</code>
   */
  void onProgressReportReceived(final ClientRequestProgressReport progressReport);
  
  /**
   * @param errorReport indicates whether the <code>ClientRequest</code>
   * was executed successfully or not in the server side
   */
  void onErrorReportReceived(final ClientRequestErrorReport errorReport);
}
