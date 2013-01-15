package cern.c2mon.notification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cern.c2mon.notification.impl.NotifierImpl;
import cern.c2mon.notification.impl.TagCache;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.TagNotFoundException;
import cern.c2mon.notification.shared.UserNotFoundException;

/**
 * @author felixehm
 *
 */
public interface SubscriptionRegistry {

	/**
	 * 
	 * @param tagId the tag id to search for.
	 * @return a list of {@link Subscription} objects or and empty list. NEVER NULL!
	 */
	public HashMap<Subscriber, Subscription> getSubscriptionsForTagId(Long tagId);
	
	/**
	 * 
	 * @param subscriber the {@link Subscriber} object to update/add
	 * @throws TagNotFoundException in case one of the associated tags is not defined.
	 */
	public void setSubscriber(Subscriber subscriber) throws TagNotFoundException;
	
	/**
	 * @return a list of all tagids for which a subscription exists.
	 */
	public HashSet<Long> getAllRegisteredTagIds();
	
	/**
	 * @param subscription the subscription to remove.
	 */
	public void removeSubscription(Subscription subscription);

	/**
	 * 
	 * @param subscription the subscription to add
	 * @throws UserNotFoundException in case the user cannot be found
	 */
	public void addSubscription(Subscription subscription) throws UserNotFoundException; 
	
	/**
	 * <b>Note:</b> The returned subscription objects keep a reference to the Subscriber.
	 * @param user the subscriber object which identifies the user
	 * @return a {@link List} of {@link Subscription}s for the passed user. 
	 */
	public List<Subscription> getSubscriptionsForUser(Subscriber user);
	
	/**
	 * 
	 * @param userName the identifier for the user.
	 * @return
	 * @throws UserNotFoundException in case the user cannot be found
	 */
	public Subscriber getSubscriber(String userName) throws UserNotFoundException; 
	
	/**
	 * @return a {@link List} of {@link Subscriber}s which are currently registered.
	 */
	public List<Subscriber> getRegisteredUsers();
	
	/**
	 * 
	 * @return a list of all registered Subscriptions
	 */ 
	public HashSet<Subscription> getRegisteredSubscriptions();
	
	public void reloadConfig();
	
	public void start();

    public void setTagCache(TagCache tagCache);
    
    public TagCache getTagCache();
    
    public void updateLastModificationTime();
}