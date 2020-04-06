package cern.c2mon.client.ext.dynconfig.factories;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.query.IQueryObj;
import cern.c2mon.client.ext.dynconfig.strategy.ITagConfigStrategy;

import java.net.URI;

import static cern.c2mon.client.ext.dynconfig.DynConfigException.Context.UNSUPPORTED_SCHEME;
import static cern.c2mon.client.ext.dynconfig.config.Protocols.PROTOCOL_DIP;
import static cern.c2mon.client.ext.dynconfig.config.Protocols.PROTOCOL_OPCUA;

/**
 * Abstract factory defining the factory methods shared in between the different supported protocols. This is the
 * point to add factories for newly supported protocols.
 */
public interface ProtocolSpecificFactory {

    /**
     * Defined the interface method to create an {@link IQueryObj}
     * @return the protocol-specific implementation of{@link IQueryObj}
     * @throws DynConfigException if the query object cannot be created
     */
    IQueryObj createQueryObj() throws DynConfigException;

    /**
     * Defined the interface method to create an {@link ITagConfigStrategy}
     * @param queryObj the protocol-specific implementation of{@link IQueryObj} that the strategy will handle
     * @return a new protocol-specific implementation of {@link ITagConfigStrategy}
     */
    ITagConfigStrategy createStrategy(IQueryObj queryObj);


    /**
     * A static factory-of-factories method to return the protocol specific factory appropriate for the scheme.
     * @param uri uri must contain at least the scheme, authority, and the itemName query. Other queries may be possible
     *            or mandatory depending on the scheme. With optional queries in brackets, the URI must have the form:
     *                   scheme://host[:port][/path]
     *                   ?itemName=hardwareAddressItemName;
     *                   [&class=dataType]
     *                   [&tagName=tagName]
     *                   [&tagDescription=tagDescription]
     *            for OPC UA queries:
     *                   &namespace=namespace
     *                   [&commandType=method|classic]
     *                   [&commandPulseLength=commandPulseLength]
     * @return A new concrete implementation of a {@link ProtocolSpecificFactory} to fit the scheme
     * @throws DynConfigException is thrown is the scheme is not supported.
     */
    static ProtocolSpecificFactory of(URI uri) throws DynConfigException {

        String scheme = uri.getScheme();
        if (scheme.equalsIgnoreCase(PROTOCOL_DIP.getUrlScheme())) {
            return new DipFactory(uri);
        } else if (scheme.equalsIgnoreCase(PROTOCOL_OPCUA.getUrlScheme())) {
            return new OpcUaFactory(uri);
        } else {
            throw new DynConfigException(UNSUPPORTED_SCHEME, scheme);
        }
    }
}
