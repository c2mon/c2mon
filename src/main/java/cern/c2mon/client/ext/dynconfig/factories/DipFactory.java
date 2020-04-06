package cern.c2mon.client.ext.dynconfig.factories;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.query.DipQueryObj;
import cern.c2mon.client.ext.dynconfig.query.IQueryObj;
import cern.c2mon.client.ext.dynconfig.strategy.DipConfigStrategy;
import cern.c2mon.client.ext.dynconfig.strategy.ITagConfigStrategy;
import lombok.AllArgsConstructor;

import java.net.URI;

/**
 * A concrete factory class for responsible for creating the OPC UA protocol-specific implementations of {@link IQueryObj}
 * and {@link ITagConfigStrategy}.
 */
@AllArgsConstructor
public class DipFactory implements ProtocolSpecificFactory {

    private URI uri;

    /**
     * Create a new {@link DipQueryObj} holding the values of the properties map.
     * @throws DynConfigException if the properties map does not contain a key "uri", or the URI does not specify a protocol.
     * @return a new {@link DipQueryObj} holding the values of the properties map.
     */
    public IQueryObj createQueryObj() throws DynConfigException {
        return new DipQueryObj(uri);
    }

    /**
     * Create a new {@link DipConfigStrategy}
     * @param queryObj the query object that the strategy will handle.
     * @return a new {@link DipConfigStrategy}
     */
    public ITagConfigStrategy createStrategy(IQueryObj queryObj) {
        return new DipConfigStrategy((DipQueryObj) queryObj);
    }

}
