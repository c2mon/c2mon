/**
 * 
 */
package cern.c2mon.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.notification.impl.SubscriptionRegistryImpl;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.TagNotFoundException;
import cern.c2mon.notification.shared.UserNotFoundException;
import cern.dmn2.core.Status;

/**
 * @author felixehm
 *
 */
public class SubscriptionRegistryTest {

	SubscriptionRegistryImpl reg = null;
	
	/**
	 * 
	 */
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
	
	public static Subscriber getNonExistingSubscriber() {
	    return new Subscriber("non-existent", "no", "no");
	}
	
	/**
     * our private little RegistryImpl which does not use the DB for the unit tests
     * 
     */
    public static class RegWithoutDb extends SubscriptionRegistryImpl {
        /**
         * 
         */
        public RegWithoutDb() {
            super.setAutoSaveInterval(0);
            super.setBackUpWriter(null);
            users.clear();
        }
        @Override
        public Subscriber getSubscriber(String userName) {
            
            if (userName.toUpperCase().equals(getNonExistingSubscriber().getUserName().toUpperCase())) {
                throw new UserNotFoundException("User " + userName + " not found.");
            }
            
            if (super.users.containsKey(userName)) {
                return super.users.get(userName);
            } else {
                Subscriber s = new Subscriber(userName, userName + "@cern.ch", null);
                super.users.put(userName, s);
                return s;
            }
        }
    }
	
	/**
	 * 
	 */
	@Before
	public void initRegistry() {
		reg = new RegWithoutDb();
	}
	
