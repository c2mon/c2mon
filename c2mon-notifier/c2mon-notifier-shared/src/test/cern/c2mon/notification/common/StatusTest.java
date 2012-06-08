/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cern.c2mon.notification.shared.Status;

public class StatusTest {

    @Test
    public void testCorrectStatus() {
        assertTrue(Status.fromInt(-1).equals(Status.UNKNOWN));
        assertTrue(Status.fromInt(0).equals(Status.OK));
        assertTrue(Status.fromInt(1).equals(Status.WARNING));
        assertTrue(Status.fromInt(2).equals(Status.ERROR));
        assertTrue(Status.fromInt(3).equals(Status.FATAL));
    }

    
    @Test
    public void testCorrectStatusComparison() {
        
        assertTrue(Status.UNKNOWN.betterThan(Status.OK));
        assertTrue(Status.OK.betterThan(Status.WARNING));
        assertTrue(Status.WARNING.betterThan(Status.ERROR));
        assertTrue(Status.ERROR.betterThan(Status.FATAL));
        
        assertTrue(Status.FATAL.worserThan(Status.ERROR));
        assertTrue(Status.ERROR.worserThan(Status.WARNING));
        assertTrue(Status.WARNING.worserThan(Status.OK));
        assertTrue(Status.OK.worserThan(Status.UNKNOWN));
        
        assertFalse(Status.OK.worserThan(Status.FATAL));
        
        
    }
}


