/*
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.HashSet;

import org.junit.Test;

import cern.c2mon.notification.Notifier;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.shared.Subscription;
import cern.dmn2.core.Status;

/**
 * 
 * @author felixehm 
 */
public class ReminderTest {

    @Test
    public void testReminder() {
        ReminderImpl service = new ReminderImpl();
        
        /* prepare registry list */
        Subscription sub = new Subscription("test", 1L);
        sub.setLastNotifiedStatus(Status.WARNING);
        // last normal notification older than the reminder interval.  
        sub.setLastNotification(new Timestamp(System.currentTimeMillis() - service.getReminderTime() - 100));
        
        HashSet<Subscription> list = new HashSet<Subscription>();
        list.add(sub);
        
        /* Registry */
        SubscriptionRegistry registry = getRegistryMock(list);

        /* Notifier */
        Notifier notifier = getNotifierMock(sub);

        /* replay */
        replay(registry);
        replay(notifier);
        
        /* do action */
        service.setRegistry(registry);
        service.setNotifier(notifier);
        service.checkForReminder();
        
        /* verify */
        verify(registry);
        verify(notifier);
     
        
        
    }
    
    private Notifier getNotifierMock(Subscription sub) {
        Notifier notifier = createMock(Notifier.class);
        notifier.sendReminder(sub);
        expectLastCall().once();
        return notifier;
    }
    
    private SubscriptionRegistry getRegistryMock(HashSet<Subscription> list) {
        SubscriptionRegistry registry = createMock(SubscriptionRegistry.class);
        expect(registry.getRegisteredSubscriptions()).andReturn(list).once();
        return registry;
    }
    
    @Test
    public void testDisabledReminderByNormalNotificationTs() {
        
        ReminderImpl service = new ReminderImpl();
        
        /* prepare registry calls */
        Subscription sub = new Subscription("test", 1L);
        sub.setLastNotifiedStatus(Status.WARNING);
        
        HashSet<Subscription> list = new HashSet<Subscription>();
        list.add(sub);
        
        SubscriptionRegistry registry = getRegistryMock(list);
        
        /* Notifier */
        Notifier notifier = createMock(Notifier.class);
        
        /* replay */
        replay(registry);
        replay(notifier);
        
        
        /* do action */
        service.setRegistry(registry);
        service.setNotifier(notifier);
        service.checkForReminder();
        
        /* verify */
        verify(registry);
        verify(notifier);
        
    }
}

