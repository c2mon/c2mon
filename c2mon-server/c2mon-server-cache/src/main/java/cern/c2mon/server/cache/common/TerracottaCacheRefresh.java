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
package cern.c2mon.server.cache.common;

/**
 * This class provides a cache refresh functionality
 * for use in the distributed Terracotta Ehcache
 * configuration.
 *
 * <p>It is intended for use a server startup, to load
 * the cache onto the Terracotta node from the Terracotta
 * server (all the cache is instantly available on the
 * node, but is not actually transfer over the network
 * until the cache object is accessed: this is done
 * by this class for all elements in the cache.
 *
 * <p>The purpose of the cache load/refresh is to ensure
 * the TIM server can treat request rapidly once it comes
 * "online"
 *
 * @author Mark Brightwell
 *
 */
public class TerracottaCacheRefresh {

}
