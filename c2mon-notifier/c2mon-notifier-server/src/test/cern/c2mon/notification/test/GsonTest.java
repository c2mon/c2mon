package cern.c2mon.notification.test;

import java.util.HashMap;

import org.junit.Test;

import cern.c2mon.notification.jms.ClientRequest;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;

import com.google.gson.Gson;

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
		s.addSubscription(new Subscription(s, 1L));
		s.addSubscription(new Subscription(s, 2L));
		s.addSubscription(new Subscription(s, 3L));

		Gson gson = new Gson();
		String text = gson.toJson(s);

		System.out.println("Generated String from object : \n" + text);
		
		System.out.println("Back to object :\n ");
		Subscriber back = gson.fromJson(text, Subscriber.class);
		
		System.out.println(back);
		
//		Subscriber sback = gson.fromJson((String) back.getBody(), Subscriber.class);
//		System.out.println(sback);
	}
	
	
	@Test
	public void testRubbish() {
	    Gson gson = new Gson();
	    Rubbish rubbish = new Rubbish();

        String body = gson.toJson(rubbish);
        
        System.out.println(body);
        
        Rubbish back = gson.fromJson(body, Rubbish.class);
        System.out.println(back);
        
        if (back.getType() == 1) {
            RubbishOne c = (RubbishOne) back.getObject();
        } else if (back.getType() == 2) {
            RubbishTwo c = (RubbishTwo) back.getObject();
        }
	}
	
	
	private class Rubbish {
	    
	    private Object child;
	    public final int type;
	    
	    public Rubbish() {
	        child = new RubbishOne();
	        type = 1;
	    }
	    public int getType() {
	        return type;
	    }
	    public Object getObject() {
	        return child;
	    }
	}
	
	
	private class RubbishOne {
	    private String name;
	    
	    public RubbishOne() {
	        this.name = "Rubbish1";
	    }
	}
	private class RubbishTwo {
        private String name;
        
        public RubbishTwo() {
            this.name = "Rubbish2";
        }
    }
}
