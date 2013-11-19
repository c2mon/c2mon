package cern.c2mon.shared.util.buffer;

import java.util.EventListener;

/**
 *  SynchroBuffer listener interface.
 * @author F.Calderini
 */
public interface SynchroBufferListener extends EventListener {

  /** Callback method. Called with respect to the window size management properties. The callback is executed
   * within a single thread of execution.
   * @param event the pulled objects event
   * @throws PullException if the pull action failed
   */
  public void pull(PullEvent event) throws PullException;
}