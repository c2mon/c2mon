/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
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
