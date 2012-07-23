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

import cern.c2mon.notification.shared.Subscription;
import cern.dmn2.core.Status;

public class SubscriptionTest {

    @Test
    public void testIsInterestedInLevel() {
        Subscription s = new Subscription("test", 1L);
        
        assertTrue("isInterestedInLevel() reports " + s.isInterestedInLevel(Status.OK), s.isInterestedInLevel(Status.OK));
        assertTrue("isInterestedInLevel() reports " + s.isInterestedInLevel(Status.WARNING), s.isInterestedInLevel(Status.WARNING));
        assertTrue("isInterestedInLevel() reports " + s.isInterestedInLevel(Status.ERROR), s.isInterestedInLevel(Status.ERROR));

        s.setNotificationLevel(Status.ERROR);
        assertFalse("isInterestedInLevel() reports " + s.isInterestedInLevel(Status.WARNING), s.isInterestedInLevel(Status.WARNING));
        
    }
}
