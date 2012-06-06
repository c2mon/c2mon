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
