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
package cern.c2mon.daq.opcua.connection.common.impl;

import java.net.URI;
import java.net.URISyntaxException;

import cern.c2mon.daq.opcua.connection.common.AbstractOPCUAAddress;

/**
 * 
 * 
 * @author vilches
 */
public class OPCUADefaultAddress extends AbstractOPCUAAddress {
    
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
        this.aliveWriter = defaultBuilder.isAliveWriter();
    }
    
    /**
     * Builder class.
     *
     */
    public static class DefaultBuilder extends  AbstractBuilder {

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
