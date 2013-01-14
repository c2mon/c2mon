/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import cern.c2mon.notification.impl.DbBackupWriter;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.UserNotFoundException;
import cern.dmn2.core.Status;

public class DbBackupWriterTest {

    public JdbcTemplate jdbcTemplate; 
    
    private DbBackupWriter writer = null;
    
    private SingleConnectionDataSource driver;
    
    private String fileName = "test.db";
    
    
    @BeforeClass
    public static void initLog4J() {
        System.setProperty("log4j.configuration", DbBackupWriterTest.class.getResource("log4j.properties").toExternalForm());
        System.out.println(System.getProperty("log4j.configuration"));
    }
    
    @Before
    public void setUp() {
        File f = new File (fileName);
        if (f.exists()) {
            System.out.println("Deleting " + fileName + " for test...");
            f.delete();
        }
        
        driver = new SingleConnectionDataSource("jdbc:sqlite:" + fileName, "" , "", false);
        driver.setDriverClassName("org.sqlite.JDBC");
        org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy proxy = new org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy(driver);
        writer = new DbBackupWriter(proxy);
        writer.createSchema();
        
    }
    
    @After
    public void tearDown() {
        driver.destroy();
    }
    
    
    
    @Test
    public void testAddSubscriber() throws UserNotFoundException {
        Subscriber s = SubscriptionRegistryTest.getSubscriber();
        writer.addSubscriber(s);
        Subscriber fromDb = writer.getSubscriber(s.getUserName());
        assertTrue(s.equals(fromDb));
        try {
            writer.addSubscriber(s);
            fail("I was able to add the user twice.");
        } catch (Exception ex) {
            // IGNORE
        }
    }
    
    @Test
    public void testAddSubscriberWithSubscriptions() throws UserNotFoundException {
        Subscriber toAdd = SubscriptionRegistryTest.getSubscriber();
        toAdd.addSubscription(new Subscription(toAdd, 1L, 2));
        toAdd.addSubscription(new Subscription(toAdd, 2L, 2));
        toAdd.addSubscription(new Subscription(toAdd, 3L, 2));
        
        writer.setSubscriber(toAdd);
        System.out.println("Write to DB : \n" + toAdd);
        
        Subscriber fromDb = writer.getSubscriber(toAdd.getUserName());
        System.out.println("Read from DB : \n" + fromDb);
        
        assertEquals(toAdd.getSubscribedTagIds().size(), fromDb.getSubscribedTagIds().size());
    }
    
    @Test(expected = UserNotFoundException.class)
    public void testUserNotFoundException() throws UserNotFoundException {
        Subscriber toAdd = SubscriptionRegistryTest.getSubscriber();
        //writer.setSubscriber(toAdd);
        //Subscriber toCheck = SubscriptionRegistryTest.getSubscriber();
        writer.getSubscriber(toAdd.getUserName());
    }
    
    @Test(expected = UserNotFoundException.class)
    public void testSchema() throws UserNotFoundException {

        /*
         * add a nice subscriber 
         */
        Subscriber toAdd = SubscriptionRegistryTest.getSubscriber();
        toAdd.addSubscription(new Subscription(toAdd, 1L, 2));
        toAdd.addSubscription(new Subscription(toAdd, 2L, 2));
        toAdd.addSubscription(new Subscription(toAdd, 3L, 2));
        writer.setSubscriber(toAdd);
        
        /*
         * should also remove the subscriptions
         */
        writer.removeSubscriber(toAdd);
       
        /*
         * throws.. 
         */
        writer.getSubscriber(toAdd.getUserName());
    }
    
    
    @Test
    public void testMeasureWriteAndLoadTime() {
        int total = 100;
        
        /*
         * test writing a large set to db and see how long it takes.
         */

        ConcurrentHashMap<String, Subscriber> toStore = new ConcurrentHashMap<String, Subscriber>(total);
        long totalSubs = 0;
        for (int i = 1; i <= total; i++) {
            Subscriber s = new Subscriber("test-" + i, "test" + i + "@cern.ch", "");
            for (int y = 1; y <= 100; y++) {
                s.addSubscription(new Subscription(s, new Long(y)));
                totalSubs++;
            }
            toStore.put(s.getUserName(), s);
        }
        
        System.out.println("Created " + total + " subscribers with " + totalSubs + " subscriptions");
        writer.store(toStore);
        ConcurrentHashMap<String, Subscriber> loaded = writer.load();
    }
 
    
    @Test
    public void testSafeSubscriptionObject() {
        Subscriber toAdd = SubscriptionRegistryTest.getSubscriber();
        
        Subscription valueChangeEnabled = new Subscription(toAdd, 2L, Status.WARNING.toInt());
        valueChangeEnabled.setNotifyOnMetricChange(true);
        valueChangeEnabled.setEnabled(false);
        Timestamp lastNotification = new Timestamp(System.currentTimeMillis());
        valueChangeEnabled.setLastNotifiedStatus(Status.WARNING);
        valueChangeEnabled.setLastNotification(lastNotification);
        valueChangeEnabled.setSmsNotification(true);
        
        toAdd.addSubscription(valueChangeEnabled);
        
        writer.setSubscriber(toAdd);
        Subscription fromStore = writer.getSubscriber(toAdd.getUserName()).getSubscriptions().get(valueChangeEnabled.getTagId());
        
        assertTrue(fromStore.getLastNotification().equals(lastNotification));
        assertTrue(fromStore.getLastNotifiedStatus().equals(Status.WARNING));
        assertTrue("Notification Level not expected : " + fromStore.getNotificationLevel(), fromStore.getNotificationLevel() == Status.WARNING);
        assertFalse(fromStore.isEnabled());
        assertTrue(fromStore.isNotifyOnMetricChange());
        assertTrue(fromStore.isSmsNotification());
        
        
    }
        
    
    
    public void testConcurrentTest() {
        Subscriber toAdd = SubscriptionRegistryTest.getSubscriber();
        Subscription valueChangeEnabled = new Subscription(toAdd, 2L, Status.WARNING.toInt());
        toAdd.addSubscription(valueChangeEnabled);
        
        writer.setSubscriber(toAdd);
    }
}
