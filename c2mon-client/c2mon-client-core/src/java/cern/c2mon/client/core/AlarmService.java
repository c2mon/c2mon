package cern.c2mon.client.core;

import java.util.Collection;

import javax.jms.JMSException;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

public interface AlarmService {

  /**
   * Registers an <code>AlarmListener</code> to the <code>TagManager</code>.
   * @param listener The listener to be registered
   * @throws JMSException
   */
  void addAlarmListener(AlarmListener listener) throws JMSException;
  
  /**
   * Returns an {@link AlarmValue} object for every valid id on the list.
   * The values are fetched from the server.
   * However, in case of a connection error or an unknown tag id the corresponding
   * tag might be missing.
   *
   * @param alarmIds A collection of alarm id's
   * @return A collection of all <code>AlarmValue</code> objects
   */
  Collection<AlarmValue> getAlarms(final Collection<Long> alarmIds);
  
  /**
   * Returns an {@link AlarmValue} object for every active alarm found
   * in the server.
   *
   * @return A collection of all active <code>AlarmValue</code> objects
   */
  Collection<AlarmValue> getAllActiveAlarms();
  
  /**
   * Unregisters an <code>AlarmListener</code> from the <code>TagManager</code>.
   * @param listener The listener to be unregistered
   * @throws JMSException
   */
  void removeAlarmListener(AlarmListener listener) throws JMSException;
}
