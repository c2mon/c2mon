package cern.c2mon.driver.common.conf.core;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

/**
 * Class storing the DAQ configuration details that are generic across all DAQs.
 * 
 * @author mbrightw
 * 
 */
@Service
public class CommonConfiguration {
    /**
     * The default connection ping interval
     */
    private static final int DEFAULT_CONNCECTION_PING_INTERVAL = 5;

    /**
     * The default connection retry period
     */
    private static final int DEFAULT_CONNECTION_RETRY_PERIOD = 4000;

    /**
     * Reference to the properties used at initialization.
     */
    @Resource
    private Properties daqProperties;

    /**
     * The Sonic JMS user for the statistics module.
     */
    private String jmsStatsUser;
    /**
     * The Sonic JMS password for the statistics module.
     */
    private String jmsStatsPassword;
    /**
     * The Sonic JMS queue name for the statistics module.
     */
    private String jmsStatsQueue;

    /**
     * JNDI lookup name for the filter path, used for sending data to the
     * statistics module
     */
    private String statsJmsJndi;

    /**
     * The capacity of the filter buffer, that is the maximum number of objects
     * it can hold (FIFO is applied thereafter).
     */
    private int filterBufferCapacity;

    /**
     * Name of the RMI context factory used from Sonic MQ
     */
    private String rmiInitialContextFactory;

    /**
     * Name of the security principle used from Sonic MQ
     */
    private String securityPrinciple;

    /**
     * Name of the security credentials used from Sonic MQ
     */
    private String securityCredentials;

    /**
     * Name of the provider USED used from Sonic MQ
     */
    private String providerURL;

    /**
     * JNDI lookup name of the TopicConnectionFactory to be used for creating
     * the JMS connection for receiving data (Sonic MQ)
     */
    private String jmsTcfJndiName;

    /**
     * The required username for JMS Sonic MQ connection.
     */
    private String jmsRequestUser;

    /**
     * The required password for JMS Sonic MQ connection.
     */
    private String jmsRequestPassword;

    /**
     * The configuration topic of the Sonic MQ connection.
     */
    private String confTopic;

    /**
     * Interval (in milliseconds!) between each 2 connection attempts
     */
    private long connectionRetryPeriod;

    /**
     * Interval (in seconds!) between each connections probing (pinging)
     */
    private long connectionPingInterval;

    /**
     * Request timeout for Sonic MQ JMS session.
     */
    private Long requestTimeout;

    /**
     * Init method called at bean initialization (must not be final!).
     */
    @PostConstruct
    public void init() {
        setFromProperties();
    }

    /**
     * At initialization, sets the fields that need setting from the Properties
     * injected bean.
     */
    private void setFromProperties() {
        String propValue = "";
        propValue = daqProperties.getProperty("RMIInitialContextFactory", "");
        this.setRMIInitialContextFactory(propValue);
        propValue = daqProperties.getProperty("SecurityPrinciple", "");
        this.setSecurityPrinciple(propValue);
        propValue = daqProperties.getProperty("SecurityCredentials", "");
        this.setSecurityCredentials(propValue);
        propValue = daqProperties.getProperty("ProviderURL", "");
        this.setProviderURL(propValue);
        propValue = daqProperties.getProperty("TopicConnectionFactory", "");
        this.setTopicConnectionFactory(propValue);
        propValue = daqProperties.getProperty("JMSRequestUser", "");
        this.setJMSRequestUser(propValue);
        propValue = daqProperties.getProperty("JMSRequestPassword", "");
        this.setJMSRequestPassword(propValue);
        propValue = daqProperties.getProperty("ConfTopic", "");
        this.setConfTopic(propValue);

        try {
            propValue = daqProperties.getProperty("ConnectionRetryPeriod", "");
            this.setConnectionRetryPeriod(new Long(propValue).longValue());
        } catch (NumberFormatException ex) {
            System.out.println("WARNING : Basic configruation file contains improper of ConnectionRetryPeriod atribute");
            System.out.println("\t the default value (4000) will be used");
            this.setConnectionRetryPeriod(DEFAULT_CONNECTION_RETRY_PERIOD);
        }

        try {
            propValue = daqProperties.getProperty("ConnectionPingInterval", "");
            this.setConnectionPingInterval(new Long(propValue).longValue());
        } catch (NumberFormatException ex) {
            System.out.println("WARNING : Basic configruation file contains improper of ConnectionPingInterval atribute");
            System.out.println("\t the default value (5) will be used");
            this.setConnectionPingInterval(DEFAULT_CONNCECTION_PING_INTERVAL);
        }

        // fetch the JNDI lookup for the filter QCF
        propValue = daqProperties.getProperty("JMSStatsQCF");
        this.setFilterQCF(propValue);

        // fetch and save the statistics client parameters
        propValue = daqProperties.getProperty("JMSStatsUser", "");
        this.setJMSStatsUser(propValue);
        propValue = daqProperties.getProperty("JMSStatsPassword", "");
        this.setJMSStatsPassword(propValue);
        propValue = daqProperties.getProperty("JMSStatsQueue", "");
        this.setJMSStatsQueue(propValue);

        // get the maximum capacity of the filter SynchroBuffer
        // (FIFO thereafter: values added after this point will prompt the
        // buffer to
        // remove the oldest values)
        propValue = daqProperties.getProperty("FilterBufferCapacity","1000");
        this.setFilterBufferCapacity(new Integer(propValue).intValue());

        propValue = daqProperties.getProperty("server.request.timeout","120000");
        this.setRequestTimeout(Long.valueOf(propValue));
    }

