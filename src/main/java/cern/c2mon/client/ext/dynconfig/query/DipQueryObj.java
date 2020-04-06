package cern.c2mon.client.ext.dynconfig.query;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.config.Protocols;
import lombok.Getter;

import java.net.URI;
import java.util.Objects;

/**
 * A class holding the properties of a query using the DIP protocol.
 */
public class DipQueryObj extends QueryObjBase {

    @Getter
    private String uri;

    /**
     * Create as a new query object holding properties specific to the DIP protocol.
     * @param uri the uri containing all relevant information over the query
     * @throws DynConfigException thrown if not all mandatory properties are present.
     */
    public DipQueryObj(URI uri) throws DynConfigException {
        super(uri);
        this.uri = QueryObjBase.getSchemeAuthorityAndPath(uri);
    }

    /**
     * Get the protocol of the given query object
     * @return the @{@link Protocols} of the Query Object, in this case DIP
     */
    @Override
    public Protocols getProtocol() {
        return Protocols.PROTOCOL_DIP;
    }

    /**
     * checks whether the regular expression matches any of the relevant QueryObject fields uri and itemName
     * @param regex the regular expression to check for
     * @return true if the regular expression matches the values of any of the relevant fields.
     */
    @Override
    public boolean matches(String regex) {
        return uri.matches(regex) || super.matches(regex);
    }

    /**
     * DipQueryObjects are considered equal if they are of the same type and their fields hold the same values.
     * @param o the object to compare to
     * @return true if the objects are of the same type and their fields hold the same values.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else if (!super.equals(o)) {
            return false;
        }
        DipQueryObj that = (DipQueryObj) o;
        return Objects.equals(uri, that.uri);
    }

    /**
     * Override the hashCode method to fit the custom equals implementation
     * @return the hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uri);
    }

    @Override
    protected String[] getRequiredPropertiesForProtocol() {
        return new String[]{};
    }

}
