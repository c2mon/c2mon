package cern.c2mon.cache.impl.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Ignite module properties
 *
 * @author Tiago Oliveira
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.ignite")
public class IgniteProperties {

    /**
     * Enable/Disable the initiation of Embedded Ignite instance
     */
    private boolean embedded = true;

    /**
     * List of addresses pointing to the Ignite cluster
     */
    private List<String> addresses = Arrays.asList("localhost", "localhost:47500..47509");
}
