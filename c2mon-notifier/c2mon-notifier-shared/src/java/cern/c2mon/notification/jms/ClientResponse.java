/**
 * 
 */
package cern.c2mon.notification.jms;

/**
 * @author felixehm
 *
 */
public class ClientResponse extends RemoteObject {

	public static enum Type {
		AddSubscriptionReponse,
		GetSubscriptionsForUserReponse,
		GetSubscriberReponse,
		RemoveUserReponse,
		RemoveSubscriptionReponse,
		UpdateSubscriberReponse,
		EmptyResponse,
		ErrorResponse,
		UserNotFoundError,
		TagNotFoundError;
	}
	
	private final Object body;
	
	private final Type type;

	public ClientResponse(Type type, Object body) {
		this.body = body;
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
	public Object getBody() {
		return body;
	}
	
	public String toString() {
	    return "ClientResponse from " + super.getOriginHostName() + " with Type=" + type.toString();
	}
}
