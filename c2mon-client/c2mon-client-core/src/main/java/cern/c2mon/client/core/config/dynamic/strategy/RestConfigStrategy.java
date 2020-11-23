package cern.c2mon.client.core.config.dynamic.strategy;

import cern.c2mon.client.core.config.dynamic.DynConfigException;
import cern.c2mon.client.core.config.dynamic.query.QueryKey;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements a configuration strategy for DIP.
 */
public class RestConfigStrategy extends TagConfigStrategy implements ITagConfigStrategy {
    // These  Query Keys must be handled in a custom way as they must be set in the HardwareAddress Constructor.
    private static final List<QueryKey<?>> keys = new ArrayList<>();

    static {
        // also the DataTag.CreateBuilder has a method "mode". Hence the necessity to set the target class explicitly.
        QueryKey<String> mode = new QueryKey<>("mode", "GET", true);
        mode.setVerifier(s -> "GET".equalsIgnoreCase(s) || "POST".equalsIgnoreCase(s));
        mode.setTargetClass(DataTagAddress.class);

        keys.add(mode);
        keys.add(new QueryKey<>("url", null, true));
        keys.add(new QueryKey<>("getFrequency"));
        keys.add(new QueryKey<>("postFrequency"));
        keys.add(new QueryKey<>("jsonPathExpression"));
    }

    protected RestConfigStrategy(URI uri) throws DynConfigException {
        messageHandler = "cern.c2mon.daq.rest.RestMessageHandler";
        super.createQueryObj(uri, keys);
    }

    /**
     * Create appropriate preliminary data tag configurations with a @{@link cern.c2mon.shared.common.datatag.address.DIPHardwareAddress}
     * which can then be passed to the C2MON server for creation.
     * @return the preliminary DataTags to pass on to the C2MON server for creation.
     * @throws DynConfigException if a value in the query cannot be converted to the necessary class, but is mandatory.
     */
    public DataTag prepareDataTagConfigurations() throws DynConfigException {
        HardwareAddress restHardwareAddress = new HardwareAddressImpl();
        DataTag dataTag = super.toTagConfiguration(restHardwareAddress);

        HashMap<String, String> params = keys.stream().filter(k -> queryObj.contains(k)).collect(Collectors
                .toMap(QueryKey::getKeyName, o -> StringUtils.join(queryObj.get(o, String.class), ", "), (o, o2) -> o, HashMap::new));
        dataTag.getAddress().setAddressParameters(params);

        return dataTag;
    }

    @Override
    public CommandTag prepareCommandTagConfigurations() throws DynConfigException {
        HardwareAddress restHardwareAddress = new HardwareAddressImpl();
        return super.toCommandConfiguration(restHardwareAddress);
    }
}
