package cern.c2mon.client.ext.dynconfig.strategy;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.config.ProcessEquipmentURIMapping;
import cern.c2mon.client.ext.dynconfig.config.Protocols;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;

import java.net.URI;

import static cern.c2mon.client.ext.dynconfig.DynConfigException.Context.UNSUPPORTED_SCHEME;
import static cern.c2mon.client.ext.dynconfig.config.Protocols.*;

/**
 * An interface for strategies on how a specific protocol or standard is handled in the C2MON
 * subscription process.
 */
public interface ITagConfigStrategy {

    /**
     * Return the protocol specific factory appropriate for the scheme.
     * @param uri uri must contain at least the scheme and authority, so the address where the equipment is reachable.
     *            Other mandatory query values may be possible depending on the protocol / scheme. Refer to the
     *            class corresponding to the protocol of {@link ITagConfigStrategy} for specifics.
     *
     *            Therefore, the URI must follow the form:
     *                   scheme://host[:port][/path]
     *                   [tagName=tag name]
     *                   [dataType= java class name]
     *                   [tagDescription=tag description]
     *            for OPC UA queries:
     *                    &itemName=opc ua item name]
     *                   [&commandType=method|classic]
     *            for REST queries:
     *                    &url=url]
     *                    &mode=get|post
     *                    [&getFrequency=integer value]
     *                    [&postFrequency=integer value]
     *            for DIP queries:
     *                    &publicationName=publication name]
     *                    [&fieldName=name of an array field within the structured publication]
     *                    [&fieldIndex=array index of the desired value within the field]
     *            Note that the parts in brackets are optional.
     *
     *            Additionally to the here listed query keys, it is possible to pass any query corresponding to a setter
     *            method of the {@link DataTag.CreateBuilder}, the {@link cern.c2mon.shared.common.datatag.DataTagAddress},
     *            and the protocol-specific {@link cern.c2mon.shared.common.datatag.address.HardwareAddress} class.
     *            For example, to set namespace of a OPC UA tag, one may append ""&hw.setNamespace=namespace"
     * @return A concrete implementation of a {@link ITagConfigStrategy} to fit the scheme of the uri
     * @throws DynConfigException is thrown is the scheme is not supported or the URI does not contain mandatory query keys.
     */
    static ITagConfigStrategy of(URI uri) throws DynConfigException {
        Protocols protocol = getEnumForScheme(uri.getScheme());
        switch (protocol) {
            case PROTOCOL_OPCUA:
                return new OpcUaConfigStrategy(uri);
            case PROTOCOL_DIP:
            return new DipConfigStrategy(uri);
            case PROTOCOL_REST:
                return new RestConfigStrategy(uri);
            default:
                throw new DynConfigException(UNSUPPORTED_SCHEME, uri.getScheme());
        }
    }

    /**
     * Create appropriate preliminary data tag configurations which can then be passed to the
     * C2MON server for creation.
     * @throws DynConfigException if a value in the query cannot be converted to the necessary class, but is mandatory
     * @return the preliminary DataTags to pass on to the C2MON server for creation.
     */
    DataTag prepareTagConfigurations() throws DynConfigException;

    /**
     * Create the equipment configuration which can then be passed to the C2MON server
     * @param mapping contains additional specification regarding the C2MON-internal equipment name and description
     * @throws DynConfigException if the equipment address passed through the original query is malformed
     * @return equipmentBuilder the equipmentBuilder to extend with protocol-specific fields
     */
    Equipment prepareEquipmentConfiguration(ProcessEquipmentURIMapping mapping) throws DynConfigException;

    /**
     * Check whether a regular expression matches the strategy including the parameters parsed from the query URI
     * @param pattern a regular expression
     * @return whether of not the pattern matches the strategy including the parameters parsed from the query URI
     */
    boolean matches(String pattern);
}