    /**
     * Setter method
     * 
     * @param daqProperties
     *            the daqProperties to set
     */
    public final void setDaqProperties(final Properties daqProperties) {
        this.daqProperties = daqProperties;
    }

    /**
     * returns the JMS user for the Statistics module connection
     * 
     * @return The JMS user stats for statistic module connection.
     */
    public final String getJMSStatsUser() {
        return jmsStatsUser;
    }

    /**
     * sets the JMS user for the Statistics module connection
     * 
     * @param pJmsStatsUser
     *            the JMS user
     */
    public final void setJMSStatsUser(final String pJmsStatsUser) {
        this.jmsStatsUser = pJmsStatsUser;
    }

    /**
     * returns the JMS password for the Statistics module connection
     * 
     * @return the JMS password
     */
    public final String getJMSStatsPassword() {
        return jmsStatsPassword;
    }

    /**
     * sets the JMS password for the Statistics module connection
     * 
     * @param pJmsStatsPassword
     *            the JMS password
     */
    public final void setJMSStatsPassword(final String pJmsStatsPassword) {
        this.jmsStatsPassword = pJmsStatsPassword;
    }

    /**
     * returns the JMS queue for the Statistics module connection
     * 
     * @return the queue name
     */
    public final String getJMSStatsQueue() {
        return jmsStatsQueue;
    }

    /**
     * sets the JMS queue for the Statistics module connection
     * 
     * @param pJmsStatsQueue
     *            the queue name
     */
    public final void setJMSStatsQueue(final String pJmsStatsQueue) {
        this.jmsStatsQueue = pJmsStatsQueue;
    }

    /**
     * Returns the maximum capacity of the filter buffer.
     * 
     * @return the filterBufferCapacity
     */
    public final int getFilterBufferCapacity() {
        return filterBufferCapacity;
    }

    /**
     * Sets the maximum capacity of the filter synchrobuffer (FIFO thereafter).
     * 
     * @param pFilterBufferCapacity
     *            the filterBufferCapacity to set
     */
    public final void setFilterBufferCapacity(final int pFilterBufferCapacity) {
        filterBufferCapacity = pFilterBufferCapacity;
    }

    /**
     * This method sets the rmi initial context factory for jms connections
     * 
     * @param rmiInitialContextFactory
     *            the rmi initial context factory
     */
    public void setRMIInitialContextFactory(final String rmiInitialContextFactory) {
        this.rmiInitialContextFactory = rmiInitialContextFactory;
    }

    /**
     * This method gets the rmi initial context factory for jms connections
     * 
     * @return String
     */
    public String getRMIInitialContextFactory() {
        return rmiInitialContextFactory;
    }

    /**
     * This method sets the jms connection security principle
     * 
     * @param secPrinciple
     *            the security principle
     */
    public void setSecurityPrinciple(final String secPrinciple) {
        securityPrinciple = secPrinciple;
    }

