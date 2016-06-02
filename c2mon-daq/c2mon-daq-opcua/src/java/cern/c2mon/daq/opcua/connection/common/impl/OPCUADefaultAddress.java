/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.opcua.connection.common.impl;

import java.net.URI;
import java.net.URISyntaxException;

import cern.c2mon.daq.opcua.connection.common.AbstractOPCUAAddress;
import lombok.Getter;

/**
 *
 *
 * @author vilches
 */
@Getter
public class OPCUADefaultAddress extends AbstractOPCUAAddress {

    private String vendor;

    /**
     * Protected constructor. Use the Builder class to create an instance.
     *
     * @param builder The builder to create an instance of this class.
     */
    private OPCUADefaultAddress (final DefaultBuilder defaultBuilder) {
        this.uri = defaultBuilder.getUri();
        this.serverRetryTimeout = defaultBuilder.getServerRetryTimeout();
        this.serverTimeout = defaultBuilder.getServerTimeout();
        this.user = defaultBuilder.getUser();
        this.password = defaultBuilder.getPassword();
        this.domain = defaultBuilder.getDomain();
        this.aliveWriterEnabled = defaultBuilder.isAliveWriterEnabled();
        this.vendor = defaultBuilder.getVendor();
    }

    /**
     * Builder class.
     *
     */
    @Getter
    public static class DefaultBuilder extends  AbstractBuilder {

        protected String vendor = "";

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
        public DefaultBuilder(
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
        public DefaultBuilder(
                final URI uri, final int serverTimeout,
                final int serverRetryTimeout) {
            this.uri = uri;
            this.serverTimeout = serverTimeout;
            this.serverRetryTimeout = serverRetryTimeout;
        }

        public DefaultBuilder vendor(String vendor) {
          this.vendor = vendor;
          return this;
        }

        /**
         * Builds the OPCUAAddress object based on the provided parameters.
         *
         * @return The new OPCUAAddress object.
         */
        @Override
        public OPCUADefaultAddress build() {
            return new OPCUADefaultAddress(this);
        }
    }
}
