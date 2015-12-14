package cern.c2mon.server.eslog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Holds the configuration for the ElasticSearch cluster.
 * es.port
 * es.host
 * es.cluster
 * es.node.name
 * @author Alban Marguet.
 */

@Configuration
@PropertySource("file:${c2mon.properties.location}")
public class ElasticSearchConfig {

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}