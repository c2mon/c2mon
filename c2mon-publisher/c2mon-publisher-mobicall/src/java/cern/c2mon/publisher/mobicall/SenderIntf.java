/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import java.io.IOException;

/**
 * Interface of the class operating the communication with Mobicall (for mock)
 * 
 * @author mbuttner
 */
public interface SenderIntf {

    // to prepare the system
    void setup() throws IOException;

    // send one message for a given notification id.
    void send(String mobicallId, String message);

}
