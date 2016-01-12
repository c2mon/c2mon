/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
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
