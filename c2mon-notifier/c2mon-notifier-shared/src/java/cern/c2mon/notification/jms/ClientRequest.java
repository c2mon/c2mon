/**
 * 
 */
package cern.c2mon.notification.jms;


/**
 * @author felixehm
 *
 */
public class ClientRequest extends RemoteObject {

	public static enum Type {
		AddSubscription,
		GetSubscriptionsForUser,
		GetSubscriber,
		RemoveUserResponse,
		RemoveSubscription,
		UpdateSubscriber;
	}
	
	private Object body;
	
	private final Type type;

	public ClientRequest(Type type, Object body) {
		this.body = body;
		this.type = type;
	}
	
	public void setBody(Object obj) {
		this.body = obj;
	}
	
	public Type getType() {
		return type;
	}
	
	public Object getBody() {
		return body;
	}
	
	
	
}
