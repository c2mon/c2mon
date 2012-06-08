/**
 * 
 */
package cern.c2mon.client.notification;

import cern.c2mon.notification.shared.ServiceException;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.UserNotFoundException;

/**
 * @author felixehm
 *
 */
public interface NotificationService {

	/** Subscribes to WARNING level of the tag Id.
	 * 
	 * @param subscription the new subscription  
	 * @throws UserNotFoundException in case the user can't be found.
	 * @throws ServiceException in case of a problem while using the service.
	 */
	public void subscribe(Subscription subscription) throws UserNotFoundException, ServiceException;
	
	/**
	 * 
	 * @param sub
     * @throws ServiceException in case of a problem while using the service.
	 */
	public void setSubscriber(Subscriber sub) throws ServiceException;
	
	/** Subscribes to the tagId for notification.<br><br>
	 * 
	 * SMS will not be enabled by default.<br>
	 * WARING level is the threshold<br>
	 * 
	 * @param userId the id of the user
	 * @param tagId the tagId to subscribe to.
	 * @throws UserNotFoundException in case the user with userId was not found.
     * @throws ServiceException in case of a problem while using the service.
	 */
	public void subscribe(String userId, Long tagId) throws UserNotFoundException, ServiceException;
	
	/**
	 * @param userName the unique identifier of the user. 
	 * @return the {@link Subscriber} object with all its {@link Subscription} objects. 
	 * @throws UserNotFoundException in case the user cannot be found
     * @throws ServiceException in case of a problem while using the service.
	 */
	public Subscriber getSubscriber(String userName) throws UserNotFoundException, ServiceException;
	
	/**
	 * 
	 * @param user a valid, in the system existing {@link Subscriber} object.
	 * @param tagId the data tag id [long]
	 * @return true, in case the user is subscribed to this data tag.
	 * @throws UserNotFoundException in case the user can't be found.
     * @throws ServiceException in case of a problem while using the service.
	 */
	public boolean isSubscribed(Subscriber user, Long tagId) throws UserNotFoundException, ServiceException;

	/**
	 * @param subscription a valid, in the system existing {@link Subscription} object.
	 * @throws UserNotFoundException in case the associated user cannot be found
     * @throws ServiceException in case of a problem while using the service.
	 */
	public void removeSubscription(Subscription subscription) throws UserNotFoundException, ServiceException;
	
	/**
	 * @param user a valid, in the system existing {@link Subscriber} object.
	 * @param tagId the data tag id [long]
	 * @throws UserNotFoundException in case the associated user cannot be found
     * @throws ServiceException in case of a problem while using the service.
	 */
	public void removeSubscription(Subscriber user, Long tagId) throws UserNotFoundException, ServiceException;
	
}
