package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.query.QueryKey;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Arrays;

/**
 * Implements a configuration strategy for OPC UA.
 */
@Slf4j
public class OpcUaConfigStrategy extends TagConfigStrategy implements ITagConfigStrategy {

    private static final QueryKey<String> ITEM_NAME = new QueryKey<>("itemName", null, true);
    private static final QueryKey<Integer> COMMAND_PULSE = new QueryKey<>("commandPulseLength");
    private static final QueryKey<String> ADDRESS_TYPE = new QueryKey<>("addressType", "STRING", false);

    /**
     * Creates a new configuration strategy addressing the cern.c2mon.daq.opcua.OPCUAMessageHandler.
     * @param uri The request URI specifying the Tags to configure
     * @throws DynConfigException if the uri lacks required keys.
     */
    public OpcUaConfigStrategy(URI uri) throws DynConfigException {
        messageHandler = "cern.c2mon.daq.opcua.OPCUAMessageHandler";
        super.createQueryObj(uri, Arrays.asList(ITEM_NAME, COMMAND_PULSE));
    }

    /**
     * Create appropriate preliminary data tag configurations with a @{@link OPCHardwareAddressImpl} which can then be
     * passed to the C2MON server for creation.
     * @return the preliminary DataTags to pass on to the C2MON server for creation.
     * @throws DynConfigException if a value in the query cannot be converted to the necessary class, but is mandatory.
     */
    public DataTag prepareDataTagConfigurations() throws DynConfigException {
        OPCHardwareAddressImpl hwAddr = new OPCHardwareAddressImpl(queryObj.get(ITEM_NAME).get(0));
        applyAddressType(hwAddr);
        queryObj.applyQueryPropertiesTo(hwAddr);
        return super.toTagConfiguration(hwAddr);
    }

    /**
     * Create appropriate preliminary command tag configurations with a @{@link OPCHardwareAddressImpl} which can then
     * be passed to the C2MON server for creation.
     * @return the preliminary CommandTags to pass on to the C2MON server for creation.
     * @throws DynConfigException if a value in the query cannot be converted to the necessary class, but is mandatory.
     */
    public CommandTag prepareCommandTagConfigurations() throws DynConfigException {
        OPCHardwareAddressImpl hwAddr = (queryObj.contains(COMMAND_PULSE))
                ? new OPCHardwareAddressImpl(queryObj.get(ITEM_NAME).get(0), queryObj.get(COMMAND_PULSE, Integer.class).get(0))
                : new OPCHardwareAddressImpl(queryObj.get(ITEM_NAME).get(0));
        applyAddressType(hwAddr);
        queryObj.applyQueryPropertiesTo(hwAddr);
        return super.toCommandConfiguration(hwAddr);
    }

    private void applyAddressType(OPCHardwareAddressImpl hwAddr) {
        if (queryObj.contains(ADDRESS_TYPE)) {
            hwAddr.setAddressType(OPCHardwareAddress.ADDRESS_TYPE.valueOf(queryObj.get(ADDRESS_TYPE, String.class).get(0)));
            log.info("Set addressType to {}", hwAddr.getAddressType());
        }
    }

}
