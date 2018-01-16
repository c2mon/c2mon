package cern.c2mon.client.ext.dynconfig.strategy;

import java.net.URI;
import java.util.Collection;
import java.util.function.Predicate;

import org.springframework.util.MultiValueMap;

import cern.c2mon.client.ext.dynconfig.configuration.ProcessEquipmentURIMapping;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;

public interface ITagConfigurationMapping extends Predicate<URI> {

	MultiValueMap<String, DataTag> getConfigurations(ProcessEquipmentURIMapping mapping, Collection<URI> uris);
	
}