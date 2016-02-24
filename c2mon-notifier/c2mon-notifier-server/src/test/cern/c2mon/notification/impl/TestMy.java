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
package cern.c2mon.notification.impl;


import org.junit.Test;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.impl.TagCache;

public class TestMy {

    static {
        System.setProperty("c2mon.client.conf.url", "http://abwww/~dmndev/c2mon/client/client.properties")
        ;
    }
    
    @Test
    public void testMy() {
        
        C2monServiceGateway.startC2monClient();
        
        while (!C2monServiceGateway.getSupervisionService().isServerConnectionWorking()) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Error while waiting for C2Mon serivce to start up.");
            }
        }
        
        TagCache c = new TagCache();
        Tag t = c.resolveSubTags(1107819L);
        System.out.println(t.getAllChildRules());
    }
    
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    //
    // -- implements XXXX -----------------------------------------------
    //

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    // 
    // -- INNER CLASSES -----------------------------------------------
    //
}
