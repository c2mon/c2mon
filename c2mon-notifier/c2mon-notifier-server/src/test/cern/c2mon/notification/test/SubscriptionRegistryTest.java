/**
 * 
 */
package cern.c2mon.notification.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.notification.impl.SubscriptionRegistryImpl;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.UserNotFoundException;

/**
 * @author felixehm
 *
 */
public class SubscriptionRegistryTest {

	SubscriptionRegistryImpl reg = null;
	
	@BeforeClass
    public static void initLog4J() {
        System.setProperty("log4j.configuration", SubscriptionRegistryTest.class.getResource("log4j.properties").toExternalForm());
        System.out.println(System.getProperty("log4j.configuration"));
    }
	
	public static Subscriber getSubscriber() {
		return new Subscriber("test", "test@cern.ch", "161240");
	}
	
	public static Subscription getValidSubscription() {
		return new Subscription(getSubscriber(), 1L);
	}
	
	@Before
	public void initRegistry() {
		reg = new SubscriptionRegistryImpl();
	}
	
	@Test
	public void testAddSubscriber() throws UserNotFoundException {
		
	    // test API calls
		Subscriber toAdd = getSubscriber();
	    toAdd.addSubscription(new Subscription(getSubscriber(), 1L, 1));
	    
	    reg.addSubscriber(toAdd);
	    System.out.println(reg);
		
		assertEquals(1, reg.getRegisteredUsers().size());
		assertEquals(1, reg.getAllRegisteredTagIds().size());
		System.out.println(reg);
		
		// second way : pass userId as String and the subscription object. 
		reg.addSubscription(new Subscription(getSubscriber(), 2L));
		System.out.println(reg);
		assertEquals(2, reg.getAllRegisteredTagIds().size());
		assertEquals(1, reg.getRegisteredUsers().size());
	}
	
	@Test
	public void testAddSubscriberWithEmptySubscriptions() throws UserNotFoundException {
		Subscriber toAdd = getSubscriber();
		reg.addSubscriber(toAdd);
		assertEquals(1, reg.getRegisteredUsers().size());
		
		Subscriber inReg = reg.getSubscriber(toAdd.getUserName());
		assertEquals(inReg, toAdd);
		assertEquals(0, inReg.getSubscribedTagIds().size());
		assertEquals(0, reg.getAllRegisteredTagIds().size());
		assertEquals(1, reg.getRegisteredUsers().size());
	}
	
	@Test
	public void testAddSubscriberWithOneDefaultSubscription() throws UserNotFoundException {
		Subscriber toAdd = getSubscriber();
		toAdd.addSubscription(new Subscription(toAdd, 1L));
		reg.addSubscriber(toAdd);
		assertEquals(1, reg.getRegisteredUsers().size());

		// get the subscriber form reg and check its subscriptions.
		Subscriber inReg = reg.getSubscriber(toAdd.getUserName());
		
		assertEquals(1, inReg.getSubscribedTagIds().size());
		
		HashMap<Subscriber, Subscription> list = reg.getSubscriptionsForTagId(1L);
		assertEquals(1, list.size());
		System.out.println(list);
		
		Subscription original = toAdd.getSubscriptions().get(1L);
		
		Subscription fromReg = list.entrySet().iterator().next().getValue(); 
		
		assertTrue(fromReg.equals(original));
		assertEquals(1, reg.getAllRegisteredTagIds().size());
	}
	
