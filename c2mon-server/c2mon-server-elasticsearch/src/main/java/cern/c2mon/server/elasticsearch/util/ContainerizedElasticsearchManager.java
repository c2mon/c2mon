package cern.c2mon.server.elasticsearch.util;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClientType;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.time.Duration;

public final class ContainerizedElasticsearchManager {

  private static FixedHostPortGenericContainer<?> esContainer;

  private ContainerizedElasticsearchManager() {}

  public static void start(ElasticsearchProperties properties) {
    synchronized (ContainerizedElasticsearchManager.class) {
      if (esContainer == null) {
        esContainer = new FixedHostPortGenericContainer<>("docker.elastic.co/elasticsearch/elasticsearch:" + properties.getVersion())
          .withEnv("discovery.type", "single-node")
          .withFixedExposedPort(
            properties.getPort(),
            ElasticsearchClientType.REST.getDefaultPort()
          )
          .waitingFor(
            new HttpWaitStrategy()
              .forPort(ElasticsearchClientType.REST.getDefaultPort())
              .forStatusCodeMatching(res -> res == 200 || res == 401)
          )
          .withStartupTimeout(Duration.ofMinutes(1L));

        esContainer.start();
      }
    }
  }

  public static void stop() {
    synchronized (ContainerizedElasticsearchManager.class) {
      if (esContainer != null) {
        esContainer.stop();
      }
    }
  }
}