    /**
     * This method gets the JMS connection's security principle
     * 
     * @return The security principle of the JMS connection.
     */
    public String getSecurityPrinciple() {
        return securityPrinciple;
    }

    /**
     * This method sets the JMS connection security credentials
     * 
     * @param secCredentials The security credentials
     */
    public void setSecurityCredentials(final String secCredentials) {
        securityCredentials = secCredentials;
    }

    /**
     * This method gets the JMS connection security credentials
     * 
     * @return Returns the security credentials of the JMS connection.
     */
    public String getSecurityCredentials() {
        return securityCredentials;
    }

    /**
     * This method sets the JMS connection provider URL
     * 
     * @param providerURL The provider URL
     */
    public void setProviderURL(final String providerURL) {
        this.providerURL = providerURL;
    }

    /**
     * This method gets the JMS connection provider URL
     * 
     * @return Returns the provider URL of the JMS connection.
     */
    public String getProviderURL() {
        return providerURL;
    }

    /**
     * This method sets the JMS topic connection factory
     * 
     * @param topicConFact the JMS topic connection factory
     */
    public void setTopicConnectionFactory(final String topicConFact) {
        jmsTcfJndiName = topicConFact;
    }

    /**
     * This method gets the JMS topic connection factory
     * 
     * @return String
     */
    public final String getTopicConnectionFactory() {
        return jmsTcfJndiName;
    }

    /**
     * This method sets the JMS configuration request topic
     * 
     * @param topic
     *            the JMS topic
     */
    public final void setConfTopic(final String topic) {
        confTopic = topic;
    }

    /**
     * This method gets the JMS configuration request topic
     * 
     * @return String
     */
    public final String getConfTopic() {
        return confTopic;
    }

    /**
     * This method sets the jms requestor's user name
     * 
     * @param jmsReqUser
     *            the user name
     */
    public final void setJMSRequestUser(final String jmsReqUser) {
        this.jmsRequestUser = jmsReqUser;
    }

    /**
     * This method gets the jms requestor's user name
     * 
     * @return The JMS request user.
     */
    public final String getJMSRequestUser() {
        return jmsRequestUser;
    }

    /**
     * This method sets the jms requestor's password
     * 
     * @param jmsReqPasswd
     *            the password
     */
    public final void setJMSRequestPassword(final String jmsReqPasswd) {
        this.jmsRequestPassword = jmsReqPasswd;
    }

    /**
     * This method gets the jms requestor's password
     * 
     * @param jms_req_passwd
     * @return String
     */
    public final String getJMSRequestPassword() {
        return jmsRequestPassword;
    }

    /**
     * This method sets connection retry period
     * 
     * @param period
     *            the time period
     */
    public final void setConnectionRetryPeriod(final long period) {
        connectionRetryPeriod = period;
    }

    /**
     * This method gets connection retry period
     * 
     * @return long retry period
     */
    public final long getConnectionRetryPeriod() {
        return connectionRetryPeriod;
    }

    /**
     * This method sets the connection ping interval
     * 
     * @param interval
     *            the ping interval (in miliseconds)
     */
    public final void setConnectionPingInterval(final long interval) {
        connectionPingInterval = interval;
    }

    /**
     * This method gets the connection ping interval
     * 
     * @return long
     */
    public final long getConnectionPingInterval() {
        return connectionPingInterval;
    }

    /**
     * Returns the QCF for the statistics/filter path
     * 
     * @return the jms_filter_qcf_jndi_name
     */
    public final String getFilterQCF() {
        return statsJmsJndi;
    }

    /**
     * Sets the QCF for the statistics/filter path
     * 
     * @param pStatsJmsJndi
     *            the jms_filter_qcf_jndi_name to set
     */
    public final void setFilterQCF(final String pStatsJmsJndi) {
        this.statsJmsJndi = pStatsJmsJndi;
    }

    /**
     * Setter method.
     * 
     * @param requestTimeout
     *            millisecond timeout
     */
    private void setRequestTimeout(final Long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    /**
     * Getter method.
     * 
     * @return the timeout in milliseconds
     */
    public Long getRequestTimeout() {
        return requestTimeout;
    }
}
