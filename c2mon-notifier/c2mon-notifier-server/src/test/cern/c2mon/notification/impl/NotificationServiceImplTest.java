/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import cern.c2mon.notification.impl.NotificationServiceImpl;
import cern.c2mon.notification.impl.SubscriptionRegistryImpl;
import cern.c2mon.notification.jms.ClientRequest;
import cern.c2mon.notification.jms.ClientResponse;
import cern.c2mon.notification.shared.ServiceException;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;

import com.google.gson.Gson;

/**
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class NotificationServiceImplTest {

    private Gson gson = new Gson();
    
    private NotificationServiceImpl service;
    
    
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public NotificationServiceImplTest() {
        
    }

    @BeforeClass
    public static void initLog4J() {
        System.setProperty("log4j.configuration", NotificationServiceImplTest.class.getResource("log4j.properties").toExternalForm());
        System.out.println(System.getProperty("log4j.configuration"));
    }
    
    public Subscriber getValidSubscriber() {
        return new Subscriber("test", "test@cern.ch", "");
    }
    
    @Before
    public void initServiceAndReg() {
        service = new NotificationServiceImpl(new SubscriptionRegistryTest.RegWithoutDb());
    }
    
    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    @Test
    public void testSetSubscriber() {
        System.out.println("starting testSetSubscriber()");
        Subscriber s = getValidSubscriber();
        
        ClientRequest request = new ClientRequest(ClientRequest.Type.UpdateSubscriber, gson.toJson(s));
        System.out.println("Subscriber object to register : " + s);
        ClientResponse resp = service.prepareResponse(request);
        System.out.println("Server Response : " + resp.getType());
        
        
        if (resp.getType().equals(ClientResponse.Type.ErrorResponse)) {
            fail((String) resp.getBody());
        }
    }

    @Test
    public void testSetAndGetSameSubscriber() {
        System.out.println("starting testSetAndGetSameSubscriber()");
        Subscriber s = getValidSubscriber();
        ClientResponse resp = null;
        ClientRequest request = null;
        
        request = new ClientRequest(ClientRequest.Type.UpdateSubscriber, gson.toJson(s));
        System.out.println("Subscriber object to register : " + s);
        resp = service.prepareResponse(request);
        
        request = new ClientRequest(ClientRequest.Type.GetSubscriber, getValidSubscriber().getUserName());
        resp = service.prepareResponse(request);
        System.out.println("Server Response : " + resp.getType());
        if (resp.getType().equals(ClientResponse.Type.ErrorResponse)) {
            fail((String) resp.getBody());
        }
        
        Subscriber fromReg = gson.fromJson((String) resp.getBody(), Subscriber.class);
        assertTrue(fromReg.equals(s));
    }
    
    @Test
    public void testSetAndGetSubscriberWithSubscriptions() {
        System.out.println("starting testSetAndGetSubscriberWithSubscriptions()");
        Subscriber s = getValidSubscriber();
        ClientResponse resp = null;
        ClientRequest request = null;
        
        s.addSubscription(new Subscription(s, 1L));
        s.addSubscription(new Subscription(s, 2L));
        s.addSubscription(new Subscription(s, 3L));
        
        request = new ClientRequest(ClientRequest.Type.UpdateSubscriber, gson.toJson(s.getCopy()));
        System.out.println("Subscriber object to register : " + s);
        resp = service.prepareResponse(request);
        
        request = new ClientRequest(ClientRequest.Type.GetSubscriber, getValidSubscriber().getUserName());
        resp = service.prepareResponse(request);
        System.out.println("Server Response : " + resp.getType());
        if (resp.getType().equals(ClientResponse.Type.ErrorResponse)) {
            fail((String) resp.getBody());
        }
        
        Subscriber fromReg = gson.fromJson((String) resp.getBody(), Subscriber.class);
        assertTrue(fromReg.equals(s));
        System.out.println("Subscriber object from Server : " + fromReg);
        assertEquals(s.getSubscriptions().size(), fromReg.getSubscriptions().size());
    }
    
    
    @Test
    public void testUpdateWithNewSubscription() {
        
        System.out.println("starting testUpdateWithNewSubscription()");
        Subscriber s = getValidSubscriber();
        ClientResponse resp = null;
        ClientRequest request = null;
        
        
        s.addSubscription(new Subscription(s, 1L));
        request = new ClientRequest(ClientRequest.Type.UpdateSubscriber, gson.toJson(s));
        System.out.println("Subscriber object to register : " + s);
        resp = service.prepareResponse(request);
        
        // add subscription to local subscriber and update the notificaiton service.
        s.addSubscription(new Subscription(s, 2L));
        request = new ClientRequest(ClientRequest.Type.UpdateSubscriber, gson.toJson(s));
        System.out.println("Subscriber object to register : " + s);
        service.prepareResponse(request);
        
        
        // get the subscriber and check for the 2 subscriptions 
        request = new ClientRequest(ClientRequest.Type.GetSubscriber, getValidSubscriber().getUserName());
        resp = service.prepareResponse(request);
        System.out.println("Server Response : " + resp.getType());
        if (resp.getType().equals(ClientResponse.Type.ErrorResponse)) {
            fail((String) resp.getBody());
        }
        Subscriber fromReg = gson.fromJson((String) resp.getBody(), Subscriber.class);
        System.out.println("Subscriber object from Server : " + fromReg);
        assertEquals(2, fromReg.getSubscriptions().size());
    }
    
    
    @Test
    public void testAddsubscriptionCall() {
        System.out.println("starting testAddsubscriptionCall()");
        Subscriber s = getValidSubscriber();
        ClientResponse resp = null;
        ClientRequest request = null;
        
        request = new ClientRequest(ClientRequest.Type.UpdateSubscriber, gson.toJson(s));
        service.prepareResponse(request);
        
        Subscription sub = new Subscription(s, 1L);
        System.out.println("Adding Subscription : " + sub);
        request = new ClientRequest(ClientRequest.Type.AddSubscription, gson.toJson(sub));
        resp = service.prepareResponse(request);
        
        if (resp.getType().equals(ClientResponse.Type.ErrorResponse)) {
            fail((String) resp.getBody());
        }
        
        request = new ClientRequest(ClientRequest.Type.GetSubscriber, getValidSubscriber().getUserName());
        System.out.println("Getting Subscriber : " + s);
        resp = service.prepareResponse(request);
        System.out.println("Server Response : " + resp.getType());
        Subscriber fromReg = gson.fromJson((String) resp.getBody(), Subscriber.class);
        
        System.out.println("Subscriber object from Server : " + fromReg);
        assertEquals(1, fromReg.getSubscriptions().size());
    }
    
    
    @Test
    public void useIntermediateBroker() throws Exception {
        BrokerService broker = null;
        try {
            broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.setUseShutdownHook(false);
            broker.setSystemExitOnShutdown(true);
            broker.start();
            
            String requestQueue = "requests";
            
            ConnectionFactory fac = new ActiveMQConnectionFactory("vm://localhost");

            NotificationServiceImpl service = new NotificationServiceImpl(new SubscriptionRegistryTest.RegWithoutDb());
            JmsTemplate responderForRequests = new JmsTemplate(fac);
            responderForRequests.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            responderForRequests.setPriority(0);
            responderForRequests.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
            service.setClientResponseJmsTemplate(responderForRequests);
    
            Connection connection = fac.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(session.createQueue(requestQueue));
            consumer.setMessageListener(service);
            connection.start();
            
            // create the client and connect it to the same internal broker.
            cern.c2mon.client.notification.NotificationServiceImpl clientService = new cern.c2mon.client.notification.NotificationServiceImpl(fac);
            clientService.setRequestQueue(requestQueue);
            clientService.setRequestTimeout(1000L);
            
            Subscriber s = getValidSubscriber();
            try {
                clientService.setSubscriber(s);
            } catch (ServiceException sx) {
                System.err.println(sx.getMessage());
                fail(sx.getMessage());
            }
            Subscriber fromServer = clientService.getSubscriber(s.getUserName());
            assertTrue(s.equals(fromServer));
            
            System.out.println("Got subscriber from Server : "  + fromServer);
        } finally {
            broker.stop();
        }
    }
    
}

