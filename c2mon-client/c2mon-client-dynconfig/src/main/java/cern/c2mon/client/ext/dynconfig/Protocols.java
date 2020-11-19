package cern.c2mon.client.ext.dynconfig;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;


/**
 * This enum defined the protocols currently supported by the DynConfig Module along with their schemes.
 */
public enum Protocols {
    PROTOCOL_DIP("dip"),
    PROTOCOL_OPCUA("opc.tcp"),
    PROTOCOL_REST("http", "https");

    private final List<String> urlSchemes;

    Protocols(String... urlSchemes) {
        this.urlSchemes = Arrays.asList(urlSchemes);
    }

    /**
     * Fetches the Protocols enum object matching a scheme
     * @param scheme the uri scheme for which the Protocol shall be fetched
     * @return the Protocols value matching the scheme
     * @throws DynConfigException if the scheme does not correspond to any supported Protocol
     */
    public static Protocols getEnumForScheme(String scheme) throws DynConfigException {
        scheme = scheme.toLowerCase(Locale.getDefault());
        if (PROTOCOL_DIP.urlSchemes.contains(scheme)) {
            return PROTOCOL_DIP;
        } else if (PROTOCOL_OPCUA.urlSchemes.contains(scheme)) {
            return PROTOCOL_OPCUA;
        } else if (PROTOCOL_REST.urlSchemes.contains(scheme)) {
            return PROTOCOL_REST;
        } else {
            throw new DynConfigException(DynConfigException.Context.UNSUPPORTED_SCHEME, scheme);
        }
    }
}