	@Test
	public void testAddSubscriberWithOneErrorSubscription() throws UserNotFoundException {
		Subscriber toAdd = getSubscriber();
		toAdd.addSubscription(new Subscription(toAdd, 1L, 2));
		reg.addSubscriber(toAdd);
		assertEquals(1, reg.getRegisteredUsers().size());
		
		Subscriber inReg = reg.getSubscriber(toAdd.getUserName());
		assertEquals(1, inReg.getSubscribedTagIds().size());
		
		HashMap<Subscriber, Subscription> list = reg.getSubscriptionsForTagId(1L);
		assertEquals(1, list.size());
		
		Subscription fromReg = list.entrySet().iterator().next().getValue();
		assertEquals(fromReg.getNotificationLevel(), 2);
		assertEquals(1, reg.getAllRegisteredTagIds().size());
	}
	
	
	@Test
	public void testSetSubscriber() throws UserNotFoundException {
		Subscriber toAdd = getSubscriber();
		Subscription sub = new Subscription(toAdd, 1L, 2);
		toAdd.addSubscription(sub);
		reg.addSubscriber(toAdd);
		
		Subscriber inReg = reg.getSubscriber(toAdd.getUserName());
		inReg.removeSubscription(sub.getTagId());
		
		reg.setSubscriber(inReg);
		System.out.println(reg);
		
		assertEquals(0, reg.getAllRegisteredTagIds().size());
		assertEquals(0, reg.getSubscriptionsForTagId(sub.getTagId()).size());
	}
	
	
	@Test(expected=UserNotFoundException.class)
	public void testUserNotFoundException() throws UserNotFoundException {
		Subscription sub = getValidSubscription();
		reg.addSubscription(sub);
	}
	
	
	@Test
	public void testRemoveSubscription() throws UserNotFoundException {
		Subscriber toAdd = getSubscriber();
		Subscription sub = new Subscription(toAdd, 1L, 2);
		
		reg.addSubscriber(toAdd);
		System.out.println("Added subscription : "  + reg.toString());

		reg.removeSubscription(sub);
		
		// user stays
		assertEquals(1, reg.getRegisteredUsers().size());
		
		// the subscription should have disappeared
		assertEquals(0, reg.getSubscriptionsForUser(toAdd).size());
		
		System.out.println(reg);
		
	}
	
	@Test
	public void testRemoveSubscriptionForTwo() throws UserNotFoundException {
	    Subscriber first = new Subscriber("test1", "test1@cern.ch", "");
        Subscriber second = new Subscriber("test2", "test2@cern.ch", "");
        
        reg.addSubscriber(first);
        reg.addSubscriber(second);
        
        reg.addSubscription(new Subscription(first, 1L, 1));
        reg.addSubscription(new Subscription(second, 1L, 1));
        System.out.println("Added two subscriptions for same tag  : "  + reg.toString());
        
        assertEquals(2, reg.getSubscriptionsForTagId(1L).size());
        assertEquals(1, reg.getAllRegisteredTagIds().size());
        
        reg.removeSubscription(new Subscription(first, 1L));
        System.out.println("After removing one subscription :" + reg.toString());
        
        assertEquals(1, reg.getSubscriptionsForTagId(1L).size());
        assertEquals(1, reg.getAllRegisteredTagIds().size());
	}
	
	@Test
	public void testUpdateReportInterval() throws UserNotFoundException {
		Subscriber toAdd = getSubscriber();
		reg.addSubscriber(toAdd);
		
		String userId = getSubscriber().getUserName();

		reg.getSubscriber(userId).setReportInterval(20);
		assertEquals(20, reg.getSubscriber(userId).getReportInterval());
		System.out.println(reg.getSubscriber(userId));
	}
	
	
	@Test
	public void testWriteAndReadBack() {
		
		Subscriber toAdd = getSubscriber();
		toAdd.addSubscription(new Subscription(toAdd, 1L, 2));
		toAdd.addSubscription(new Subscription(toAdd, 2L, 2));
		toAdd.addSubscription(new Subscription(toAdd, 3L, 2));
		
		Subscriber another = new Subscriber("Test2", "test2@gmail.com", null);
		another.addSubscription(new Subscription(toAdd, 1L, 2));
		another.addSubscription(new Subscription(toAdd, 2L, 2));
		another.addSubscription(new Subscription(toAdd, 3L, 2));
		
		reg.setSubscriber(toAdd);
		reg.setSubscriber(another);
		//reg.store("test");
		//reg.loadFromFile("test");
	}
	
	
	
}
