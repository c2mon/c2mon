package cern.c2mon.client.core.configuration.dynamic.strategy;

import cern.c2mon.client.core.config.C2monClientDynConfigProperties.ProcessEquipmentURIMapping;
import cern.c2mon.client.core.configuration.dynamic.DynConfigException;
import cern.c2mon.client.core.configuration.dynamic.query.IQueryObj;
import cern.c2mon.client.core.configuration.dynamic.query.QueryKey;
import cern.c2mon.client.core.configuration.dynamic.query.QueryObj;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A common superclass to the protocol-specific implementation classes of {@link ITagConfigStrategy}. This class handles
 * logic common across all strategies such as creating the IQueryObj, DataTag and Equipment objects with proper
 * parameter values.
 */
@Slf4j
@NoArgsConstructor
public abstract class TagConfigStrategy {
    public static final int MAX_COMMAND_TAG_NAME_LENGTH = 60;

    public static final QueryKey<String> TAG_NAME = new QueryKey<>("tagName");
    public static final QueryKey<String> TAG_TYPE = new QueryKey<>("tagType");
    private static final QueryKey<String> TAG_DESCRIPTION = new QueryKey<>("description", "dynamically configured tag", false);
    private static final QueryKey<Class<?>> DATA_TYPE = new QueryKey<>("dataType", Object.class, false);
    /**
     * CommandTag Keys
     **/
    private static final QueryKey<Integer> CLIENT_TIMEOUT = new QueryKey<>("clientTimeout", 5000);
    private static final QueryKey<Integer> EXEC_TIMEOUT = new QueryKey<>("execTimeout", 5000);
    private static final QueryKey<Integer> SOURCE_TIMEOUT = new QueryKey<>("sourceTimeout", 5000);
    private static final QueryKey<Integer> SOURCE_RETRIES = new QueryKey<>("sourceRetries", 0);
    private static final QueryKey<String> RBAC_CLASS = new QueryKey<>("rbacClass", "no class configured");
    private static final QueryKey<String> RBAC_DEVICE = new QueryKey<>("rbacDevice", "no device configured");
    private static final QueryKey<String> RBAC_PROPERTY = new QueryKey<>("rbacProperty", "no property configured");

    protected String messageHandler;
    protected IQueryObj queryObj;

    /**
     * Check whether a regular expression matches the strategy including the parameters parsed from the query URI
     * @param pattern a regular expression
     * @return whether of not the pattern matches the strategy including the parameters parsed from the query URI
     */
    public boolean matches(String pattern) {
        return queryObj.matches(pattern);
    }

    /**
     * Create the equipment configuration which can then be passed to the C2MON server
     * @param mapping contains additional specification regarding the C2MON-internal equipment name and description
     * @return equipmentBuilder the equipmentBuilder to extend with protocol-specific fields
     * @throws DynConfigException if the equipment address passed through the original query is malformed
     */
    public Equipment prepareEquipmentConfiguration(ProcessEquipmentURIMapping mapping) throws DynConfigException {
        Equipment.CreateBuilder builder = Equipment.create(mapping.getEquipmentName(), messageHandler)
                .description(mapping.getEquipmentDescription())
                .address(queryObj.getUriWithoutParams());
        return builder.build();
    }

    protected void createQueryObj(URI uri, Collection<? extends QueryKey<?>> protocolKeys) throws DynConfigException {
        List<QueryKey<?>> keys = Stream.concat(protocolKeys.stream(), Stream.of(TAG_NAME, TAG_DESCRIPTION, DATA_TYPE))
                .collect(Collectors.toList());
        this.queryObj = new QueryObj(uri, keys);
    }

    protected CommandTag toCommandConfiguration(HardwareAddress hwAddress) {
        /* Parameter "name" of commandTag must be 1 to 60 characters long (see cern.c2mon.server.cache.command.CommandTagFacadeImpl.CommandTagFacadeImpl) */
        return CommandTag.create(
                StringUtils.left(queryObj.get(TAG_NAME).get(0), MAX_COMMAND_TAG_NAME_LENGTH),
                queryObj.get(DATA_TYPE, Class.class).get(0),
                hwAddress,
                queryObj.get(CLIENT_TIMEOUT, Integer.class).get(0),
                queryObj.get(EXEC_TIMEOUT, Integer.class).get(0),
                queryObj.get(SOURCE_TIMEOUT, Integer.class).get(0),
                queryObj.get(SOURCE_RETRIES, Integer.class).get(0),
                queryObj.get(RBAC_CLASS).get(0),
                queryObj.get(RBAC_DEVICE).get(0),
                queryObj.get(RBAC_PROPERTY).get(0)).build();
    }

    protected DataTag toTagConfiguration(HardwareAddress hwAddress) throws DynConfigException {
        DataTagAddress address = new DataTagAddress(hwAddress);
        queryObj.applyQueryPropertiesTo(address);

        DataTag.CreateBuilder builder = DataTag
                .create(queryObj.get(TAG_NAME).get(0), queryObj.get(DATA_TYPE, Class.class).get(0), address)
                .description(StringUtils.join(queryObj.get(TAG_DESCRIPTION), ", "));
        queryObj.applyQueryPropertiesTo(builder);
        return builder.build();
    }

    /**
     * Those Query Keys that must be especially handled by the strategies either through value conversions or
     * nonstandard method calls
     */

    public enum TagType {DATA, COMMAND}
}
