/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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
