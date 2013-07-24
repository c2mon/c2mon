/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.client.notification;



import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import cern.c2mon.notification.jms.ClientRequest;
import cern.c2mon.notification.jms.ClientResponse;
import cern.c2mon.notification.jms.ClientRequest.Type;
import cern.c2mon.notification.shared.RemoteServerException;
import cern.c2mon.notification.shared.ServiceException;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.TagNotFoundException;
import cern.c2mon.notification.shared.UserNotFoundException;

import com.google.gson.Gson;

/**
 * 
 * @author felixehm
 */
public class NotificationServiceImpl implements NotificationService {
    /**
     * our Logger
     */
    private Logger logger = Logger.getLogger(NotificationServiceImpl.class);
    
    private static String REQUEST_QUEUE = "";
    
    /**
     * our session for the consumer
     */
    private Session consumerSession;
    /**
     * the connection to the notificaiton service
     */
    private Connection connection;
    /**
     * the queue where the remote notification service listens for requests.
     */
    private String requestQueue = REQUEST_QUEUE;
    /**
     * 
     */
    private String hostName;
    
    /**
     * the timeout a request to the notification service is allowed to take.
     */
    private long requestTimeout = 10000;

    /**
     * @return Returns the requestQueue.
     */
    public String getRequestQueue() {
        return requestQueue;
    }

    /**
     * @param requestQueue The requestQueue to set.
     */
    public void setRequestQueue(String requestQueue) {
        logger.trace("Setting request Queue name to '" + requestQueue + "'");
        this.requestQueue = requestQueue;
    }

    
    
