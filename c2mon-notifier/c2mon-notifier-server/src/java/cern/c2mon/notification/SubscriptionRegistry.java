/**
 * 
 */
package cern.c2mon.notification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
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
	 */
	public void setSubscriber(Subscriber subscriber);
	
	/**
	 * @return a list of all tagids for which a subscription exists.
	 */
	public HashSet<Long> getAllRegisteredTagIds();
	
	/**
	 * @param subscription the subscription to remove.
	 */
	public void removeSubscription(Subscription subscription);
//	
//	/**
//	 * 
//	 * @param user the user identifier
//	 * @param tagId the tag id
//	 * @param level the subscription level
//	 */
//	public void addSubscription(Subscriber user, Long tagId, int level);
//	
//	/**
//	 * 
//	 * @param user the user identifier
//	 * @param tagId the tag id
//	 */
//	public void addSubscription(Subscriber user, Long tagId);
//	
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
	
	public void reloadConfig();
	
}