package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.query.QueryKey;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.address.DIPHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.DIPHardwareAddressImpl;

import java.net.URI;
import java.util.Arrays;

/**
 * Implements a configuration strategy for DIP.
 *
 */
public class DipConfigStrategy extends TagConfigStrategy implements ITagConfigStrategy {
	/**
	 * These  Query Keys must be handles specifically as they must be set in the HardwareAddress constructor.
	 */
	private static final QueryKey<String> PUBLICATION_NAME =new QueryKey<>("publicationName", null, true);
	private static final QueryKey<String> FIELD_NAME = new QueryKey<>("fieldName");
	private static final QueryKey<Integer> FIELD_INDEX = new QueryKey<>("fieldIndex", -1);

	DipConfigStrategy(URI uri) throws DynConfigException {
		messageHandler = "cern.c2mon.daq.dip.DIPMessageHandler";
		super.createQueryObj(uri, Arrays.asList(PUBLICATION_NAME, FIELD_NAME, FIELD_NAME));
	}

	@Override
	public DataTag prepareTagConfigurations() throws DynConfigException {
		DIPHardwareAddress dipHardwareAddress = new DIPHardwareAddressImpl(
				queryObj.get(PUBLICATION_NAME).get(0),
				queryObj.get(FIELD_NAME).get(0),
				queryObj.get(FIELD_INDEX, Integer.class).get(0));
		return super.toTagConfiguration(dipHardwareAddress);
	}
}
