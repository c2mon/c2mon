/*
 * Copyright CERN 2011-2013, All Rights Reserved.
 */
package cern.c2mon.daq.jmx;

import static java.lang.String.format;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import cern.accsoft.commons.util.proc.ProcUtils;
import cern.accsoft.security.rba.login.LoginPolicy;
import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.common.conf.equipment.ICommandTagChanger;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.patterncache.PatternCache;
import cern.c2mon.patterncache.PatternCacheFileWatchdog;
import cern.c2mon.shared.common.datatag.address.JMXHardwareAddress;
import cern.c2mon.shared.common.datatag.address.JMXHardwareAddress.ReceiveMethod;
import cern.c2mon.shared.daq.command.ISourceCommandTag;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.rba.util.lookup.RbaTokenLookup;
import cern.rba.util.relogin.RbaLoginService;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The class implements an
 * EquipmentMessageHandler for JMX.
 */
public class JMXMessageHandler extends EquipmentMessageHandler implements ICommandRunner, IDataTagChanger,
        ICommandTagChanger, IEquipmentConfigurationChanger {

    static long MBEAN_CONNECTION_RETRY_TIMOUT = 30000; // ms

    static long CONNECTION_TEST_INTERVAL = 30000; // ms

    static final int EQ_ADDRESS_MIN_NUMBER_OF_EXPECTED_PARAMETERS = 2;

    static final int SERVICE_URL_INDEX = 0;
    static final int USER_INDEX = 1;
    static final int PASSWD_INDEX = 2;
    static final int POLLING_TIME_INDEX = 3;

    static final int POLLING_THREADS = 20;

    static final String JVM_UPTIME_OBJECT_NAME = "java.lang:type=Runtime";
    static String JVM_UPTIME_OBJECT_ATTRIBUTE = "Uptime";

    volatile static RbaLoginService service = null;

    private String jmxServiceUrl;
    private int jmxPollingTime;

    // the MBean service connection object
    MBeanServerConnection srvcon;

    // executor service for periodic threads (pollers and connection-testing thread)
    ScheduledExecutorService executor;

    // logger is initialized in the connectToDataSource() method
    private EquipmentLogger logger;

    /**
     * handles for pollers
     */
    private ConcurrentMap<Long, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<Long, ScheduledFuture<?>>();

    /**
     * map of notified tags, with references to their update listener instances
     */
    private ConcurrentMap<Long, DataTagUpdateListener> jmxUpdateListeners = new ConcurrentHashMap<Long, DataTagUpdateListener>();

    private ScheduledFuture<?> connectionTestFuture;

    private volatile boolean mbeanServiceConnected = false;

    private Thread connectionThread;

    private static final String PCACHE_FILE = System.getProperty("jmx.daq.passwd.file", "conf/jmx-passwords.txt");

    /**
     * The pattern cache + the watcher are static, since they should be shared across all instances of JMX handler
     */
    private static PatternCache<UserCredentials> pcache;
    private static PatternCacheFileWatchdog<UserCredentials> pcacheWatchdog;

    /**
     * This class implements a Thread responsibile for opening RMI connection to MBean server and initializing DataTags
     * 
     * @author wbuczak
     */
    protected class OpenMBeanConnectionTask implements Runnable {

        JMXMessageHandler handler;

        private Collection<ISourceDataTag> tags;

        private boolean comfaultSent = false;

        public OpenMBeanConnectionTask(final JMXMessageHandler handler) {
            this.handler = handler;
            this.tags = handler.getEquipmentConfiguration().getSourceDataTags().values();
        }

        /**
         * requests values of all DataTags This method is called right after the connection is established
         */
        private void initDataTags() {
            logger.debug("entering initDataTags()..");

            for (ISourceDataTag tag : tags) {
                handler.updateValue(tag, "initial value received");
            }

            logger.debug("leaving initDataTags()..");
        }

        @Override
        public void run() {

            boolean interrupted = false;

            while (!mbeanServiceConnected && !Thread.interrupted()) {
                try {

                    logger.debug(format("trying to connect to JMX service: %s", handler.getJmxServiceUrl()));

                    JMXServiceURL url = new JMXServiceURL(jmxServiceUrl);

                    JMXConnector jmxc = null;

                    UserCredentials ucredentials = pcache.findMatch(handler.getDeviceName());

                    if (null != ucredentials) {
                        String[] credentials = new String[] { ucredentials.getUserName(), ucredentials.getUserPasswd() };
                        Map<String, Object> env = new HashMap<String, Object>();
                        env.put("jmx.remote.credentials", credentials);
                        jmxc = JMXConnectorFactory.connect(url, env);
                    } else {
                        // try rbac credentials
                        try {
                            Map<String, Object> credEnv = new HashMap<String, Object>();
                            if (service == null) {
                                service = new RbaLoginService();
                                service.setLoginPolicy(LoginPolicy.LOCATION);
                                service.setAutoRefresh(true);
                                service.setApplicationName(ProcUtils.getApplicationName());
                                service.startAndLogin();
                            }

                            credEnv.put(JMXConnector.CREDENTIALS, RbaTokenLookup.findRbaToken());
                            jmxc = JMXConnectorFactory.connect(url, credEnv);
                        } catch (final Exception ex) {
                            logger.warn("RBAC authentication by location failed", ex);
                        }

                        // try no password
                        try {
                            jmxc = JMXConnectorFactory.connect(url, null);
                        } catch (final Exception error) {
                            throw new Exception("RBAC token & plain text authorization failed.");
                        }
                    }

                    srvcon = jmxc.getMBeanServerConnection();

                    // set the connection status to 'connected'
                    mbeanServiceConnected = true;

                    // report the equipment is correctly connected
                    getEquipmentMessageSender().confirmEquipmentStateOK();

                    // schedule connection testing task
                    connectionTestFuture = executor.scheduleAtFixedRate(new ConnectionTestingTask(handler), 0,
                            CONNECTION_TEST_INTERVAL, TimeUnit.MILLISECONDS);

                    logger.info(format("successfully connected to JMX service: %s", jmxServiceUrl));
                } catch (Exception ex) {
                    StringBuilder bld = new StringBuilder("failed to connect to JMX service: ").append(jmxServiceUrl)
                            .append(" ").append(ex.getMessage());
                    logger.error(bld);

                    if (!comfaultSent) {
                        // report the problem to the server (if not yet done)
                        // NOTE: no need to invalidate tags - sending comfault tag does the job
                        getEquipmentMessageSender().confirmEquipmentStateIncorrect(bld.toString());
                        comfaultSent = true;
                    }

                    try {
                        Thread.sleep(MBEAN_CONNECTION_RETRY_TIMOUT);
                    } catch (InterruptedException e) {
                        logger.warn("Interrupted while waiting for connection timeout " + e);
                        interrupted = true;
                    }
                }// catch
            }// while

            // do the initialization and registration ONLY is not previously interrupted
            // NOTE: this thread may be interrupted from method disconnectFromDataSource()
            if (!interrupted) {

                logger.info(format("requesting update for %d tags belonging to service: %s",
                        Integer.valueOf(this.tags.size()), handler.getJmxServiceUrl()));
                initDataTags();

                // iterate throughout the tag list and register tags
                for (ISourceDataTag tag : getEquipmentConfiguration().getSourceDataTags().values()) {
                    registerTag(tag);
                }
            }

        }// run
    }// OpenMBeanConnectionTask

    /**
     * This class implements a polling task. Polling tasks are executed periodically in order to get most recent values
     * of tags, that support polling as reception method
     * 
     * @author wbuczak
     */
    protected class PollerTask implements Runnable {

        private JMXMessageHandler handler;
        private ISourceDataTag tag;

        public PollerTask(final JMXMessageHandler handler, final ISourceDataTag tag) {
            this.handler = handler;
            this.tag = tag;
        }

        @Override
        public void run() {
            handler.updateValue(tag, null);
        }

    }// PollerTask

    /**
     * This class implements a task used for periodic connection checking.
     */
    protected class ConnectionTestingTask implements Runnable {

        private JMXMessageHandler handler;

        public ConnectionTestingTask(final JMXMessageHandler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                handler.getJVMUptime();
            } catch (Exception ex) {
                logger.error("Exception caught while trying to get JVM uptime", ex);

                if (mbeanServiceConnected) {

                    try {
                        mbeanServiceConnected = false;

                        logger.info("assuming connection lost - sending CommfaultTag");
                        handler.getEquipmentMessageSender().confirmEquipmentStateIncorrect(
                                "Looks like connection with the JMX server has been dropped");

                        // reconnect
                        handler.disconnectFromDataSource();
                        handler.connectToDataSource();
                    } catch (Exception e) {
                        logger.error("Exception caught:", e);
                    }
                }// if
            }
        }
    }// PollerTask

    /**
     * This class implements a listener for MBean server notifications
     * 
     * @author wbuczak
     */
    protected class DataTagUpdateListener implements NotificationListener {
        ISourceDataTag tag;

        public DataTagUpdateListener(final ISourceDataTag tag) {
            this.tag = tag;
        }

        public ISourceDataTag getTag() {
            return this.tag;
        }

        @Override
        public void handleNotification(Notification notification, Object handback) {
            if (logger.isTraceEnabled())
                logger.trace(format("handleNotification: received notification [%s]", notification.getMessage()));

            // only AttributeChangeNotification are supported
            if (!(notification instanceof AttributeChangeNotification)) {
                logger.info(format(
                        "Notifications of type %s are not supported! Invalidating tag[%d] with quality UNSUPPORTED_TYPE",
                        notification.getClass(), tag.getId()));
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.UNSUPPORTED_TYPE, null);
            }

            AttributeChangeNotification not = (AttributeChangeNotification) notification;

            Object[] valueWithDescription = null;
            Object value = null;
            String descr = null;
            Object attrVal = not.getNewValue();

            try {
                valueWithDescription = extractValue(attrVal, tag);
                value = valueWithDescription[0];
                descr = (String) valueWithDescription[1];
            } catch (Exception ex) {
                logger.info(format("Invalidating tag[%d] with quality INCORRECT_NATIVE_ADDRESS", tag.getId()));
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                        ex.getMessage());
                return;
            }

            if (null != value) {

                if (logger.isDebugEnabled())
                    logger.debug("\treceived value: " + value);

                // send the value up to the server
                getEquipmentMessageSender().sendTagFiltered(tag, value, System.currentTimeMillis(), descr);
            } else {
                // invalidate tag
                logger.info(format("Invalidating tag[%d] with quality CONVERSION_ERROR", tag.getId()));
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.CONVERSION_ERROR, null);
            }

            logger.trace("leaving handleNotification()");
        }
    }

    /**
     * default constructor
     */
    public JMXMessageHandler() {
    }

    /**
     * @param pollingTime
     */
    void jmxStartPoller(final ISourceDataTag tag, final int pollingTime) {
        if (logger.isTraceEnabled())
            logger.trace(format("entering startPoller(%d,%d)..", tag.getId(), Integer.valueOf(pollingTime)));

        scheduledFutures.put(tag.getId(), executor.scheduleAtFixedRate(new PollerTask(this, tag), pollingTime,
                pollingTime, TimeUnit.MILLISECONDS));

        logger.trace("leaving startPoller()");
    }

    /**
     * stops poller for given tag
     * 
     * @param tagId
     */
    void jmxStopPoller(final Long tagId) {
        if (logger.isTraceEnabled())
            logger.trace(format("entering stopPoller(%d)..", tagId));

        ScheduledFuture<?> sf = scheduledFutures.get(tagId);
        if (null != sf) {
            sf.cancel(false);
            scheduledFutures.remove(tagId);
        }

        logger.trace("leaving stopPoller()");
    }

    void jmxStopPollers() {
        logger.trace("entering stopPollers()..");

        for (Long tagId : this.scheduledFutures.keySet()) {
            this.jmxStopPoller(tagId);
        }

        logger.trace("leaving stopPollers()");
    }

    /**
     * opens subscription for a given tag from the 'notifiedTags' list
     */
    void jmxSubscribe(final ISourceDataTag tag) {
        if (logger.isTraceEnabled())
            logger.trace(format("entering openSubscription(%d)..", tag.getId()));

        JMXHardwareAddress addr = (JMXHardwareAddress) tag.getHardwareAddress();

        String errorMsg = null;
        boolean invalidateTag = false;

        if (addr.getReceiveMethod() == ReceiveMethod.notification) {
            try {
                ObjectName mbeanName = new ObjectName(addr.getObjectName());
                DataTagUpdateListener updateListener = new DataTagUpdateListener(tag);
                srvcon.addNotificationListener(mbeanName, updateListener, null, null);
                this.jmxUpdateListeners.put(tag.getId(), updateListener);
            } catch (InstanceNotFoundException ex) {
                errorMsg = "The specified MBean does not exist in the repository: " + ex.getMessage();
                invalidateTag = true;
            } catch (Exception ex) {
                errorMsg = "Could not register notification listener for mbean :" + ex.getMessage();
                invalidateTag = true;
            }

            if (invalidateTag) {
                logger.error(format("Exception caught while trying to open subscription for tag[%d]. error: %s",
                        tag.getId(), errorMsg));
                logger.error(format("Invalidating tag[%d] with quality: INCORRECT_NATIVE_ADDRESS", tag.getId()));
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, errorMsg);
            }

        } else {
            logger.warn(format("tag: #%d is not configured to work i notification mode, but in polling"));
        }

        logger.trace("leaving openSubscription()");
    }

    /**
     * closes JMX subscription for a given tag
     */
    void jmxUnsubscribe(final ISourceDataTag tag) {
        if (logger.isTraceEnabled())
            logger.trace(format("entering jmxUnsubscribe(%d)..", tag.getId()));

        JMXHardwareAddress addr = (JMXHardwareAddress) tag.getHardwareAddress();

        if (this.jmxUpdateListeners.get(tag.getId()) != null) {

            try {
                ObjectName mbeanName = new ObjectName(addr.getObjectName());
                srvcon.removeNotificationListener(mbeanName, this.jmxUpdateListeners.get(tag.getId()));
                this.jmxUpdateListeners.remove(tag.getId());
            } catch (Exception ex) {
                logger.error(format("Exception caught while trying to close subscription for tag[%d]. error: %s",
                        tag.getId(), ex.getMessage()));
                // we do not invalidate the tag here
            }
        } else {
            logger.warn(format("there is no JMX subscription opened for tag: #%d", tag.getId()));
        }

        if (logger.isTraceEnabled())
            logger.trace(format("leaving jmxUnsubscribe(%d)", tag.getId()));
    }

    /**
     * closes all active JMX subscriptions
     */
    void jmxUnsubscribe() {
        logger.trace("entering jmxUnsubscribe()..");
        for (DataTagUpdateListener updateListener : this.jmxUpdateListeners.values()) {
            jmxUnsubscribe(updateListener.getTag());
        }
        logger.trace("leaving jmxUnsubscribe()");
    }

    /**
     * 
     */
    void stopConnectionTestingTask() {
        logger.trace("entering stopConnectionTestingTask()..");
        if (null != connectionTestFuture)
            connectionTestFuture.cancel(false);

        logger.trace("leaving stopConnectionTestingTask()");
    }

    private Object[] extractValueFromSimpleType(Object attrVal, JMXHardwareAddress addr) {
        Object result = null;
        String valueDescription = null;

        // check if the returned attribute of a JMX bean is a number or a string
        if (attrVal instanceof Number || attrVal instanceof String || attrVal instanceof Boolean
                || attrVal instanceof Character) {
            result = attrVal;
            // check if the returned attribute of a JMX bean is an array
        } else if (attrVal.getClass().isArray()) {
            if (addr.hasIndex()) {
                result = java.lang.reflect.Array.get(attrVal, addr.getIndex());
            } else { // by default return the current length of the array
                result = new Integer(java.lang.reflect.Array.getLength(attrVal));
                valueDescription = arrayToString(attrVal);
            }
            // check if the returned attribute of a JMX bean is a list
        } else if (attrVal instanceof java.util.List<?>) {
            if (addr.hasIndex()) {
                result = ((java.util.List<?>) attrVal).get(addr.getIndex());
            } else { // if the index is not set, by default return the current size of collection
                result = ((java.util.List<?>) attrVal).size();
                valueDescription = ((java.util.List<?>) attrVal).toString();
            }
            // check if the returned attribute of a JMX bean is a map
        } else if (attrVal instanceof java.util.Map<?, ?>) {
            if (addr.hasMapField()) {
                Map<?, ?> map = (java.util.Map<?, ?>) attrVal;
                result = ((java.util.Map<?, ?>) attrVal).get(addr.getMapField());
            } else { // if the field is not set, by default return the current size of the map
                result = ((java.util.Map<?, ?>) attrVal).size();
                valueDescription = ((java.util.Map<?, ?>) attrVal).toString();
            }
            // check if the returned attribute of a JMX bean is a set
        } else if (attrVal instanceof java.util.Set<?>) {
            result = ((java.util.Set<?>) attrVal).size();
            valueDescription = ((java.util.Set<?>) attrVal).toString();
        }

        return new Object[] { result, valueDescription };
    }

    private static String arrayToString(Object array) {

        if (!array.getClass().isArray()) {
            return "";
        }

        int length = java.lang.reflect.Array.getLength(array);

        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < length; i++) {
            result.append(java.lang.reflect.Array.get(array, i));
            if (i < length - 1) {
                result.append(",");
            }
        }
        result.append("]");

        return result.toString();
    }

    /**
     * this private method extracts a value from a given map attribute of <code>CompositeData</code> bean implementing
     * <code>TabularData</code> interface
     * 
     * @param mapAttribute a <code>TabularData</code> attribute
     * @param key a map key to be used
     * @return a value extracted from the map or null + value description
     */
    private Object[] extractValueFromJmxTabularDataAttribute(final Object mapAttribute, final String key) {
        Object result = null;
        String valueDescription = null;

        if (mapAttribute instanceof TabularData) {
            TabularData td = (TabularData) mapAttribute;
            if (null == key) {
                result = td.size();
                valueDescription = td.toString();
            } else {
                CompositeData cp2 = td.get(new Object[] { key });
                result = cp2.get("value");
            }
        }

        return new Object[] { result, valueDescription };
    }

    /**
     * This method extracts the value of the JMX bean's attribute, based on the attribute type and information provided
     * inside the tag's hardware address
     * 
     * @param jmxAttribute JMX bean's attribute
     * @param tagAddress DataTag's hardware address
     * @return extracted value of null, if extraction was not possible
     */
    private Object[] extractValue(Object attrVal, ISourceDataTag tag) throws Exception {
        Object[] result = null;
        JMXHardwareAddress addr = (JMXHardwareAddress) tag.getHardwareAddress();

        if (attrVal instanceof TabularData) {
            result = extractValueFromJmxTabularDataAttribute(attrVal, addr.getMapField());
        } else
        // check if the returned attribute of a JMX bean is a number, string, list, map set or an array of primitives
        if (attrVal instanceof Number || attrVal instanceof String || attrVal instanceof Boolean
                || attrVal instanceof Character || attrVal instanceof java.util.List
                || attrVal instanceof java.util.Map || attrVal instanceof java.util.Set || attrVal.getClass().isArray()) {

            result = extractValueFromSimpleType(attrVal, addr);

            // check if the returned attribute of a JMX bean is a CompositeData
        } else if (attrVal instanceof CompositeData) {
            if (addr.hasCompositeField()) {
                Object field = ((CompositeData) attrVal).get(addr.getCompositeField());

                if (field instanceof TabularData) {
                    result = extractValueFromJmxTabularDataAttribute(field, addr.getMapField());
                } else {
                    result = extractValueFromSimpleType(field, addr); // extractValue(field, tag);
                }
            }
        } else { // assume this the returned type is a JavaBean

            if (addr.hasCompositeField()) {
                BeanWrapper bean = new BeanWrapperImpl(attrVal);
                result = extractValueFromSimpleType(bean.getPropertyValue(addr.getCompositeField()), addr);
            }
        }

        return result;
    }

    /**
     * @param tag
     * @param userValueDescription
     * @throws Exception
     */
    void updateValue(final ISourceDataTag tag, final String userValueDescription) {
        if (logger.isTraceEnabled())
            logger.trace(format("entering updateValue(#%d)..", tag.getId()));

        JMXHardwareAddress addr = (JMXHardwareAddress) tag.getHardwareAddress();

        boolean sendInvalid = false;
        String errorMsg = null;

        try {

            ObjectName mbeanName = new ObjectName(addr.getObjectName());

            Object attrVal = null;
            Object[] rawValueWithDescription = null;
            Object rawValue = null;
            String valueDesc = null;

            // if attribute is set
            if (addr.hasAttribute()) {
                // JMX query for attribute
                attrVal = srvcon.getAttribute(mbeanName, addr.getAttribute());

            } else if (addr.hasCallMethod()) {
                // JMX call to remote MBean method
                attrVal = srvcon.invoke(mbeanName, addr.getCallMethod(), null, null);
            }

            // extract the value from the bean
            rawValueWithDescription = extractValue(attrVal, tag);
            rawValue = rawValueWithDescription[0];
            valueDesc = (String) rawValueWithDescription[1];

            if (rawValue == null) {

                errorMsg = "Could not extract value from JMX attribute. Check your configuration";
                sendInvalid = true;
            }

            if (!sendInvalid) {

                if (rawValue != null) {
                    // send the value up to the server
                    getEquipmentMessageSender().sendTagFiltered(tag, rawValue, System.currentTimeMillis(), valueDesc);
                } else {
                    // there's some configuration problem
                    logger.info(format("Invalidating tag[%d] with quality INCORRECT_NATIVE_ADDRESS", tag.getId()));
                    getEquipmentMessageSender()
                            .sendInvalidTag(
                                    tag,
                                    SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                                    "could not extract value from JMX bean. Either the tag is wrongly configured or the JMX bean. Check your configuration!");
                }
            }// !sendInvalid

        } catch (MalformedObjectNameException ex) {
            errorMsg = "The format of the string does not correspond to a valid ObjectName: " + ex.getMessage();
            sendInvalid = true;
        } catch (AttributeNotFoundException ex) {
            errorMsg = "The specified attribute does not exist or cannot be retrieved: " + ex.getMessage();
            sendInvalid = true;
        } catch (InstanceNotFoundException ex) {
            errorMsg = "The specified MBean does not exist in the repository: " + ex.getMessage();
            sendInvalid = true;
        } catch (Exception ex) {
            sendInvalid = true;
            errorMsg = "Extracting data from JMX bean failed: " + ex.getMessage();
        }

        if (sendInvalid) {
            if (logger.isDebugEnabled())
                logger.debug(format("Invalidating tag[%d] with quality INCORRECT_NATIVE_ADDRESS and description: %s",
                        tag.getId(), errorMsg));
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, errorMsg);
        }

        if (logger.isTraceEnabled())
            logger.trace(format("leaving updateValue(#%d)", tag.getId()));
    }

    /**
     * @param tag
     * @param userValueDescription
     * @throws Exception
     */
    void getJVMUptime() throws Exception {
        logger.debug("entering getJVMUptime()..");

        ObjectName mbeanName = new ObjectName(JVM_UPTIME_OBJECT_NAME);

        Object val = srvcon.getAttribute(mbeanName, JVM_UPTIME_OBJECT_ATTRIBUTE);

        if (null != val)
            if (logger.isDebugEnabled())
                logger.debug("\treceived value: " + val);

        logger.debug("leaving getJVMUptime()");
    }

    @Override
    public void connectToDataSource() throws EqIOException {
        logger = getEquipmentLogger();

        logger.debug("entering connectToDataSource()..");

        this.parseEquipmentAddress();

        if (pcache == null) {
            pcache = new PatternCache<UserCredentials>(UserCredentials.class);
        }

        if (pcacheWatchdog == null) {
            pcacheWatchdog = new PatternCacheFileWatchdog<UserCredentials>(pcache, PCACHE_FILE, 30000);
            pcacheWatchdog.start();
        }

        // create executor
        this.executor = Executors.newScheduledThreadPool(POLLING_THREADS);

        // register handler as command runner
        getEquipmentCommandHandler().setCommandRunner(this);
        // register handler as data-tag changer
        getEquipmentConfigurationHandler().setDataTagChanger(this);
        // register handler as command-tag changer
        getEquipmentConfigurationHandler().setCommandTagChanger(this);

        // set equipment configuration changer
        getEquipmentConfigurationHandler().setEquipmentConfigurationChanger(this);

        // start the connection thread
        connectionThread = new Thread(new OpenMBeanConnectionTask(this));
        connectionThread.start();

        logger.debug("leaving connectToDataSource()");
    }

    /**
     * checks the equipment address
     * 
     * @throws EqIOException
     */
    private void parseEquipmentAddress() throws EqIOException {
        logger.debug("entering validateEquipmentAddress()..");

        String address = this.getEquipmentConfiguration().getAddress();
        if (null == address) {
            throw new EqIOException("equipment address must NOT be null. Check DAQ configuration!");
        }

        String[] tokens = address.trim().split(";");

        if (tokens.length < EQ_ADDRESS_MIN_NUMBER_OF_EXPECTED_PARAMETERS) {
            throw new EqIOException(format(
                    "equipment address is expected to have %d parameters. Check DAQ configuration!",
                    EQ_ADDRESS_MIN_NUMBER_OF_EXPECTED_PARAMETERS));
        }

        this.jmxServiceUrl = tokens[0];

        // if (tokens.length == EQ_ADDRESS_MAX_NUMBER_OF_EXPECTED_PARAMETER) {
        // this.jmxUserName = tokens[1];
        // this.jmxUserPassword = tokens[2];
        // }

        try {
            // polling time is supposed to be the last parameter
            this.jmxPollingTime = Integer.parseInt(tokens[tokens.length - 1]);
            if (this.jmxPollingTime <= 0)
                throw new EqIOException(
                        "polling time in the equmpent address must NOT be <= 0. Check DAQ configuration!");
        } catch (NumberFormatException ex) {
            throw new EqIOException(
                    "polling time in the equmpent address must be an integer >= 0. Check DAQ configuration!");
        }

        logger.debug("leaving validateEquipmentAddress()");
    }

    @Override
    public void disconnectFromDataSource() throws EqIOException {
        logger.debug("entering diconnectFromDataSource()..");

        // stop all pollers
        this.jmxStopPollers();

        // stop all active subscriptions
        this.jmxUnsubscribe();

        this.stopConnectionTestingTask();

        if (null != executor)
            executor.shutdown();

        this.mbeanServiceConnected = false;
        this.jmxUpdateListeners.clear();
        this.scheduledFutures.clear();

        if (this.connectionThread != null && this.connectionThread.isAlive()) {
            this.connectionThread.interrupt();
        }

        logger.debug("leaving diconnectFromDataSource()");
    }

    public String getJmxServiceUrl() {
        return this.jmxServiceUrl;
    }

    public String getDeviceName() {
        return this.getEquipmentConfiguration().getName();
    }

    // public String getJmxUser() {
    // return this.jmxUserName;
    // }
    //
    // public String getJmxPasswd() {
    // return this.jmxUserPassword;
    // }

    public int getJmxPollingTime() {
        return this.jmxPollingTime;
    }

    @Override
    public void refreshAllDataTags() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refreshDataTag(long dataTagId) {
        // TODO Auto-generated method stub

    }

    @Override
    public String runCommand(SourceCommandTagValue sctv) throws EqCommandTagException {
        if (logger.isDebugEnabled())
            logger.debug(format("entering runCommand(%d)..", sctv.getId()));

        String result = null;

        // check the command is registered
        if (!getEquipmentConfiguration().getSourceCommandTags().containsKey(sctv.getId())) {
            throw new EqCommandTagException(format("command tag id: %d is unknown", sctv.getId()));
        }

        // check the connection is fine
        if (!mbeanServiceConnected)
            throw new EqCommandTagException("equipment is currently disconnected");

        ISourceCommandTag command = getEquipmentConfiguration().getSourceCommandTags().get(sctv.getId());

        JMXHardwareAddress addr = (JMXHardwareAddress) command.getHardwareAddress();

        try {
            ObjectName mbeanName = new ObjectName(addr.getObjectName());

            if (null != addr.getAttribute()) {
                // JMX query for attribute

                // for attributes - command executes simple <<get>>
                result = srvcon.getAttribute(mbeanName, addr.getAttribute()).toString();

            } else if (null != addr.getCallMethod()) {
                // JMX call to remote MBean method

                // we support one-argument methods
                Object objres = null;
                if (null != sctv.getValue()) {
                    objres = srvcon.invoke(mbeanName, addr.getCallMethod(), new Object[] { sctv.getValue() },
                            new String[] { sctv.getValue().getClass().getCanonicalName() });
                } else {
                    objres = srvcon.invoke(mbeanName, addr.getCallMethod(), null, null);
                }

                if (null != objres)
                    result = objres.toString();
            }

        } catch (Exception ex) {
            logger.error(format("exception caught while trying to execute command id: %d", sctv.getId()));
            throw new EqCommandTagException(format("command id: %d execution failed. problem: %s", sctv.getId(),
                    ex.getMessage()));
        }

        if (logger.isDebugEnabled())
            logger.debug(format("leaving runCommand(%d)", sctv.getId()));
        return result;
    }

    @Override
    public void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        if (logger.isDebugEnabled())
            logger.debug(format("entering onAddDataTag(%d)..", sourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        // register tag
        registerTag(sourceDataTag);

        if (logger.isDebugEnabled())
            logger.debug(format("leaving onAddDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        if (logger.isDebugEnabled())
            logger.debug(format("entering onRemoveDataTag(%d)..", sourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        // unregister tag
        try {
            unregisterTag(sourceDataTag);
        } catch (TagOperationException ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }

        if (logger.isDebugEnabled())
            logger.debug(format("leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {
        if (logger.isDebugEnabled())
            logger.debug(format("entering onUpdateDataTag(%d,%d)..", sourceDataTag.getId(), oldSourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (!oldSourceDataTag.getHardwareAddress().equals(sourceDataTag.getHardwareAddress())) {
            try {
                logger.debug(format("calling  unregisterTag(%d)..", oldSourceDataTag.getId()));
                unregisterTag(oldSourceDataTag);
            } catch (TagOperationException ex) {
                changeReport.appendWarn(ex.getMessage());
            }

            logger.debug(format("calling  registerTag(%d)..", sourceDataTag.getId()));
            registerTag(sourceDataTag);

        }// if
        else {
            changeReport.appendInfo("No change detected in the tag hardware address. No action effected");
        }

        if (logger.isDebugEnabled())
            logger.debug(format("leaving onUpdateDataTag(%d,%d)", sourceDataTag.getId(), oldSourceDataTag.getId()));
    }

    boolean isTagAlreadyRegistered(final Long tagId) {
        boolean result = true;
        if (!this.jmxUpdateListeners.containsKey(tagId)) {
            if (!this.scheduledFutures.containsKey(tagId)) {
                result = false;
            }
        }

        return result;
    }

    boolean isTagPollerRegistered(final Long tagId) {
        boolean result = true;
        if (!this.scheduledFutures.containsKey(tagId)) {
            result = false;
        }

        return result;
    }

    boolean isTagUpdateListenerRegistered(final Long tagId) {
        boolean result = true;
        if (!this.jmxUpdateListeners.containsKey(tagId)) {
            result = false;
        }

        return result;
    }

    void registerTag(ISourceDataTag tag) {
        if (logger.isTraceEnabled())
            logger.trace(format("entering registerTag(%d)", tag.getId()));

        // check if this tag is not already registered

        // this tag should not be present neither in the notification-tags map nor a scheduler should be present
        if (isTagAlreadyRegistered(tag.getId())) {
            logger.warn(format("tag: %d is already registered. You must unregister it first!", tag.getId()));
            return;
        }

        try {

            JMXHardwareAddress addr = (JMXHardwareAddress) tag.getHardwareAddress();
            switch (addr.getReceiveMethod()) {

            case poll:
                this.jmxStartPoller(tag, getJmxPollingTime());
                break;

            case notification:
                this.jmxSubscribe(tag);
                break;

            }// switch

        } catch (Exception ex) {
            String err = format("Unable to create subscription for tag: %d. Problem description: %s", tag.getId(),
                    ex.getMessage());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, err);
        }

        finally {
            if (logger.isTraceEnabled())
                logger.trace(format("leaving registerTag(%d)", tag.getId()));
        }

    }

    private void unregisterTag(ISourceDataTag tag) throws TagOperationException {
        if (logger.isTraceEnabled())
            logger.trace(format("entering unregisterTag(%d)", tag.getId()));

        if (!isTagAlreadyRegistered(tag.getId())) {
            throw new TagOperationException(format("tag: %d is not registered. You must register it first!",
                    tag.getId()));
        }

        try {

            if (isTagPollerRegistered(tag.getId())) {
                // stop the poller for that tag (if exists)
                this.jmxStopPoller(tag.getId());
            } else if (isTagUpdateListenerRegistered(tag.getId())) {
                this.jmxUnsubscribe(tag);
            } else {
                throw new TagOperationException(
                        format("could not unregister tag: %d. Neither update listener nor poller was fould by the DAQ. You must restart the DAQ!",
                                tag.getId()));
            }

        } catch (Exception ex) {
            String err = format("Unable to stop monitoring for tag: %d. Problem description: %s", tag.getId(),
                    ex.getMessage());
            logger.error(err);
            throw new TagOperationException(err);
        }

        finally {
            if (logger.isTraceEnabled())
                logger.trace(format("leaving unregisterTag(%d)", tag.getId()));
        }

    }

    @Override
    public void onAddCommandTag(ISourceCommandTag sourceCommandTag, ChangeReport changeReport) {
        if (logger.isDebugEnabled())
            logger.debug(format("entering onAddCommandTag(%d)..", sourceCommandTag.getId()));

        // nothing more to be done here
        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (logger.isDebugEnabled())
            logger.debug(format("leaving onAddCommandTag(%d)", sourceCommandTag.getId()));
    }

    @Override
    public void onRemoveCommandTag(ISourceCommandTag sourceCommandTag, ChangeReport changeReport) {
        if (logger.isDebugEnabled())
            logger.debug(format("entering onRemoveCommandTag(%d)..", sourceCommandTag.getId()));

        // nothing more to be done here
        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (logger.isDebugEnabled())
            logger.debug(format("leaving onRemoveCommandTag(%d)", sourceCommandTag.getId()));
    }

    @Override
    public void onUpdateCommandTag(ISourceCommandTag sourceCommandTag, ISourceCommandTag oldSourceCommandTag,
            ChangeReport changeReport) {

        if (logger.isDebugEnabled())
            logger.debug(format("entering onUpdateCommandTag(%d,%d)..", sourceCommandTag.getId(),
                    oldSourceCommandTag.getId()));

        // nothing more to be done here
        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (logger.isDebugEnabled())
            logger.debug(format("leaving onUpdateCommandTag(%d,%d)", sourceCommandTag.getId(),
                    oldSourceCommandTag.getId()));

    }

    @Override
    public void onUpdateEquipmentConfiguration(
            @SuppressWarnings("unused") IEquipmentConfiguration equipmentConfiguration,
            @SuppressWarnings("unused") IEquipmentConfiguration oldEquipmentConfiguration, ChangeReport changeReport) {

        logger.debug("entering onUpdateEquipmentConfiguration()..");

        // without analyzing what has changed in the equipment's configuration
        // we simply call disconnectFromDataSource() and right after - connectToDataSource()
        try {
            this.disconnectFromDataSource();
            this.connectToDataSource();

            changeReport.setState(CHANGE_STATE.SUCCESS);
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.REBOOT);
            changeReport.appendWarn(ex.getMessage());
        } finally {
            logger.debug("leaving onUpdateEquipmentConfiguration()");
        }

    }

}
