package cern.c2mon.client.core.service;

import java.util.ArrayList;
import java.util.Collection;

import javax.jms.JMSException;

import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.ConfigurationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.ConfigurationService;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.tag.TagConfig;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

  /**
   * Provides methods for requesting tag information from the C2MON server
   */
  private final RequestHandler clientRequestHandler;

  private ConfigurationRequestSender configurationRequestSender;

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   *
   * @param requestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected ConfigurationServiceImpl(final RequestHandler requestHandler, final ConfigurationRequestSender configurationRequestSender) {
    this.clientRequestHandler = requestHandler;
    this.configurationRequestSender = configurationRequestSender;
  }

  @Override
  public ConfigurationReport applyConfiguration(final Long configurationId) {
    return clientRequestHandler.applyConfiguration(configurationId);
  }

  @Override
  public ConfigurationReport applyConfiguration(Long configurationId, ClientRequestReportListener reportListener) {
    return clientRequestHandler.applyConfiguration(configurationId, reportListener);
  }

  @Override
  public ConfigurationReport applyConfiguration(Configuration configuration, ConfigurationListener listener) {
    return configurationRequestSender.applyConfiguration(configuration, listener);
  }

  @Override
  public Collection<ConfigurationReportHeader> getConfigurationReports() {
    try {
      return clientRequestHandler.getConfigurationReports();
    } catch (JMSException e) {
      log.error("getConfigurationReports() - JMS connection lost -> Could not retrieve configuration reports from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<ConfigurationReport> getConfigurationReports(Long id) {
    try {
      return clientRequestHandler.getConfigurationReports(id);
    } catch (JMSException e) {
      log.error("getConfigurationReports() - JMS connection lost -> Could not retrieve configuration reports from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<ProcessNameResponse> getProcessNames() {

    try {
      return clientRequestHandler.getProcessNames();
    } catch (JMSException e) {
      log.error("getProcessNames() - JMS connection lost -> Could not retrieve process names from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Collection<TagConfig> getTagConfigurations(final Collection<Long> tagIds) {

    try {
      // no cache for Tag Configurations => fetch them from the server
      return clientRequestHandler.requestTagConfigurations(tagIds);
    } catch (JMSException e) {
      log.error("getTagConfigurations() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return new ArrayList<>();
  }

  @Override
  public String getProcessXml(final String processName) {

    try {
      return clientRequestHandler.getProcessXml(processName);
    } catch (JMSException e) {
      log.error("getProcessXml() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return null;
  }
}