    @Autowired
    public NotificationServiceImpl(ConnectionFactory factory) throws JMSException {
        this.connection = factory.createConnection();
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            hostName = "UNKNOWN";
        }
    }
    
    @PostConstruct
    public void initJms() throws JMSException {
        logger.trace("Initializing JMS connection and session.");
        if (connection == null) {
            throw new IllegalStateException("Connection is closed!");
        }
        if (consumerSession == null) {
            consumerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        }
        connection.start();
    }
    
    @Required
    public void setRequestTimeout(long timeout) {
        logger.trace("Setting request timeout to " + timeout);
        this.requestTimeout = timeout;
    }
    
    
    /**
     * 
     * @param request the {@link ClientRequest}
     * @return a {@link ClientResponse}
     * @throws ServiceException if the notification service cannot be contacted.
     * @throws RemoteServerException if an error occurred on the server side.
     */
    private ClientResponse getResponse(ClientRequest request) throws ServiceException, RemoteServerException {
        if (logger.isTraceEnabled()) {
            logger.trace("entering getResponse() for request " + request.getType());
        }
        
        try {
            initJms();
        } catch (JMSException e) {
            throw new ServiceException(e.getMessage());
        }
        request.setOriginHostName(hostName);
        
        ClientResponse response = null;
        Gson gson = new Gson();
        String toSendText = gson.toJson(request.getBody());
        
        MessageProducer producer = null;
        Session producerSession = null;
        MessageConsumer myConsumer = null;
        
        // prepare and send the request to the server:
        try {
            Destination replyTo = consumerSession.createTemporaryQueue();
            myConsumer = consumerSession.createConsumer(replyTo);
            TextMessage toSend = consumerSession.createTextMessage(toSendText);
            toSend.setJMSCorrelationID(request.getId());
            toSend.setJMSReplyTo(replyTo);
            
            toSend.setStringProperty("TYPE", request.getType().toString());
            toSend.setStringProperty("FROM", request.getOriginHostName());
            
            producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = producerSession.createProducer(null);
            
            if (logger.isInfoEnabled()) {
                logger.info(request.getId() + " Sending request and waiting on " + replyTo.toString() + " for reply");
                if (logger.isTraceEnabled()) {
                    logger.trace(request.getId() + " Message Content : " + toSendText);
                }
            }
            
            producer.send(producerSession.createQueue(getRequestQueue()), toSend, DeliveryMode.PERSISTENT, 0, requestTimeout);
            
            if (logger.isInfoEnabled()) {
                logger.info(request.getId() + " Waiting " + requestTimeout + "msec for answer from remote server...");
            }
            TextMessage fromServer = (TextMessage)myConsumer.receive(requestTimeout);
            
            if (fromServer == null) {
                throw new ServiceException(request.getId() + " Timeout while trying to get answer from remote notification service!");
            }
            logger.trace("Got message from server : " + fromServer.getText());
            
            response = gson.fromJson(fromServer.getText(), ClientResponse.class);
            
            logger.trace("Transformed message from server to :\n" + response);
            
        }catch(Exception ex) {
            // an error on OUR side. Need of course to publish this.
            throw new ServiceException(ex);
        } finally {
            if (producer != null) {
                try {producer.close();} catch (JMSException e) {
                    e.printStackTrace();
                }
            }
            if (myConsumer != null) {
                try {myConsumer.close();} catch (JMSException e) {
                    e.printStackTrace();
                }
            }
            if (producerSession != null) {
                try {producerSession.close();} catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // handle remote server exceptions:
        if (response.getType().equals(ClientResponse.Type.ErrorResponse)) {
            logger.error("Got error from server : "  + (String) response.getBody());
            throw new RemoteServerException((String)response.getBody());
        } else if (response.getType().equals(ClientResponse.Type.UserNotFoundError)) {
            throw new UserNotFoundException((String)response.getBody());
        }else if (response.getType().equals(ClientResponse.Type.TagNotFoundError)) {
            throw new TagNotFoundException((String)response.getBody());
        }
        // ClientResponse for the API calls
        return response;
        
    }
    
   
    @Override
    public Subscriber getSubscriber(String userName) throws ServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("entering getSubscriber() for User=" + userName);
        }
        ClientRequest request = new ClientRequest(Type.GetSubscriber, userName);
        ClientResponse response = getResponse(request);
        Subscriber subscriber = null;
        Gson gson = new Gson();
        try {
            subscriber = gson.fromJson((String)response.getBody(), Subscriber.class);
        }catch(Exception ex) {
            throw new ServiceException("Can't read message body from server " + response.getType() + " response: " + ex.getMessage());
        }
        return subscriber;
    }

    @Override
    public boolean isSubscribed(String user, Long tagId) throws UserNotFoundException, ServiceException {
        throw new ServiceException("Not yet implemented.");
    }

    private void removeSubscription(Subscription subscription) throws UserNotFoundException, ServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("entering removeSubscription() for User=" + subscription.getSubscriberId() + " and TagID=" + subscription.getTagId());
        }
        ClientRequest request = new ClientRequest(Type.RemoveSubscription, subscription);
        getResponse(request);
    }

    @Override
    public void removeSubscription(String user, Long tagId) throws UserNotFoundException, ServiceException {
        removeSubscription(new Subscription(user,tagId));
    }

    @Override
    public Subscriber setSubscriber(Subscriber sub) throws ServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("entering setSubscriber() for User=" + sub.getUserName());
            
            if (logger.isTraceEnabled()) {
                logger.trace("Subscriber object: " + sub);
            }
        }
        
        ClientRequest request = new ClientRequest(Type.UpdateSubscriber, sub);
        ClientResponse reponse = getResponse(request);
        return getSubscriberFromClientResponse(reponse);
    }

    private Subscriber subscribe(Subscription subscription) throws UserNotFoundException, ServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("entering subscribe() for User=" + subscription.getSubscriberId() + " and TagID=" + subscription.getTagId());
        }
        ClientRequest request = new ClientRequest(Type.AddSubscription, subscription);
        ClientResponse reponse = getResponse(request);
        return getSubscriberFromClientResponse(reponse);
    }

    @Override
    public Subscriber subscribe(String userId, Long tagId) throws UserNotFoundException, ServiceException {
        Subscription sub = new Subscription(userId, tagId);
        return subscribe(sub);
    }

    @Override
    public void addSubscriber(Subscriber sub) throws ServiceException {
        setSubscriber(sub);
    }

    
    private Subscriber getSubscriberFromClientResponse(ClientResponse response) {
        Subscriber subscriber = null;
        Gson gson = new Gson();
        try {
            subscriber = gson.fromJson((String)response.getBody(), Subscriber.class);
        }catch(Exception ex) {
            throw new ServiceException("Can't read message body from server " + response.getType() + " response: " + ex.getMessage());
        }
        return subscriber;
    }
    
}
