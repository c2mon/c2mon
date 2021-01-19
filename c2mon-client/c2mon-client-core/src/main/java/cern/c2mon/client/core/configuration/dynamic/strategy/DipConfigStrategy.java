package cern.c2mon.client.core.configuration.dynamic.strategy;

import cern.c2mon.client.core.configuration.dynamic.DynConfigException;
import cern.c2mon.client.core.configuration.dynamic.query.IQueryObj;
import cern.c2mon.client.core.configuration.dynamic.query.QueryKey;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.address.DIPHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.DIPHardwareAddressImpl;

import java.net.URI;
import java.util.Arrays;

/**
 * Implements a configuration strategy for DIP.
 */
public class DipConfigStrategy extends TagConfigStrategy implements ITagConfigStrategy {
    /**
     * These  Query Keys must be handled in a custom way as they must be set in the HardwareAddress constructor.
     */
    private static final QueryKey<String> PUBLICATION_NAME = new QueryKey<>("publicationName", null, true);
    private static final QueryKey<String> FIELD_NAME = new QueryKey<>("fieldName");
    private static final QueryKey<Integer> FIELD_INDEX = new QueryKey<>("fieldIndex", -1);

    protected DipConfigStrategy(URI uri) throws DynConfigException {
        messageHandler = "cern.c2mon.daq.dip.DIPMessageHandler";
        super.createQueryObj(uri, Arrays.asList(PUBLICATION_NAME, FIELD_NAME, FIELD_NAME));
    }

    @Override
    public DataTag prepareDataTagConfigurations() throws DynConfigException {
        return super.toTagConfiguration(getDipHardwareAddress(queryObj));
    }

    @Override
    public CommandTag prepareCommandTagConfigurations() {
        return super.toCommandConfiguration(getDipHardwareAddress(queryObj));
    }

    private DIPHardwareAddress getDipHardwareAddress(IQueryObj queryObj) {
        return new DIPHardwareAddressImpl(
                queryObj.get(PUBLICATION_NAME).get(0),
                queryObj.get(FIELD_NAME).get(0),
                queryObj.get(FIELD_INDEX, Integer.class).get(0));
    }
}
