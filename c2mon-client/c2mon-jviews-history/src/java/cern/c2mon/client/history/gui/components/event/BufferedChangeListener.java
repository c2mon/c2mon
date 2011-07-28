package cern.c2mon.client.history.gui.components.event;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Defines an object which listens for ChangeEvents.
 * 
 * 
 * @author vdeila
 */
public interface BufferedChangeListener extends ChangeListener {
  
  /**
   * Invoked when someone tries to change the value to more than is loaded. The
   * value is forced to the currently buffered value, then the listeners is
   * called
   * 
   * @param e
   *          a ChangeEvent object
   */
  void valueForced(final ChangeEvent e);
  
}
