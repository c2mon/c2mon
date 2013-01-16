/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
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
        
        while (!C2monServiceGateway.getSupervisionManager().isServerConnectionWorking()) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Error while waiting for C2Mon serivce to start up.");
            }
        }
        
        
        
        
        TagCache c = new TagCache();
        Tag t = c.resolveSubTags(2137951L);
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
