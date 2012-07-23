package cern.c2mon.notification.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;

public class SubscriberTest {

	private Subscriber getSubscriber() {
		return new Subscriber("TestName", "test@gmail.com", "");
	}
	
	@Test
	public void testCreateSubscriber() {
		Subscriber s = getSubscriber();
	}
	
	@Test
	public void testSameSubscriptionObjectTwice() {
		Subscriber s = getSubscriber();
		assertEquals(0, s.getSubscriptions().size());
		Subscription sub = new Subscription(s, 1L);
		s.addSubscription(sub);
		s.addSubscription(sub);
		assertEquals(1, s.getSubscriptions().size());
	}
	
	
	@Test
	public void testSameSubscriptionInformationTwice() {
		Subscriber s = getSubscriber();
		s.addSubscription(new Subscription(s, 1L));
		s.addSubscription(new Subscription(s, 1L));
		assertEquals(1, s.getSubscriptions().size());
	}
	
	
	@Test
	public void testAddSubscription() {
		Subscriber s = getSubscriber();
		s.addSubscription(new Subscription(s, 1L));
		assertEquals(1, s.getSubscriptions().size());
	}
	
	@Test
	public void testRemoveSubscription() {
		Subscriber s = getSubscriber();
		Subscription sub = new Subscription(s, 1L);
		s.addSubscription(sub);
		s.removeSubscription(sub.getTagId());
		assertEquals(0, s.getSubscriptions().size());
	}
	
	@Test
	public void testRemoveSubscriptionbyTagId() {
		Subscriber s = getSubscriber();
		Subscription sub = new Subscription(s, 1L);
		s.addSubscription(sub);
		s.removeSubscription(1L);
		assertEquals(0, s.getSubscriptions().size());
	}

	
	@Test
	public void testGetSubscribedTagIds() {
		Subscriber s = getSubscriber();
		Subscription sub = new Subscription(s, 1L);
		s.addSubscription(sub);
		List<Long> subsTagIds = s.getSubscribedTagIds();
		assertEquals(1, subsTagIds.size());
		assertTrue(1L == subsTagIds.get(0));
	}
	
	
}
