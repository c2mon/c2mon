/**
 * 
 */
package cern.c2mon.notification.jms;

import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;


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
	
	
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(getType().toString());
//	    if (getType().equals(Type.AddSubscription) || getType().equals(Type.RemoveSubscription)) {
//	        Subscription s = ((Subscription) body);
//	        sb.append(s.toString());
//	    } else if (getType().equals(Type.GetSubscriber) || getType().equals(Type.GetSubscriptionsForUser)) {
//	        sb.append((String) body);
//	    } else if (getType().equals(Type.UpdateSubscriber)) {
//            sb.append(((Subscriber) body).toString());
//        } else {
//            sb.append("Body unknown!");
//        }
	    return sb.toString();
	}
	
}
