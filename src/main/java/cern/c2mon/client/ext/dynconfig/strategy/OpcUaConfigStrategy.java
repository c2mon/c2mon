package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.config.ProcessEquipmentURIMapping;
import cern.c2mon.client.ext.dynconfig.query.OpcUaQueryObj;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.OPCCommandHardwareAddress;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress.ADDRESS_TYPE;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Implements a configuration strategy for OPC UA.
 */
@NoArgsConstructor
@AllArgsConstructor
public class OpcUaConfigStrategy implements ITagConfigStrategy {

	private static  final String MESSAGE_HANDLER = "cern.c2mon.daq.opcua.OPCUAMessageHandler";
	private OpcUaQueryObj queryObj;


	public DataTag prepareTagConfigurations() {
		DataTagAddress address = new DataTagAddress(getHardwareAddress());
		return DataTag
				.create(queryObj.getTagName(), queryObj.getDataType(), address)
				.description(queryObj.getTagDescription()).build();
	}
	/**
	 * Returns an OPC hardware address with the values contained in the queryObj.
	 * @return a C2MON hardware address with itemName and Namespace as defined in the queryObj. The hardware address is always of type String.
	 */
	public HardwareAddress getHardwareAddress() {
		Integer commandPulseLength = queryObj.getCommandPulseLength();
		OPCHardwareAddressImpl hwAddr = (commandPulseLength != null)
				? new OPCHardwareAddressImpl(queryObj.getItemName(), commandPulseLength)
				: new OPCHardwareAddressImpl(queryObj.getItemName());
		hwAddr.setAddressType(ADDRESS_TYPE.STRING);
		hwAddr.setNamespace(queryObj.getNamespace());
		OPCCommandHardwareAddress.COMMAND_TYPE commandType = queryObj.getCommandType();
		if (commandType != null) {
			hwAddr.setCommandType(commandType);
		}
		return  hwAddr;
	}

	/**
	 * Create those fields of the equipmentBuilder that are protocol-specific.
	 * @param mapping contains additional specification regading the C2MON-internal equipment name and description
	 * @return equipmentBuilder the equipmentBuilder to extend with protocol-specific fields
	 */
	public Equipment prepareEquipmentConfiguration(ProcessEquipmentURIMapping mapping) {
		return Equipment.create(mapping.getEquipmentName(), MESSAGE_HANDLER)
				.description(mapping.getEquipmentDescription())
				.address(queryObj.getUri()).build();
	}

}
