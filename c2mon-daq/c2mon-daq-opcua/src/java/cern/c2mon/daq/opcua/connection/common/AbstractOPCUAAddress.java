/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2014 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.daq.opcua.connection.common;

import java.net.URI;

import cern.c2mon.daq.opcua.connection.common.impl.AliveWriter;

/**
 * AbstractOPCUAAddress abstract class (no implementation of this class)
 * 
 * @author vilches
 */
public abstract class AbstractOPCUAAddress {
    /**
     * URI of the OPC server.
     */
    protected URI uri;

    /**
     * The server timeout when a server is considered down.
     */
    protected int serverTimeout;

    /**
     * The server retry timeout indicates how much time should be between
     * reconnection attempts.
     */
    protected int serverRetryTimeout;

    /**
     * User name to authenticate.
     */
    protected String user;
    
    /**
     * Password to authenticate.
     */
    protected String password;

    /**
     * Domain to authenticate to.
     */
    protected String domain;
    
    /**
     * If, set to false, then the Alive WriterTask is not started.
     * The OPC has then to update itself the equipment alive tag,
     * otherwise the C2MON server will invalidate all tags from this
     * process because of an alive timer expiration.<p>
     * The default value is <code>true</code>.
     * @see AliveWriter
     */
    protected boolean aliveWriter = true;
    
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
     * If this method returns <code>false</code>, then the Alive WriterTask
     * is not started. The OPC has then to update itself the equipment alive tag,
     * otherwise the C2MON server will invalidate all tags from this process
     * because of an alive timer expiration.
     * 
     * @return <code>true</code>, if the AliveWriter
     *         task shall be enabled or not.
     * @see AliveWriter
     */
    public boolean isAliveWriteEnabled() {
      return aliveWriter;
    }
    


    /**
     * Builder class.
     *
     */
    public static abstract class AbstractBuilder {

        /**
         * The uri for the address.
         */
        protected URI uri;

        /**
         * The server timeout after which time the server is considered down.
         */
        protected int serverTimeout;

        /**
         * The server retry timeout in which interval reconnections are done.
         */
        protected int serverRetryTimeout;

        /**
         * User name to authenticate.
         */
        protected String user;

        /**
         * Password to authenticate.
         */
        protected String password;

        /**
         * Domain to authenticate to.
         */
        protected String domain;
        
        /**
         * If, set to <code>false</code>, then the Alive WriterTask is not started.
         * The OPC has then to update itself the equipment alive tag,
         * otherwise the C2MON server will invalidate all tags from this
         * process because of an alive timer expiration.<p>
         * The default value is <code>true</code>.
         * @see AliveWriter
         */
        protected boolean aliveWriter = true;

        /**
         * Sets the user and domain in the form of user@domain.
         * 
         * @param userAtDomain user@domain string.
         * @return The Builder object itself to chain the calls.
         */
        public AbstractBuilder userAtDomain(final String userAtDomain) {
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
        public AbstractBuilder user(final String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the password for authentication.
         * 
         * @param password The password.
         * @return The Builder object itself to chain the calls.
         */
        public AbstractBuilder password(final String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the domain to authenticate to.
         * 
         * @param domain The domain name.
         * @return The Builder object itself to chain the calls.
         */
        public AbstractBuilder domain(final String domain) {
            this.domain = domain;
            return this;
        }
        
        /**
         * If this value is set to <code>false</code>, then the Alive WriterTask
         * is not started. The OPC has then to update itself the equipment alive tag,
         * otherwise the C2MON server will invalidate all tags from this process
         * because of an alive timer expiration.<p>
         * The default value is set to <code>true</code>
         * @param aliveWriter The startup option of the AliveWriter process.
         * @return The Builder object itself to chain the calls.
         * 
         * @see AliveWriter
         */
        public AbstractBuilder aliveWriter(final boolean aliveWriter) {
          this.aliveWriter = aliveWriter;
          return this;
        }
        
        /**
         * 
         * @return
         */
        public final URI getUri() {
            return uri;
        }

        /**
         * 
         * @return
         */
        public final int getServerTimeout() {
            return serverTimeout;
        }

        /**
         * 
         * @return
         */
        public final int getServerRetryTimeout() {
            return serverRetryTimeout;
        }

        /**
         * 
         * @return
         */
        public final String getUser() {
            return user;
        }

        /**
         * 
         * @return
         */
        public final String getPassword() {
            return password;
        }

        /**
         * 
         * @return
         */
        public final String getDomain() {
            return domain;
        }

        /**
         * 
         * @return
         */
        public final boolean isAliveWriter() {
            return aliveWriter;
        }

        /**
         * Builds the OPCUAAddress object based on the provided parameters.
         * 
         * @return The new OPCUAAddress object.
         */
        public abstract AbstractOPCUAAddress build();
    }
}
