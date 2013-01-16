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

import java.util.HashSet;

import org.easymock.classextension.EasyMock;
import org.junit.Test;

import cern.c2mon.notification.Reminder;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.impl.ReminderImpl;
import cern.c2mon.notification.impl.TagCache;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.dmn2.core.Status;

/**
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class ReminderTest {

    @Test
    public void testReminder() {
        ReminderImpl service = new ReminderImpl();
        
        TagCache c = EasyMock.createMock(TagCache.class);
        c.get(1L);
        EasyMock.expectLastCall().once();
        
        Subscriber s = new Subscriber("test", "test@mail.com", null);
        Subscription sub = new Subscription("test",1L);
        sub.setLastNotifiedStatus(Status.WARNING);
        
        HashSet<Subscription> list = new HashSet<Subscription>();
        list.add(sub);
        
        
        SubscriptionRegistry registry = EasyMock.createMock(SubscriptionRegistry.class);
        registry.getRegisteredSubscriptions();
        EasyMock.expectLastCall().once().andReturn(list);
        
        registry.getSubscriber(s.getUserName());
        EasyMock.expectLastCall().once().andReturn(s);
        
        
        
        service.setTagCache(c);
        service.setRegistry(registry);
        service.
        
    }
}
