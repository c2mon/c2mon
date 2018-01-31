package cern.c2mon.client.ext.dynconfig.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "c2mon.dynconfig")
@Data
public class DynConfigConfiguration {
	public List<ProcessEquipmentURIMapping> mappings;
	
}
