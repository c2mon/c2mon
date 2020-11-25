package cern.c2mon.client.core.config.dynamic.strategy;

import cern.c2mon.client.core.config.dynamic.DynConfigException;
import cern.c2mon.client.core.config.dynamic.C2monClientDynConfigProperties.ProcessEquipmentURIMapping;
import cern.c2mon.client.core.config.dynamic.Protocols;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;

import java.net.URI;

import static cern.c2mon.client.core.config.dynamic.DynConfigException.Context.MISSING_SCHEME;
import static cern.c2mon.client.core.config.dynamic.DynConfigException.Context.UNSUPPORTED_SCHEME;
import static cern.c2mon.client.core.config.dynamic.Protocols.getEnumForScheme;

/**
 * An interface for strategies on how a specific protocol or standard is handled in the C2MON subscription process.
 */
public interface ITagConfigStrategy {

    /**
     * Return the protocol specific factory appropriate for the scheme.
     * @param uri uri must contain at least the scheme and authority, so the address where the equipment is reachable.
     * @return A concrete implementation of a {@link ITagConfigStrategy} to fit the scheme of the uri
     * @throws DynConfigException is thrown is the scheme is not supported or the URI does not contain mandatory query
     *                            keys.
     */
    static ITagConfigStrategy of(URI uri) throws DynConfigException {
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new DynConfigException(MISSING_SCHEME);
        }
        switch (getEnumForScheme(scheme)) {
            case PROTOCOL_OPCUA:
                return new OpcUaConfigStrategy(uri);
            case PROTOCOL_DIP:
                return new DipConfigStrategy(uri);
            case PROTOCOL_REST:
                return new RestConfigStrategy(uri);
            default:
                throw new DynConfigException(UNSUPPORTED_SCHEME, scheme);
        }
    }

    /**
     * Create appropriate preliminary data tag configurations which can then be passed to the C2MON server for
     * creation.
     * @return the preliminary DataTags to pass on to the C2MON server for creation.
     * @throws DynConfigException if a value in the query cannot be converted to the necessary class, but is mandatory
     */
    DataTag prepareDataTagConfigurations() throws DynConfigException;

    /**
     * Create appropriate preliminary command tag configurations which can then be passed to the C2MON server for
     * creation.
     * @return the preliminary CommandTags to pass on to the C2MON server for creation.
     * @throws DynConfigException if a value in the query cannot be converted to the necessary class, but is mandatory
     */
    CommandTag prepareCommandTagConfigurations() throws DynConfigException;

    /**
     * Create the equipment configuration which can then be passed to the C2MON server
     * @param mapping contains additional specification regarding the C2MON-internal equipment name and description
     * @return equipmentBuilder the equipmentBuilder to extend with protocol-specific fields
     * @throws DynConfigException if the equipment address passed through the original query is malformed
     */
    Equipment prepareEquipmentConfiguration(ProcessEquipmentURIMapping mapping) throws DynConfigException;

    /**
     * Check whether a regular expression matches the strategy including the parameters parsed from the query URI
     * @param pattern a regular expression
     * @return whether of not the pattern matches the strategy including the parameters parsed from the query URI
     */
    boolean matches(String pattern);
}