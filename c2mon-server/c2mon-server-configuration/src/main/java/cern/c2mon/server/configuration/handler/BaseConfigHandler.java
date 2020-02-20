package cern.c2mon.server.configuration.handler;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Properties;

@EnableTransactionManagement(proxyTargetClass = true)
public interface BaseConfigHandler {

  /**
   * Creates a new Cacheable object in the C2MON server.
   *
   * @param element element with configuration details
   */
  @Transactional(value = "chainedCacheTransactionManager", propagation = Propagation.REQUIRED)
  List<ProcessChange> create(ConfigurationElement element);

  /**
   * Updates the Cacheable object in the C2MON server.
   *
   * @param id         id of Cacheable to update
   * @param properties reconfiguration details
   */
  @Transactional(value = "chainedCacheTransactionManager", propagation = Propagation.REQUIRED)
  List<ProcessChange> update(Long id, Properties properties);

  /**
   * Removes a Cacheable from C2MON server.
   *
   * @param id     the Alarm id
   * @param report the report on this action (is passed as cascading
   *               actions may need to add subreports)
   */
  @Transactional(value = "chainedCacheTransactionManager", propagation = Propagation.REQUIRED)
  List<ProcessChange> remove(Long id, ConfigurationElementReport report);
}
