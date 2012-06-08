/**
 * 
 */
package cern.c2mon.notification.shared;

/** Represents an error that a user was not found in the system.
 * @author felixehm
 *
 */
public class UserNotFoundException extends ServiceException {

	public UserNotFoundException(String message) {
		super(message); 
	}
	
}
