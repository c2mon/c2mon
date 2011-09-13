package cern.c2mon.driver.opcua;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The an OPC server.
 * 
 * @author Andreas Lang
 * 
 */
public final class OPCAddress {

    /**
     * URI of the OPC server.
     */
    private URI uri;

    /**
     * The server timeout when a server is considered down.
     */
    private int serverTimeout;

    /**
     * The server retry timeout indicates how much time should be between
     * reconnection attempts.
     */
    private int serverRetryTimeout;

    /**
     * User name to authenticate.
     */
    private String user;
    
    /**
     * Password to authenticate.
     */
    private String password;

    /**
     * Domain to authenticate to.
     */
    private String domain;

    /**
     * Private constructor. Use the Builder class to create an instance.
     * 
     * @param builder The builder to create an instance of this class.
     */
    private OPCAddress(final Builder builder) {
        uri = builder.uri;
        serverRetryTimeout = builder.serverRetryTimeout;
        serverTimeout = builder.serverTimeout;
        user = builder.user;
        password = builder.password;
        domain = builder.domain;
    }

    /**
     * @return the uri
     */
    public String getUriString() {
        return uri.toString();
    }

    /**
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return uri.getScheme();
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @return the serverTimeout
     */
    public int getServerTimeout() {
        return serverTimeout;
    }

    /**
     * @return the serverRetryTimeout
     */
    public int getServerRetryTimeout() {
        return serverRetryTimeout;
    }

    /**
     * Builder class.
     * 
     * @author Andreas Lang
     *
     */
    public static class Builder {

        /**
         * The uri for the address.
         */
        private final URI uri;

        /**
         * The server timeout after which time the server is considered down.
         */
        private final int serverTimeout;

        /**
         * The server retry timeout in which interval reconnections are done.
         */
        private final int serverRetryTimeout;

        /**
         * User name to authenticate.
         */
        private String user;

        /**
         * Password to authenticate.
         */
        private String password;

        /**
         * Domain to authenticate to.
         */
        private String domain;

        /**
         * Creates a new Builder object with the mandatory parameters set.
         * 
         * @param uri The URI of the address.
         * @param serverTimeout The server timeout after which the OPC server is
         * considered as down.
         * @param serverRetryTimeout The retry timeout which defines the
         * interval to retry to connect. 
         * @throws URISyntaxException Throws an {@link URISyntaxException} if
         * the uri String has a wrong format.
         */
        public Builder(
                final String uri, final int serverTimeout,
                final int serverRetryTimeout) throws URISyntaxException {
            this(new URI(uri), serverTimeout, serverRetryTimeout);
        }

        /**
         * Creates a new Builder object with the mandatory parameters set.
         * 
         * @param uri The URI of the address.
         * @param serverTimeout The server timeout after which the OPC server is
         * considered as down.
         * @param serverRetryTimeout The retry timeout which defines the
         * interval to retry to connect. 
         */
        public Builder(
                final URI uri, final int serverTimeout,
                final int serverRetryTimeout) {
            this.uri = uri;
            this.serverTimeout = serverTimeout;
            this.serverRetryTimeout = serverRetryTimeout;
        }

        /**
         * Sets the user and domain in the form of user@domain.
         * 
         * @param userAtDomain user@domain string.
         * @return The Builder object itself to chain the calls.
         */
        public Builder userAtDomain(final String userAtDomain) {
            if (userAtDomain != null && userAtDomain.contains("@")) {
                String[] userAndDomain = userAtDomain.split("@");
                this.user(userAndDomain[0]).domain(userAndDomain[1]);
            } else {
                this.user(userAtDomain);
            }
            return this;
        }

        /**
         * Sets the user name for authentication.
         * 
         * @param user The user name.
         * @return The Builder object itself to chain the calls.
         */
        public Builder user(final String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the password for authentication.
         * 
         * @param password The password.
         * @return The Builder object itself to chain the calls.
         */
        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the domain to authenticate to.
         * 
         * @param domain The domain name.
         * @return The Builder object itself to chain the calls.
         */
        public Builder domain(final String domain) {
            this.domain = domain;
            return this;
        }

        /**
         * Builds the OPCAddress object based on the provided parameters.
         * 
         * @return The new OPCAddress object.
         */
        public OPCAddress build() {
            return new OPCAddress(this);
        }
    }

}
