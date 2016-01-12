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
package cern.c2mon.notification.jms;

import java.util.UUID;


/**
 * A class containing base information on the sender.
 * 
 * @author felixehm
 */
public class RemoteObject {

    /**
     * the hostname of the machine this RemoteObject is created
     */
    private String hostName;
    private String requestID;

    public RemoteObject() {
        setId(UUID.randomUUID().toString());
    }
    
    public String getOriginHostName() {
        return hostName;
    }
    
    public void setOriginHostName(String hostName) {
        this.hostName = hostName;
    }
    
    public String getId() {
        return requestID;
    }
    
    public void setId(String id) {
        this.requestID = id;
    }
}
