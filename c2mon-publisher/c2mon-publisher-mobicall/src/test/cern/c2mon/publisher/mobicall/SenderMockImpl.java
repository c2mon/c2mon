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
