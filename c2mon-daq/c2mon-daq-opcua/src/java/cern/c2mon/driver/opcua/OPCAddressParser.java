package cern.c2mon.driver.opcua;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cern.c2mon.driver.opcua.connection.common.impl.OPCCriticalException;

/**
 * The OPCAddress parser takes a String in the format provided from the core and
 * translates it to a list of properties.
 * 
 * @author Andreas Lang
 * 
 */
public class OPCAddressParser {

    /**
     * URI key in the address string.
     */
    public static final String URI_KEY = "URI";
    /**
     * user key in the address string.
     */
    public static final String USER_KEY = "user";
    /**
     * password key in the address string.
     */
    public static final String PASSWORD_KEY = "password";
    /**
     * serverTimeout key in the address string.
     */
    public static final String SERVER_TIMEOUT_KEY = "serverTimeout";
    /**
     * serverRetryTimeout key in the address string.
     */
    public static final String SERVER_RETRY_TIMEOUT_KEY = "serverRetryTimeout";

    /**
     * Creates a properties object which has the properties defined in the
     * provided address String. A Standard OPC address String has the form:
     * 
     * <pre>
     * URI=protocol1://host1[:port1]/[path1][,protocol2://host2[:port2]/[path2]];
     * user=user1[@domain1][,user2[@domain2]];password=password1[,password2];
     * serverTimeout=serverTimeout;serverRetryTimeout=serverRetryTimeout
     * </pre>
     * 
     * The parts in brackets are optional.
     * 
     * @param address
     *            The address String.
     * @return The properties object with the properties from the provided
     *         address String.
     */
    public List<OPCAddress> parseAddressString(final String address) {
        List<OPCAddress> addresses = new ArrayList<OPCAddress>(2);
        try {
            String[] keyValues = address.split(";");
            Properties properties = new Properties();
            for (String keyValueString : keyValues) {
                String[] keyValuePair = keyValueString.trim().split("=");
                // if there is nothing to split ignore it
                if (keyValuePair.length > 1) {
                    String key = keyValuePair[0].trim();
                    String value = keyValuePair[1].trim();
                    properties.put(key, value);
                }
            }
            String uriProperty = properties.getProperty(URI_KEY);
            String[] uris = uriProperty.split(",");
            ;
            // if (uriProperty.contains(","))
            // uris = uriProperty.split(",");
            // else
            // uris = { uriProperty };
            String[] usersAtDomains =
                properties.getProperty(USER_KEY).split(",");
            String[] passwords =
                properties.getProperty(PASSWORD_KEY).split(",");
            int serverTimeout =
                Integer.valueOf(properties.getProperty(SERVER_TIMEOUT_KEY));
            int serverRetryTimeout =
                Integer.valueOf(properties.getProperty(SERVER_RETRY_TIMEOUT_KEY));
            OPCAddress primaryAddress = createOPCAddress(
                    uris[0], usersAtDomains[0], passwords[0], 
                    serverTimeout, serverRetryTimeout);
            addresses.add(primaryAddress);
            if (uris.length > 1) {
                OPCAddress alternativeAddress = createOPCAddress(
                        uris[1], usersAtDomains.length > 1 
                        ? usersAtDomains[1] : null,
                        passwords.length > 1 ? passwords[1] : null, 
                        serverTimeout,
                        serverRetryTimeout);
                addresses.add(alternativeAddress);
            }
        } catch (Exception ex) {
            throw new OPCCriticalException("Address parsing failed.", ex);
        }
        return addresses;
    }

    /**
     * Creates an OPC address.
     * 
     * @param uri
     *            The URI for the address.
     * @param userAtDomain
     *            The user and domain in the format user@domain.
     * @param password
     *            The password for authentication.
     * @param serverTimeout
     *            The time after the server will be considered dead if it
     *            doesn't react.
     * @param serverRetryTimeout
     *            The interval in which a reconnection attempt is started.
     * @return The new OPC address.
     */
    private OPCAddress createOPCAddress(final String uri, final String userAtDomain, final String password, final int serverTimeout, final int serverRetryTimeout) {
        OPCAddress primaryAddress;
        try {
            primaryAddress = new OPCAddress.Builder(
            uri.trim(), serverTimeout, serverRetryTimeout)
                .userAtDomain(userAtDomain != null ? userAtDomain.trim() : null)
                .password(password.trim())
                .build();
        } catch (URISyntaxException e) {
            throw new OPCAddressException("Syntax of OPC URI is incorrect: " + uri, e);
        }
        return primaryAddress;
    }

}
