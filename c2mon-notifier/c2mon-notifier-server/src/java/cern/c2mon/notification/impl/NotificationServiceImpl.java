/**
 * 
 */
package cern.c2mon.notification.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.jms.ClientRequest;
import cern.c2mon.notification.jms.ClientResponse;
import cern.c2mon.notification.jms.ClientRequest.Type;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.TagNotFoundException;
import cern.c2mon.notification.shared.UserNotFoundException;

import com.google.gson.Gson;

/** A service which provides notification to subscribers of tags. 
 *   
 * @author felixehm
 *
 */
public class NotificationServiceImpl implements MessageListener {
    /**
     * our Logger.
     */
	private Logger logger = Logger.getLogger(NotificationServiceImpl.class);
	
	/**
	 * our registry for keeping subscriptions.
	 */
	private SubscriptionRegistry registry;
	
	/**
	 * our jms template to answer client requets. 
	 */
	private JmsTemplate jmsTemplate; 

	/**
	 * {@link Gson} instance to re-construct and answer client requests.
	 */
	private Gson gson = new Gson();
	
	/**
	 * our hostname which we keep to set it in the client responses. We do this to avoid retrieving it everytime we send our message.
	 * @see NotificationServiceImpl#sendClientResponse(Destination, String)
	 */
	private String hostName;
	
	
	/** Empty Constructor.
	 * 
	 * @param registry The {@link SubscriptionRegistry} we want to use for subscriptions.
	 */
	public NotificationServiceImpl(SubscriptionRegistry registry) {
		this.registry = registry;
		try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            hostName = "Unknown";
        }
        logger.info("NotificationService started.");
        
	}
	
	@Required
	public void setClientResponseJmsTemplate(JmsTemplate jmsTemplate) {
	    this.jmsTemplate = jmsTemplate;
	}
	
	/**
	 * 
	 * @param destination the JMS destination to reply to. 
	 * @param response the {@link ClientResponse} to send.
	 */
	public void sendClientResponse(Destination destination, final String response) {
		jmsTemplate.send(destination, new MessageCreator() {
			@Override
			public Message createMessage(Session arg0)
					throws JMSException {
			    TextMessage msg = arg0.createTextMessage(response);
				return msg;
			}
		});
	}
	
	/** 
	 * 
	 * @param request the request from he client.
	 * @return a valid {@link ClientResponse}
	 */
	public ClientResponse prepareResponse(ClientRequest request) {
	    ClientResponse response = new ClientResponse(ClientResponse.Type.EmptyResponse, "");
	    
	    if (logger.isDebugEnabled()) {
	        logger.debug(request.getId() + " Handling " + request.getType() + " from " + request.getOriginHostName());
        }
	    if (logger.isTraceEnabled()) {
	        logger.trace(request.getId() + " Content of request " + request.getBody());
	    }
	    
        try {
            String body = (String) request.getBody();
            switch (request.getType()) {
            case UpdateSubscriber:
                Subscriber subscriber = gson.fromJson(body, Subscriber.class);
                registry.setSubscriber(subscriber);
                response = new ClientResponse(
                        ClientResponse.Type.GetSubscriberReponse, gson.toJson(
                                registry.getSubscriber(subscriber.getUserName())));
                break;
            case GetSubscriber:
                String userName = gson.fromJson(body, String.class);
                Subscriber toSend = registry.getSubscriber(userName);
                response = new ClientResponse(
                        ClientResponse.Type.GetSubscriberReponse, gson.toJson(toSend)
                        );
                break;
            case AddSubscription:
                Subscription subscription = gson.fromJson(body, Subscription.class);
                registry.addSubscription(subscription);
                break;
            case RemoveSubscription:
                registry.removeSubscription(gson.fromJson(body, Subscription.class));
                break;
            default:
                response = new ClientResponse(ClientResponse.Type.ErrorResponse, "I do not understand this request type : " + request.getType());
            }
        } catch (UserNotFoundException notFound) {
            response = new ClientResponse(ClientResponse.Type.UserNotFoundError, notFound.getMessage());
        } catch (TagNotFoundException notFound) {
            response = new ClientResponse(ClientResponse.Type.TagNotFoundError, notFound.getMessage());
        }
        response.setOriginHostName(hostName);
        response.setId(request.getId());
	    return response;
	}
	
	
	/**
	 * Method called for any client request. It deserializes the message using {@link #gson} 
	 * and prepares the answer with {@link #prepareResponse(ClientRequest)}.   
	 * 
	 * All Exceptions will be wrapped and passed to the client.
	 * 
	 * @param message the JMS Message from the client.
	 */
	public void onMessage(Message message) {

		if (message instanceof TextMessage) {
			TextMessage text = (TextMessage) message;
			Destination replyDestination = null;
			ClientResponse response = null;
			
			final Gson gson = new Gson();

			try {
			    if (logger.isInfoEnabled()) {
	                logger.info("Handling message " + message.getJMSCorrelationID() + ". Need to respond to " + message.getJMSReplyTo());
	            }
			    
			    ClientRequest request = new ClientRequest(Type.valueOf(message.getStringProperty("TYPE")), text.getText());
			    request.setId(message.getJMSCorrelationID());
			    request.setOriginHostName(message.getStringProperty("FROM"));
			    //gson.fromJson(text.getText(), ClientRequest.class);
			    
			    if (logger.isDebugEnabled()) {
			        logger.debug(request.getId() + " Request was identified as " + request.getType() + " from " + request.getOriginHostName());
			    }
				response = prepareResponse(request);
			} catch (Exception ex) {
			    // full trace to the client.
			    Writer result = new StringWriter();
			    PrintWriter printWriter = new PrintWriter(result);
			    ex.printStackTrace(printWriter);
				response = new ClientResponse(ClientResponse.Type.ErrorResponse, result.toString());
				logger.warn(ex);
				ex.printStackTrace();
			}

			try {
				replyDestination = message.getJMSReplyTo();
				sendClientResponse(replyDestination, gson.toJson(response));
			} catch (JMSException e) {
				// we can't do anything here
				e.printStackTrace();
				logger.error(e);
			} catch (Exception e) {
				// we try to send the cause to the client.
				if (replyDestination != null) {
					// TextMessage ret = session.createTextMessage();
					// ret.setText(gson.toJson(response));
					// producer.send()
				}
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		} else {
			logger.error("Received a Message which was not of type TextMessage :" + message);
		}
	}
}
