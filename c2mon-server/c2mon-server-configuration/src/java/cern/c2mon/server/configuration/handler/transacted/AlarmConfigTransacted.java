package cern.c2mon.server.configuration.handler.transacted;

import java.util.Properties;

import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * Internal class with transacted methods.
 *  
 * @author Mark Brightwell
 *
 */
public interface AlarmConfigTransacted {

  /**
   * Creates the alarm in a transaction.
   * @param element creation details
   * @throws IllegalAccessException
   */
  void doCreateAlarm(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Updates the alarm in a transaction.
   * @param alarmId id of the alarm
   * @param properties update properties
   */
  void doUpdateAlarm(Long alarmId, Properties properties);

  /**
   * Removes the alarm in a transaction.
   * @param alarmId id of alarm
   * @param alarmReport report on removal
   */
  void doRemoveAlarm(Long alarmId, ConfigurationElementReport alarmReport);

}
