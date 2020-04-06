package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.config.ProcessEquipmentURIMapping;
import cern.c2mon.client.ext.dynconfig.query.DipQueryObj;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.DIPHardwareAddressImpl;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Implements a configuration strategy for DIP.
 *
 */
@NoArgsConstructor
@AllArgsConstructor
public class DipConfigStrategy implements ITagConfigStrategy {

	private static final String MESSAGE_HANDLER = "cern.c2mon.daq.dip.DIPMessageHandler";
	private DipQueryObj queryObj;

	public DataTag prepareTagConfigurations() {
		DataTagAddress address = new DataTagAddress(getHardwareAddress());
		return DataTag
				.create(queryObj.getTagName(), queryObj.getDataType(), address)
				.description(queryObj.getTagDescription()).build();
	}

	/**
	 * Returns a DIP hardware address with the values contained in the queryObj.
	 * @return a C2MON hardware address with itemName defined in the queryObj.
	 */
	public HardwareAddress getHardwareAddress() {
		return new DIPHardwareAddressImpl(queryObj.getItemName());
	}

	/**
	 * Create those fields of the equipmentBuilder that are protocol-specific
 	 * @param mapping contains additional specification regarding the C2MON-internal equipment name and description
	 * @return equipmentBuilder the equipmentBuilder to extend with protocol-specific fields
	 */
	public Equipment prepareEquipmentConfiguration(ProcessEquipmentURIMapping mapping) {
		return Equipment.create(mapping.getEquipmentName(), MESSAGE_HANDLER)
				.description(mapping.getEquipmentDescription())
				.address(queryObj.getUri()).build();
	}
}
