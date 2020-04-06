package cern.c2mon.client.ext.dynconfig.query;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import cern.c2mon.client.ext.dynconfig.config.Protocols;
import cern.c2mon.shared.common.datatag.address.OPCCommandHardwareAddress;
import lombok.Getter;

import java.net.URI;
import java.util.Objects;

/**
 * A class holding the properties of a query specifically directed at an equipment using the OPC UA protocol.
 */
public class OpcUaQueryObj extends QueryObjBase {

    @Getter
    private int namespace;

    @Getter
    private String uri;

    @Getter
    private Integer commandPulseLength;

    @Getter
    private OPCCommandHardwareAddress.COMMAND_TYPE commandType;

    /**
     * Create as a new query object holding properties specific to the OPC UA standard.
     * @param uri the uri containing all relevant information over the query
     * @throws DynConfigException thrown if not all mandatory properties are present.
     */
    public OpcUaQueryObj(URI uri) throws DynConfigException {
        super(uri);
        this.uri = QueryObjBase.getSchemeAuthorityAndPath(uri);
        this.namespace = Integer.parseInt(properties.getOrDefault(Keys.NAMESPACE.propertyName, "1"));
        if (properties.containsKey(Keys.COMMAND_TYPE.propertyName)) {
            if ("method".equalsIgnoreCase(properties.get(Keys.COMMAND_TYPE.propertyName))) {
                commandType = OPCCommandHardwareAddress.COMMAND_TYPE.METHOD;
            } else if ("classic".equalsIgnoreCase(properties.get(Keys.COMMAND_TYPE.propertyName))) {
                commandType = OPCCommandHardwareAddress.COMMAND_TYPE.CLASSIC;
            }
        }
        if (properties.containsKey(Keys.COMMAND_PULSE.propertyName)) {
            commandPulseLength = Integer.parseInt(properties.getOrDefault(Keys.COMMAND_PULSE.propertyName, "1"));
        }

    }

    /**
     * Get the protocol of the given query object
     * @return the @{@link Protocols} of the query object, in this case OPCUA
     */
    @Override
    public Protocols getProtocol() {
        return Protocols.PROTOCOL_OPCUA;
    }

    /**
     * checks whether the regular expression matches any of the relevant QueryObject fields uri and itemName
     * @param regex the regular expression to check for
     * @return true if the regular expression matches the values of any of the relevant fields.
     */
    @Override
    public boolean matches(String regex) {
        if (uri.matches(regex)) {
            return true;
        }
        return super.matches(regex);
    }

    /**
     * OpcUaQueryObj are considered equal if they are of the same type and their fields hold the same values.
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
        OpcUaQueryObj that = (OpcUaQueryObj) o;
        return namespace == that.namespace &&
                uri.equals(that.uri);
    }

    /**
     * Override the hashCode method to fit the custom equals implementation
     * @return the hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), namespace, uri);
    }


    @Override
    protected String[] getRequiredPropertiesForProtocol() {
        return new String[]{Keys.NAMESPACE.propertyName};
    }

}