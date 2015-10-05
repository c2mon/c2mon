package cern.c2mon.client.ext.config.impl;

import cern.c2mon.client.ext.config.ConfigurationService;
import cern.c2mon.client.ext.config.request.ConfigurationRequestSender;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.configuration.Configuration;
import cern.c2mon.shared.client.configuration.configuration.ConfigurationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Justin Lewis Salmon
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

  @Autowired
  private ConfigurationRequestSender requestSender;

  @Override
  public ConfigurationReport applyConfiguration(Configuration configuration, ConfigurationListener listener) {
    return requestSender.applyConfiguration(configuration, listener);
  }
}
