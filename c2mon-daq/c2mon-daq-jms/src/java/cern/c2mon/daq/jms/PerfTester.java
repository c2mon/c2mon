/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.jms;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.log4j.Logger;

/**
 * @author ${user}
 * @version $Revision$, $Date$, $Author$
 */
public class PerfTester {

    private ActiveMQConnectionFactory producerfactory;
    private ActiveMQConnectionFactory consumerfactory;
    
    private static Logger log = Logger.getLogger(PerfTester.class);
    

    public PerfTester(String brokerUrl) {
        producerfactory = new ActiveMQConnectionFactory(brokerUrl);
        consumerfactory = producerfactory;
        producerfactory.setClientIDPrefix(JMSMessageHandler.class.getName());
        if (log.isDebugEnabled()) {
            log.debug("Creating Perftest for one broker endpoint: " + brokerUrl);
        }
    }

    public PerfTester(String broker1, String broker2) {
        producerfactory = new ActiveMQConnectionFactory(broker1);
        consumerfactory = new ActiveMQConnectionFactory(broker2);
        producerfactory.setClientIDPrefix(JMSMessageHandler.class.getName());
        consumerfactory.setClientIDPrefix(JMSMessageHandler.class.getName());
        
        if (log.isDebugEnabled()) {
            log.debug("Creating Perftest for two broker endpoints: " + broker1 + " & " + broker2);
        }
    }

    private Connection getProducerConnection() throws JMSException {
        ActiveMQConnection conn = (ActiveMQConnection) producerfactory.createConnection();
        conn.start();
        return conn;
    }

    private Connection getConsumerConnection() throws JMSException {
        ActiveMQConnection conn = (ActiveMQConnection) consumerfactory.createConnection();
        conn.start();
        return conn;
    }

    /**
     * @return
     * @throws JMSException in case the connection url is wrong (as reported by the JMS lib)<br>
     *             OR the connection cannot be established.
     */
    public boolean canConnect() throws JMSException {
        if (log.isTraceEnabled()) {
            log.trace("Entering canConnect() for " + producerfactory.getBrokerURL());
        }
        
        boolean result = false;
        ActiveMQConnection conn = null;
        try {
            conn = ((ActiveMQConnection) getProducerConnection());
            result = true;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignore) {
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("canConnect() for " + producerfactory.getBrokerURL() + " is " + result);
        }
        return result;
    }

    /**
     * 
     * @param topicName the topic name to use for the test.
     * @return the measured value <b>OR 0.0 </b> in case the message was not received within 5 sec. 
     * @throws Exception in case of a connection problem
     */
    public float measureTopicMessagePerf(String topicName) throws Exception {
        return measureMessagePerf(topicName, true);
    }
    
    /**
     * 
     * @param topicName the queue name to use for the test.
     * @return the measured value <b>OR 0.0 </b> in case the message was not received within 5 sec. 
     * @throws Exception in case of a connection problem
     */
    public float measureQueueMessagePerf(String queueName) throws Exception {
        return measureMessagePerf(queueName, false);
    }

    private float measureMessagePerf(String destName, boolean topic) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("Entering measureMessagePerf. Topic=" + topic + ", Destname=" + destName);
        }
        
        
        Connection producerConn = getProducerConnection();
        Connection consumerConn = getConsumerConnection();

        Session prodSession = producerConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Session consumerSession = consumerConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        
        ((ActiveMQSession)consumerSession).setAsyncDispatch(false);

        MyConsumer c = new MyConsumer();
        try {
            /* measuring ... */
            if (topic) {
                MessageConsumer con = consumerSession.createConsumer(consumerSession.createTopic(destName));
                con.setMessageListener(c);
                Thread.sleep(2000);
                sendTestMessage(prodSession, prodSession.createTopic(destName));
                synchronized (c) {
                    c.wait(5000);
                }
            } else {
                MessageConsumer con = consumerSession.createConsumer(consumerSession.createQueue(destName));
                con.setMessageListener(c);
                Thread.sleep(2000);
                sendTestMessage(prodSession, prodSession.createQueue(destName));
                synchronized (c) {
                    c.wait(5000);
                }
            }
        } finally {
            /* close the connections, but ignore the exceptions */
            try {
                producerConn.close();
            } catch (Exception ignore) {
            }
            try {
                consumerConn.close();
            } catch (Exception ignore) {
            }
        }

        /* return 0 if we encountered a timeout, else the time the message took */
        if (c.gotMessage()) {
            if (log.isDebugEnabled()) {
                log.debug("Got " + (topic?"TOPIC":"QUEUE") + " message speed " + c.getMessageSpeed());
            }
            return c.getMessageSpeed();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("TestMessage on " + (topic?"TOPIC":"QUEUE") + " could not be measured: timeout!");
            }
            return 0.0f;
        }
    }

    /**
     * Sends a JMS test message with empty body and the LongProperty "TS" which contains the timestamp when the message
     * had been sent.
     * 
     * @param session
     * @param dest
     * @return
     * @throws JMSException
     */
    private long sendTestMessage(final Session session, final Destination dest) throws JMSException {
        if (log.isTraceEnabled()) {
            log.trace("Entering sendTestMessage() for destination " + dest);
        }
        final long id = System.currentTimeMillis();
        MessageProducer p = null;
        p = session.createProducer(dest);
        TextMessage msg = session.createTextMessage();
        msg.setStringProperty("TS", Long.toString(id));
        msg.setText("Test message");
        if (log.isDebugEnabled()) {
            log.debug("Sending test message to " + dest + " with TS=" + id);
        }
        p.send(msg, DeliveryMode.NON_PERSISTENT, 0, 20000);
        return id;
    }

    /**
     * A small helper class which wraps a JMS consumer listening to a destination. This {@link MessageConsumer} must
     * have been created before by the caller. {@link MyConsumer#waitForMessage()} blocks (with a timeout of 5 seconds).
     * You can then ask using {@link #gotMessage()} if the message had been received.
     * 
     * @author ${user}
     * @version $Revision$, $Date$, $Author$
     */
    private class MyConsumer implements MessageListener {
        long receiveTs = 0L;
        long sendTs = 0;
        long messageTime = 0L;

        boolean gotMessage() {
            return receiveTs > 0;
        }

        long getMessageSpeed() {
            return messageTime;
        }

        @Override
        public void onMessage(Message arg0) {
            try {
                receiveTs = System.currentTimeMillis();
                if (log.isDebugEnabled()) {
                    log.debug("Got test message. TS=" + arg0.getLongProperty("TS") + ", my time is " + receiveTs);
                }
                sendTs = arg0.getLongProperty("TS");
                if (receiveTs > sendTs)
                    messageTime = receiveTs - sendTs;
                else if (receiveTs == sendTs) {
                    /* super fast message time : microseconds not enough to calc the diff. don't care, let put it to 1ms.*/
                    messageTime = 1l;
                }
                synchronized(this) {
                    notify();
                }
            } catch (JMSException ignore) {
            }
        }
    }

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    //
    // -- implements XXXX -----------------------------------------------
    //

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    // 
    // -- INNER CLASSES -----------------------------------------------
    //
}
