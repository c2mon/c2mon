package cern.c2mon.client.ext.dynconfig.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * The Configuration class of the DynConfig module. Specifies the processes and equipments that a Tag will be created for
 * if none exists.
 */
@Configuration
@ConfigurationProperties(prefix = "dynconfig")
@Data
public class DynConfiguration {
    /**
     * A list of {@link ProcessEquipmentURIMapping} object which map a regular expression to a specific C2MON Process and Equipment.
     */
    private List<ProcessEquipmentURIMapping> mappings = new ArrayList<>();

}
