package cern.c2mon.client.ext.dynconfig.config;

import cern.c2mon.client.ext.dynconfig.DynConfigException;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * This enum defined the protocols currently supported by the DynConfig Module along with their schemes.
 */
@AllArgsConstructor
@Getter
public enum Protocols {
    PROTOCOL_DIP("dip", false),
    PROTOCOL_OPCUA("opc.tcp", false),
    PROTOCOL_OPCDA("opcda",false),
    PROTOCOL_SIMU("simu", false),
    PROTOCOL_HEARTBEAT("heartbeat", false),
    PROTOCOL_RDA("rda", false),
    PROTOCOL_JAPC("japc", false);

    private final String urlScheme;
    private final boolean supportsAuthentication;

    /**
     * Fetches the Protocols enum object matching a scheme
     * @param scheme the uri scheme for which the Protocol shall be fetched
     * @return the Protocols value matching the scheme
     * @throws DynConfigException if the scheme does not correspond to any supported Protocol
     */
    public static Protocols getEnumForScheme(String scheme) throws DynConfigException {
        if (scheme.equalsIgnoreCase(PROTOCOL_DIP.urlScheme)) {
            return PROTOCOL_DIP;
        } else if (scheme.equalsIgnoreCase(PROTOCOL_OPCUA.urlScheme)) {
            return PROTOCOL_OPCUA;
        } else {
            throw new DynConfigException(DynConfigException.Context.UNSUPPORTED_SCHEME, scheme);
        }
    }
}