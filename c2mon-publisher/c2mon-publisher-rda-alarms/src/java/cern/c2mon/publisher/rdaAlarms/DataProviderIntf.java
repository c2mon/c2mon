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

package cern.c2mon.publisher.rdaAlarms;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The "dataprovider" is the interface to the database. In the publisher application, it has
 * implementations pointing directly to the database (mainly for testing, and as workarround 
 * for problems with ...) or through JMS.
 * 
 * The methods return data about alarm definitions and their link to alarm sources.
 * 
 * The close method is called when the publisher is stopped. The connection, if needed, must
 * be established at construction time.
 * 
 * @author mbuttner
 */
public interface DataProviderIntf {

    String getSource(String alarmId) throws Exception;      // the source for a given alarm
    Collection<String> getSourceNames() throws Exception;   // all known sources
    
    // for caching of the sources for the initial active list. Rather than asking for the 
    // sources one by one, we try to use an array call with all active alarms at startup
    ConcurrentHashMap<String, String> initSourceMap(Set<String> alarmIds) throws Exception;

    void close();   

}
