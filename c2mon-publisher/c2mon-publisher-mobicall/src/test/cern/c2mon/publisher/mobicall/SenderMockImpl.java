/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation of the sender mechanism. Here, we mainly log and count the
 * calls to send messages. The count can later on be used to check if the messages
 * would have been sent or not.
 * 
 * @author mbuttner
 */
public class SenderMockImpl implements SenderIntf {

    private static final Logger LOG = LoggerFactory.getLogger(SenderMockImpl.class);
    private int count;
    
    //
    // --- Implements SenderIntf ------------------------------------------------------------
    //
    @Override
    public void setup() throws IOException {
        LOG.info("Setup method called");
    }

    @Override
    public void send(String mobicallId, String message) {
        count++;
        LOG.info("Mobicall notification: {} {}", count, message);
    }

    //
    // --- PUBLIC METHODS -------------------------------------------------------------------
    //
    public int getCount() {
        return count;
    }
}
