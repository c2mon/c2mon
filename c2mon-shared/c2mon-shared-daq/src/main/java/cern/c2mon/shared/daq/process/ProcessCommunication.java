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
package cern.c2mon.shared.daq.process;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface for all classes sending a Process commnuciations (Request/Response)
 * 
 * @author vilches
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ProcessConnectionRequest.class, name = "process-connection-request"),
    @JsonSubTypes.Type(value = ProcessConnectionResponse.class, name = "process-connection-response"),
    @JsonSubTypes.Type(value = ProcessConfigurationRequest.class, name = "process-configuration-request"),
    @JsonSubTypes.Type(value = ProcessConfigurationResponse.class, name = "process-configuration-response"),
    @JsonSubTypes.Type(value = ProcessDisconnectionRequest.class, name = "process-disconnection-request")
})
public interface ProcessCommunication {

}
