package cern.c2mon.server.configuration.handler;

import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

import java.util.Properties;

public interface BaseConfigHandler<T> {

  /**
   * Creates a new Cacheable object in the C2MON server.
   *
   * @param element element with configuration details
   */
  T create(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Updates the Cacheable object in the C2MON server.
   *
   * @param id         id of Cacheable to update
   * @param properties reconfiguration details
   */
  T update(Long id, Properties properties) ;

  /**
   * Removes a Cacheable from C2MON server.
   *
   * @param id     the Alarm id
   * @param report the report on this action (is passed as cascading
   *               actions may need to add subreports)
   */
  T remove(Long id, ConfigurationElementReport report);
}
