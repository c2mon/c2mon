package cern.c2mon.notification.test;

import java.lang.reflect.Type;

import org.junit.Test;

import cern.c2mon.notification.jms.ClientRequest;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class GsonTest {

	private static int cnt = 0; 
	
	
	public Subscriber getNewSubscriber() {
		return new Subscriber("Felix" + (cnt++), "test@gmsil.com", "");
	}
	
	@Test
	public void testSerialise() {

//		ClientRequest req = new ClientRequest(ClientRequest.Type.GetSubscriber,
//				"Felix");
//		Gson gson = new Gson();
//
//		String body = gson.toJson(req);
//		System.out.println(body);
//		
//		gson.fromJson(body, ClientRequest.class);
	}
	
	
	@Test
	public void testSerializeSubscriptionsForUser() {
		Subscriber s = getNewSubscriber(); 
		ClientRequest req = new ClientRequest(ClientRequest.Type.GetSubscriptionsForUser, s);
		s.addSubscription(new Subscription(s, 1L));
		s.addSubscription(new Subscription(s, 2L));
		s.addSubscription(new Subscription(s, 3L));

		Gson gson = new Gson();
		
		
		

		String body = gson.toJson(req);
		
		System.out.println("Generated String from object : \n" + body);
		
		System.out.println("Back to object \n:");
		ClientRequest back = gson.fromJson(body, ClientRequest.class);
		
		System.out.println(back);
		
		Subscriber sback = gson.fromJson((String) back.getBody(), Subscriber.class);
		System.out.println(sback);
	}
	
	

	
}
