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

package cern.c2mon.shared.client.alarm;

import lombok.Builder;
import lombok.Getter;

/**
 * Object which allows querying the server alarm cache. 
 * 
 * @author felixehm
 */
@Builder
@Getter
public class AlarmQuery {
    
    private static final int MAX_RESULT_SIZE = 100000;
    
    private Boolean active;
    private String faultFamily;
    private String faultMember;
    private int faultCode;
    private int priority;
    
    private final int maxResultSize = MAX_RESULT_SIZE;
    
}
