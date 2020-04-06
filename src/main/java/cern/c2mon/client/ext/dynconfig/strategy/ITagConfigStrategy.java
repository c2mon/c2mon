package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.config.ProcessEquipmentURIMapping;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;

/**
 * An interface for strategies on how a specific protocol or standard is handled in the C2MON
 * subscription process.
 */
public interface ITagConfigStrategy {

    /**
     * Create appropriate preliminary data tag configurations which can then be passed to the
     * C2MON server to be created within the server.
     * @return the preliminary DataTags to pass on to the C2MON server for creation.
     */
    DataTag prepareTagConfigurations();

    /**
     * Create those fields of the equipmentBuilder that are protocol-specific
     * @param mapping contains additional specification regarding the C2MON-internal equipment name and description
     * @return equipmentBuilder the equipmentBuilder to extend with protocol-specific fields
     */
    Equipment prepareEquipmentConfiguration(ProcessEquipmentURIMapping mapping);
}