	/**
	 * 
	 * @throws UserNotFoundException in case the user cannot be found
	 * @throws TagNotFoundException in case the tag cannot be found
	 */
	@Test
	public void testAddSubscriber() throws UserNotFoundException, TagNotFoundException {
		
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
	
	/**
	 * 
     * @throws UserNotFoundException in case the user cannot be found
     * @throws TagNotFoundException in case the tag cannot be found
	 */
	@Test
	public void testAddSubscriberWithEmptySubscriptions() throws UserNotFoundException, TagNotFoundException {
		Subscriber toAdd = getSubscriber();
		reg.addSubscriber(toAdd);
		assertEquals(1, reg.getRegisteredUsers().size());
		
		Subscriber inReg = reg.getSubscriber(toAdd.getUserName());
		assertEquals(inReg, toAdd);
		assertEquals(0, inReg.getSubscribedTagIds().size());
		assertEquals(0, reg.getAllRegisteredTagIds().size());
		assertEquals(1, reg.getRegisteredUsers().size());
	}
	
	/**
	 * 
	 * @throws UserNotFoundException in case the user cannot be found
	 * @throws TagNotFoundException in case the tag cannot be found 
	 */
	@Test
	public void testAddSubscriberWithOneDefaultSubscription() throws UserNotFoundException, TagNotFoundException {
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
	
	/**
	 * 
	 * @throws UserNotFoundException in case the user cannot be found
	 * @throws TagNotFoundException in case the tag cannot be found  
	 */
	@Test
	public void testAddSubscriberWithOneErrorSubscription() throws UserNotFoundException, TagNotFoundException {
		Subscriber toAdd = getSubscriber();
		toAdd.addSubscription(new Subscription(toAdd, 1L, 2));
		reg.addSubscriber(toAdd);
		assertEquals(1, reg.getRegisteredUsers().size());
		
		Subscriber inReg = reg.getSubscriber(toAdd.getUserName());
		assertEquals(1, inReg.getSubscribedTagIds().size());
		
		HashMap<Subscriber, Subscription> list = reg.getSubscriptionsForTagId(1L);
		assertEquals(1, list.size());
		
		Subscription fromReg = list.entrySet().iterator().next().getValue();
		assertEquals(fromReg.getNotificationLevel(), Status.WARNING);
		assertEquals(1, reg.getAllRegisteredTagIds().size());
	}
	
	/**
     * 
     * @throws UserNotFoundException in case the user cannot be found
     * @throws TagNotFoundException in case the tag cannot be found  
     */
	@Test
	public void testSetSubscriber() throws UserNotFoundException, TagNotFoundException {
		Subscriber toAdd = getSubscriber();
		Subscription sub = new Subscription(toAdd, 1L, 2);
		toAdd.addSubscription(sub);
		reg.setSubscriber(toAdd);
		
		Subscriber inReg = reg.getSubscriber(toAdd.getUserName());
		
		assertEquals(1, inReg.getSubscribedTagIds().size());
		assertEquals(sub, inReg.getSubscription(sub.getTagId()));
		
		inReg.removeSubscription(sub.getTagId());
		
		reg.setSubscriber(inReg);
		System.out.println(reg);
		
		assertEquals(0, reg.getAllRegisteredTagIds().size());
		assertEquals(0, reg.getSubscriptionsForTagId(sub.getTagId()).size());
	}
	
	
	@Test(expected = UserNotFoundException.class)
	public void testUserNotFoundException() throws UserNotFoundException {
	    Subscription forInvalidUser = new Subscription(getNonExistingSubscriber().getUserName(),1L); 
		reg.addSubscription(forInvalidUser);
	}
	
	/**
     * 
     * @throws UserNotFoundException in case the user cannot be found
     * @throws TagNotFoundException in case the tag cannot be found  
     */
	@Test
	public void testRemoveSubscription() throws UserNotFoundException, TagNotFoundException {
		Subscriber toAdd = getSubscriber();
		Subscription sub = new Subscription(toAdd, 1L, 2);
		
		reg.addSubscriber(toAdd);
		System.out.println("Added subscription : "  + reg.toString());

		reg.removeSubscription(sub);
		
		// user stays
		assertEquals(1, reg.getRegisteredUsers().size());
		
		// the subscription should have disappeared
		assertEquals(0, reg.getSubscriptionsForUser(toAdd).size());
		assertEquals(0, toAdd.getSubscriptions().size());
		assertEquals(0, reg.getSubscriber(toAdd.getUserName()).getSubscriptions().size());
		
		System.out.println(reg);
		
	}
	
	/**
     * 
     * @throws UserNotFoundException in case the user cannot be found
     * @throws TagNotFoundException in case the tag cannot be found  
     */
	@Test
	public void testRemoveSubscriptionForTwo() throws UserNotFoundException, TagNotFoundException {
	    Subscriber first = new Subscriber("test1", "test1@cern.ch", "");
        Subscriber second = new Subscriber("test2", "test2@cern.ch", "");
        
        /*
         * we need a copy as we assume this call has been made over the network.
         */
        reg.addSubscriber(first.getCopy());
        reg.addSubscriber(second.getCopy());
        
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
	
	/**
     * 
     * @throws UserNotFoundException in case the user cannot be found
     * @throws TagNotFoundException in case the tag cannot be found  
     */
	@Test
	public void testUpdateReportInterval() throws UserNotFoundException, TagNotFoundException {
		Subscriber toAdd = getSubscriber();
		reg.addSubscriber(toAdd);
		
		String userId = getSubscriber().getUserName();

		reg.getSubscriber(userId).setReportInterval(20);
		assertEquals(20, reg.getSubscriber(userId).getReportInterval());
		System.out.println(reg.getSubscriber(userId));
	}
	
	/**
     * 
     * @throws TagNotFoundException in case the tag cannot be found  
     */
	@Test
	public void testWriteAndReadBack() throws TagNotFoundException {
		
		Subscriber toAdd = getSubscriber();
		toAdd.addSubscription(new Subscription(toAdd, 1L, 2));
		toAdd.addSubscription(new Subscription(toAdd, 2L, 2));
		toAdd.addSubscription(new Subscription(toAdd, 3L, 2));
		
		Subscriber another = new Subscriber("Test2", "test2@gmail.com", null);
		another.addSubscription(new Subscription(another, 1L, 2));
		another.addSubscription(new Subscription(another, 2L, 2));
		another.addSubscription(new Subscription(another, 3L, 2));
		
		reg.setSubscriber(toAdd);
		reg.setSubscriber(another);
		//reg.store("test");
		//reg.loadFromFile("test");
	}
	
	
	@Test
	public void testDeleteallSubscriptionsBySetCall() {
	    Subscriber toAdd = getSubscriber();
        toAdd.addSubscription(new Subscription(toAdd, 1L, 2));
        toAdd.addSubscription(new Subscription(toAdd, 2L, 2));
        toAdd.addSubscription(new Subscription(toAdd, 3L, 2));
        
        reg.setSubscriber(toAdd.getCopy());
        
        toAdd.getSubscriptions().clear();
        reg.setSubscriber(toAdd.getCopy());
	}
	
	
}
