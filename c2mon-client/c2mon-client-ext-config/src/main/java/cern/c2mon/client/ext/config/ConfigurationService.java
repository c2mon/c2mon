package cern.c2mon.client.ext.config;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.configuration.Configuration;
import cern.c2mon.shared.client.configuration.configuration.ConfigurationListener;

/**
 * @author Justin Lewis Salmon
 */
public interface ConfigurationService {

  /**
   * @param configuration
   * @param listener
   *
   * @return
   */
  ConfigurationReport applyConfiguration(Configuration configuration, ConfigurationListener listener);
}
