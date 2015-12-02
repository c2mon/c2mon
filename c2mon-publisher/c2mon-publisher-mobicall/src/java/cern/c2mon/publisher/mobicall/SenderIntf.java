/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import java.io.IOException;

public interface SenderIntf {

    void setup() throws IOException;

    void send(String mobicallId, String message);

}
