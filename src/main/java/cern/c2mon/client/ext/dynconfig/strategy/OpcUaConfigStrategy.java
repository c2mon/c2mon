package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.query.QueryKey;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;

import java.net.URI;
import java.util.Arrays;

/**
 * Implements a configuration strategy for OPC UA.
 */
public class OpcUaConfigStrategy extends TagConfigStrategy implements ITagConfigStrategy {

	private static final QueryKey<String> ITEM_NAME = new QueryKey<>("itemName", null, true);
	private static final QueryKey<Integer> COMMAND_PULSE = new QueryKey<>("commandPulseLength");

	OpcUaConfigStrategy(URI uri) throws DynConfigException {
		messageHandler = "cern.c2mon.daq.opcua.OPCUAMessageHandler";
		super.createQueryObj(uri, Arrays.asList(ITEM_NAME, COMMAND_PULSE));
	}

	/**
	 * Create appropriate preliminary data tag configurations with a @{@link cern.c2mon.shared.common.datatag.address.OPCHardwareAddress}
	 * which can then be passed to the C2MON server for creation.
	 * @throws DynConfigException if a value in the query cannot be converted to the necessary class, but is mandatory.
	 * @return the preliminary DataTags to pass on to the C2MON server for creation.
	 */
	public DataTag prepareTagConfigurations() throws DynConfigException {
		OPCHardwareAddressImpl hwAddr = (queryObj.contains(COMMAND_PULSE))
				? new OPCHardwareAddressImpl(queryObj.get(ITEM_NAME).get(0), queryObj.get(COMMAND_PULSE, Integer.class).get(0))
				: new OPCHardwareAddressImpl(queryObj.get(ITEM_NAME).get(0));
		queryObj.applyQueryPropertiesTo(hwAddr);
		return super.toTagConfiguration(hwAddr);
	}

}
