/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SenderMockImpl implements SenderIntf {

    private static final Logger LOG = LoggerFactory.getLogger(SenderMockImpl.class);
    private int count;
    
    @Override
    public void setup() throws IOException {
        LOG.info("Setup method called");
    }

    @Override
    public void send(String mobicallId, String message) {
        count++;
        LOG.info("Mobicall notification: {} {}", count, message);
    }

    public int getCount() {
        return count;
    }
}
