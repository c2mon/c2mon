package cern.c2mon.cache.impl;

import cern.c2mon.server.common.util.KotlinAPIs;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cern.c2mon.cache.impl.C2monCacheProperties.METRICS_LOG_FREQUENCY;

/**
 * Configures the entire ignite instance
 */
@Configuration
public class IgniteConfigurationFactory {

  @Value("${c2mon.server.cache.k8s.namespace}")
  private String k8sNamespace;

  @Value("${c2mon.server.cache.k8s.enabled}")
  private boolean k8sEnabled;

  @Bean
  public IgniteConfiguration defaultConfiguration() {
    IgniteConfiguration config = (k8sEnabled) ? igniteKubernetesConfiguration() : new IgniteConfiguration();

    config.setGridLogger(new Slf4jLogger());

    config.setMetricsLogFrequency(METRICS_LOG_FREQUENCY);

    return config;
  }

  private IgniteConfiguration igniteKubernetesConfiguration() {
    return KotlinAPIs.apply(new IgniteConfiguration(), config -> {
      TcpDiscoveryKubernetesIpFinder k8sIpFinder = new TcpDiscoveryKubernetesIpFinder();
      k8sIpFinder.setNamespace(k8sNamespace);

      config.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(k8sIpFinder));
    });
  }
}
