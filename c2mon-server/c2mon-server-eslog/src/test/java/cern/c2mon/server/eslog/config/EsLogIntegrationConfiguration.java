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
package cern.c2mon.server.eslog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(locations = {
    "classpath:cern/c2mon/server/test/server-test-properties.xml",
    "classpath:cern/c2mon/server/cache/config/server-cache-nonpersistent-server.xml",
    "classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-common.xml",
    "classpath:cern/c2mon/server/test/cache/config/server-test-datasource-hsqldb.xml",
    "classpath:cern/c2mon/server/cache/loading/config/server-cacheloading.xml",
    "classpath:cern/c2mon/server/supervision/config/server-supervision.xml"
})
@Import(EsLogConfiguration.class)
public class EsLogIntegrationConfiguration {

}
