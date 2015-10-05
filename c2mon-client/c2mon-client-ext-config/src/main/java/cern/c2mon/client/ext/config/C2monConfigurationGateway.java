package cern.c2mon.client.ext.config;

import cern.c2mon.client.core.C2monServiceGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
public class C2monConfigurationGateway {

  private static final String APPLICATION_SPRING_XML_PATH = "config.xml";

  private static ApplicationContext context;

  private static ConfigurationService configurationService = null;

  /**
   * @return
   */
  public static synchronized ConfigurationService getConfigurationService() {
    if (configurationService == null) {
      initialize();
    }

    return configurationService;
  }

  /**
   *
   */
  private static void initialize() {
    if (C2monServiceGateway.getApplicationContext() == null) {
      C2monServiceGateway.startC2monClientSynchronous();
    }

    if (context == null) {
      context = new ClassPathXmlApplicationContext(new String[]{APPLICATION_SPRING_XML_PATH}, C2monServiceGateway.getApplicationContext());
      configurationService = context.getBean(ConfigurationService.class);
    } else {
      log.warn("ConfigurationService is already initialized.");
    }
  }
}
