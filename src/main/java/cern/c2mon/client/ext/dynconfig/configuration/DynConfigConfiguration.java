package cern.c2mon.client.ext.dynconfig.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "c2mon.dynconfig")
public class DynConfigConfiguration {
	private List<ProcessEquipmentURIMapping> mappings;

	public List<ProcessEquipmentURIMapping> getMappings() {
		return mappings;
	}

	public void setMappings(List<ProcessEquipmentURIMapping> mappings) {
		this.mappings = mappings;
	}

}
