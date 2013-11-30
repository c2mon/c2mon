/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.jms;

import static java.lang.String.format;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import org.springframework.jmx.export.annotation.ManagedOperation;

import cern.c2mon.daq.jms.BrokerConfig.BridgeConfig;
import cern.c2mon.daq.jms.BrokerConfig.ServiceTest;
import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

public class JMSMessageHandler extends EquipmentMessageHandler implements IDataTagChanger,
        IEquipmentConfigurationChanger {

    private enum BrokerTest {
        CONNECTION_TEST, TOPIC_PERF_TEST, QUEUE_PERF_TEST, BRIDGE_TOPIC_PERF_TEST, BRIDGE_QUEUE_PERF_TEST;
    }
    
    private BrokerConfig myConfig = null;

    
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    Future worker = null;

    /**
     * indicates if {@link #runCheck()} is currently executed.
     */
    private boolean runningCheck;
    
    /**
     * flag to indicate that this DAQ should start up in the {@link #connectToDataSource()}.
     * Used mainly for the unittest, which disables this for better testing.
     */
    boolean autoStart = true;

    /**
     * default check interval 
     * @see {@link #setTestInterval(long)} {@link #getInterval()}
     */
    private static long DEFAULT_CHECK_INTERVAL = 1000 * 60 * 5;
    
    /**
     * the check interval 
     */
    private long interval = DEFAULT_CHECK_INTERVAL;

    
    
    
    String [] parseEquipmentAdress(String equipAdresse) throws IllegalArgumentException {
        String [] result = null; 
        
        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("Parsing '" + equipAdresse + "' as EquipmentAddress...");
        }
        if (equipAdresse == null || equipAdresse.length() == 0) {
            throw new IllegalArgumentException("EQ Address has not been defined. Reconfiguration required.");
        }
        
        result = equipAdresse.split(",");
        
        if (getEquipmentLogger().isTraceEnabled()) {
            StringBuilder b = new StringBuilder("Got following brokers as equipment address : ");
            for (int i = 0; i < result.length; i++) {
                b.append(result[i]);
                if (i != result.length) {
                    b.append(",");
                }
            }
            getEquipmentLogger().trace(b.toString());
        }
        return result;
    }

    
    @Override
    public void connectToDataSource() throws EqIOException {

        getEquipmentLogger().trace("Entering connectToDataSource()");

        getEquipmentMessageSender().confirmEquipmentStateOK();

        myConfig = generateConfig(getEquipmentConfiguration());
        
        if (autoStart) {
            start();
        }
        
        getEquipmentLogger().info("Starting with Interval = " + getInterval()+ "msec and configuration " + myConfig);

    }

    /**
     * Generates a configuration to test one broker.
     * 
     * @param config the {@link EquipmentConfiguration} to read the info from
     * @return a valid BrokerConfig. If it detects missing data it will throw a {@link EqIOException}
     * @throws EqIOException in case the configuration is wrong.
     */
    public static BrokerConfig generateConfig(IEquipmentConfiguration config) throws EqIOException {
        
        if (config.getAddress() == null || !config.getAddress().startsWith("tcp://")) {
            throw new EqIOException("Invalid format for a connection address: '" + config.getAddress() + "'");
        }
        
        BrokerConfig bc = new BrokerConfig(config.getId(), config.getName());
        bc.setBrokerUrl(config.getAddress());
        
        for (ISourceDataTag tag : config.getSourceDataTags().values()) {
            SimpleHardwareAddress add = (SimpleHardwareAddress) tag.getHardwareAddress();
            String descr = add.getAddress();
            
            String [] tmp = descr.split(";");
            BrokerTest type = BrokerTest.valueOf(tmp[0]);
            if (tmp.length > 1 && bc.getBridgeConfigForBroker(tmp[1]) == null) {
                if (!tmp[1].startsWith("tcp://")) {
                    throw new EqIOException("Invalid format for a bridge connection address:" + tmp[1]);
                }
                bc.putBrokerConfig(tmp[1], new BridgeConfig(tmp[1]));
            }
            switch(type) {
            case TOPIC_PERF_TEST: bc.getDataTagIds().setTopicDataTag(tag.getId()); break;
            case QUEUE_PERF_TEST: bc.getDataTagIds().setQueueDataTag(tag.getId()); break;
            case CONNECTION_TEST: bc.getDataTagIds().setConnTestDataTag(tag.getId()); break;
            case BRIDGE_TOPIC_PERF_TEST: bc.getBridgeConfigForBroker(tmp[1]).getDataTagIds().setTopicDataTag(tag.getId());break;
            case BRIDGE_QUEUE_PERF_TEST: bc.getBridgeConfigForBroker(tmp[1]).getDataTagIds().setQueueDataTag(tag.getId());break;
            }
        }
        
        if (bc.getDataTagIds().getQueueDataTag() == null) {
            throw new EqIOException("Configuration Problem: No TagID found for queue perf test.");
        }
        if (bc.getDataTagIds().getTopicDataTag() == null) {
            throw new EqIOException("Configuration Problem: No TagID found for topic perf test.");
        }
        if (bc.getBrokerUrl() == null) {
            throw new EqIOException("Configuration Problem: No connection URL found.");
        }
        for (BridgeConfig bridge: bc.getBridges()) {
            if (bridge.getDataTagIds().getQueueDataTag() == null) {
                throw new EqIOException("Configuration Problem: No TagID found for BRIDGE queue perf test.");
            }
            if (bridge.getDataTagIds().getTopicDataTag() == null) {
                throw new EqIOException("Configuration Problem: No TagID found for BRIDGE topic perf test.");
            }
        }
        
        return bc;
    }
    
    /**
     * Stops the worker.
     */
    @ManagedOperation(description="Stops checking the broker.")
    public void stop() {
        if (this.worker != null) { 
            getEquipmentLogger().info("Stopping broker checker thread for " + myConfig.getEquipmentName());
            this.worker.cancel(false);
        }
    }
    
    @ManagedOperation(description="Starts checking the broker (if not already running).")
    public void start() {
        getEquipmentLogger().info("Starting broker checker thread for " + myConfig.getEquipmentName());
        this.worker = service.scheduleAtFixedRate(getWorker(), 0, getInterval(), TimeUnit.MILLISECONDS);
    }
    
    @ManagedOperation(description="Returns the time interval between two equipment tests.")
    public long getInterval() {
        return this.interval;
    }
    /**
     * 
     * @param milliseconds
     */
    @ManagedOperation(description="Sets the time interval between two equipment tests.")
    public void setTestInterval(long milliseconds) {
        this.interval = milliseconds;
    }
    
    /**
     * 
     * @return a Runnable which executes {@link #runCheck()}  
     */
    Runnable getWorker() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    runCheck();
                }catch(Exception ex) {
                    getEquipmentLogger().warn(ex.getMessage(), ex);
                }
            }
        };
    }
    
    
    @Override
    public void disconnectFromDataSource() throws EqIOException {
        getEquipmentLogger().trace("Entering disconnectFromDataSource()");
        stop();
        getEquipmentLogger().trace("Leaving disconnectFromDataSource()");
    }

    @Override
    public void refreshAllDataTags() {
        getEquipmentLogger().trace("Entering refreshAllDataTags()");
        
        //TODO : only when initialized.
        
//        synchronized (this) {
//            if (!runningCheck) {
//                runCheck();
//            } else {
//                getEquipmentLogger().warn("Another check is already running. Refusing triggered refreshAllDataTags()");
//            }
//        }
        getEquipmentLogger().trace("Leaving refreshAllDataTags()");
    }

    @Override
    public void refreshDataTag(long arg0) {
        getEquipmentLogger().trace("Entering refreshDataTag()");

        // NOT IMPLEMENTED
        
        getEquipmentLogger().trace("Leaving refreshDataTag()");
    }

    @Override
    public void onAddDataTag(ISourceDataTag arg0, ChangeReport arg1) {
        getEquipmentLogger().trace("Entering onAddDataTag()");
//        try {
//            
//            // TODO
//            throw new EqIOException("Not implemented.");
//            
//        } catch (EqIOException e) {
//            getEquipmentLogger().warn(e.getMessage(),e);
//            arg1.setState(CHANGE_STATE.FAIL);
//            arg1.appendError(e.getMessage());
//        }
        arg1.setState(CHANGE_STATE.REBOOT);
        getEquipmentLogger().trace("Leaving onAddDataTag()");
    }

    @Override
    public void onRemoveDataTag(ISourceDataTag arg0, ChangeReport arg1) {
        getEquipmentLogger().trace("Entering onRemoveDataTag()");
        
        arg1.setState(CHANGE_STATE.REBOOT);
        
        getEquipmentLogger().trace("Leaving onRemoveDataTag()");
    }

    @Override
    public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {
        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(
                    format("entering onUpdateDataTag(%d,%d)..", sourceDataTag.getId(), oldSourceDataTag.getId()));

        if (!oldSourceDataTag.getHardwareAddress().equals(sourceDataTag.getHardwareAddress())) {
            changeReport.setState(CHANGE_STATE.REBOOT);
        }// if
        else {
            changeReport.appendInfo("No change detected in the tag hardware address. No action effected");
        }

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(
                    format("leaving onUpdateDataTag(%d,%d)", sourceDataTag.getId(), oldSourceDataTag.getId()));
    }

    @Override
    public void onUpdateEquipmentConfiguration(IEquipmentConfiguration old, IEquipmentConfiguration newConfig,
            ChangeReport resultReport) {
        getEquipmentLogger().trace("Entering onUpdateEquipmentConfiguration()");
        
        if (!old.getAddress().equals(newConfig.getAddress())) {
            try {
                BrokerConfig toAssign = generateConfig(newConfig);
                
                synchronized (this)  {
                    myConfig = toAssign;
                }
                resultReport.setState(CHANGE_STATE.SUCCESS);
            }catch(Exception ex) {
                resultReport.setState(CHANGE_STATE.FAIL);
                resultReport.appendError(ex.getMessage());
            }
        }
        getEquipmentLogger().trace("Leaving onUpdateEquipmentConfiguration()");
    }

    
    public synchronized void runCheck() {
        getEquipmentLogger().trace("Entering runCheck()");
        
        this.runningCheck = true;
        
        runBrokerCheck(myConfig);        
        /*
         * check if we have more than one broker? -> we need to check the bridge to the other ones as well.
         */

        if (myConfig.hasBridgeConfigured()) {
            for (BridgeConfig bridge : myConfig.getBridges()) {
                String remote = bridge.getRemoteUrl();
                getEquipmentLogger().debug("Running Bridge Test to '" + remote + "'");
                if (remote != null) {
                    /* 
                     * Bridge test 
                     */
                    runBridgeTest(bridge);
                } else {
                    String msg = "Cannot find remote bridge config for " + bridge.getRemoteUrl();
                    getEquipmentLogger().warn(msg);
                    /*
                     Cannot find the configured remote endpoint in the global config - : invalidate the tags.
                     */
                    getEquipmentMessageSender().sendInvalidTag(getEquipmentConfiguration().getSourceDataTag(bridge.getDataTagIds().getQueueDataTag()), SourceDataQuality.INCORRECT_NATIVE_ADDRESS , msg);
                    getEquipmentMessageSender().sendInvalidTag(getEquipmentConfiguration().getSourceDataTag(bridge.getDataTagIds().getTopicDataTag()), SourceDataQuality.INCORRECT_NATIVE_ADDRESS , msg);
                }
            }
        }
        
        this.runningCheck = false;
        getEquipmentLogger().trace("Leaving runCheck()");
    }

    /**
     * Runs the checks (connection, 2 x message perf) againts one broker.
     * 
     * @param url the broker url
     * @param serviceTest the {@link ServiceTest} containing the data tag ids.
     */
    private void runBrokerCheck(BrokerConfig config) {

        if (getEquipmentLogger().isTraceEnabled()) {
            getEquipmentLogger().trace("Entering runBrokerCheck() with config =" + config);
        }

        if (config.getBrokerUrl() == null || config.getDataTagIds() == null) {
            return;
        }
        ServiceTest serviceTest = config.getDataTagIds();        
        
        PerfTester p = new PerfTester(config.getBrokerUrl());

        ISourceDataTag queuePerfTest = getEquipmentConfiguration().getSourceDataTag(serviceTest.getQueueDataTag());
        ISourceDataTag topicPerfTest = getEquipmentConfiguration().getSourceDataTag(serviceTest.getTopicDataTag());
        ISourceDataTag connTest      = getEquipmentConfiguration().getSourceDataTag(serviceTest.getConnTestDataTag());

        getEquipmentLogger().info("Running Broker test on URL='" + config.getBrokerUrl() + "'");

        /*
         * connection test
         */
        if (connTest != null) {
            try {
                if (!p.canConnect()) {
                    throw new JMSException("Cannot connect to Broker '" + config.getBrokerUrl() + "'");
                } else {
                    getEquipmentMessageSender().sendTagFiltered(connTest, new Boolean(true), System.currentTimeMillis() );
                }
            } catch (JMSException e1) {
                getEquipmentMessageSender().sendTagFiltered(connTest, new Boolean(false), System.currentTimeMillis(), e1.getMessage());
                getEquipmentMessageSender().sendTagFiltered(topicPerfTest, new Float(0.0),System.currentTimeMillis());
                getEquipmentMessageSender().sendTagFiltered(queuePerfTest, new Float(0.0),System.currentTimeMillis());
                return;
            }
        }
        
        /*
         * Topic
         */
        if (topicPerfTest != null) {
            try {
                float speedTopic = p.measureTopicMessagePerf("MON2");
                //getEquipmentMessageSender().sendTagFiltered(topicPerfTest, new Float(speedTopic), System.currentTimeMillis());
                
                if (speedTopic == 0.0f) {
                    throw new Exception("Timeout!");
                } 
                getEquipmentMessageSender().sendTagFiltered(topicPerfTest, new Float(speedTopic), System.currentTimeMillis());
                
            } catch (Exception e) {
                String msg = "Cannot aquire topic message perf for '" + config.getBrokerUrl() + " ': " + e.getMessage();
                getEquipmentMessageSender().sendInvalidTag(topicPerfTest, SourceDataQuality.DATA_UNAVAILABLE, msg);
                getEquipmentLogger().info(msg, e);
            }
        }

        /*
         * Queue
         */
        if (queuePerfTest != null) {
            try {
                float speedQueue = p.measureQueueMessagePerf("MON2");
                //getEquipmentMessageSender().sendTagFiltered(queuePerfTest, new Float(speedQueue), System.currentTimeMillis());
                
                if (speedQueue == 0.0f) {
                    throw new Exception("Timeout!");
                } 
                getEquipmentMessageSender().sendTagFiltered(queuePerfTest, new Float(speedQueue), System.currentTimeMillis());
            } catch (Exception e) {
                String msg = "Cannot aquire queue message perf for '" + config.getBrokerUrl() + " ': " + e.getMessage();
                getEquipmentMessageSender().sendInvalidTag(queuePerfTest, SourceDataQuality.DATA_UNAVAILABLE, msg);
                getEquipmentLogger().info(msg, e);
            }
        }
        
        getEquipmentLogger().info("Broker test on URL='" + config.getBrokerUrl() + "' finished.");
    }

    
    /** Measures the topic and queue performance for bridge between two brokers. 
     * For this it uses the {@link #myConfig} and the passed {@link BridgeConfig}. 
     * 
     * @param config the BridgeConfig with the remote url.
     */
    private void runBridgeTest(BridgeConfig config) {
        if (getEquipmentLogger().isTraceEnabled()) {
            getEquipmentLogger().trace("Entering runBridgeTest() for " + myConfig.getBrokerUrl() + " -> " + config.getRemoteUrl());
        }

        ServiceTest serviceTest = config.getDataTagIds();
        
        ISourceDataTag queuePerfTest = getEquipmentConfiguration().getSourceDataTag(serviceTest.getQueueDataTag());
        ISourceDataTag topicPerfTest = getEquipmentConfiguration().getSourceDataTag(serviceTest.getTopicDataTag());

        PerfTester p = new PerfTester(config.getRemoteUrl(), myConfig.getBrokerUrl());

        /* Bridge Queue Perf */
        try {
            float speedQueue = p.measureQueueMessagePerf("MON2-BRIDGE");
            //getEquipmentMessageSender().sendTagFiltered(queuePerfTest, new Float(speedQueue), System.currentTimeMillis());
            if (speedQueue == 0.0f) {
                throw new Exception("Timeout!");
            } 
            getEquipmentMessageSender().sendTagFiltered(queuePerfTest, new Float(speedQueue), System.currentTimeMillis());
        } catch (Exception e) {
            String msg = "Cannot aquire queue message perf for bridge: " + e.getMessage();
            getEquipmentMessageSender().sendInvalidTag(queuePerfTest, SourceDataQuality.DATA_UNAVAILABLE, msg);
            getEquipmentLogger().info(msg, e);
        }

        /* Bridge Topic Perf */
        try {
            float speedTopic = p.measureTopicMessagePerf("MON2-BRIDGE");
            // getEquipmentMessageSender().sendTagFiltered(topicPerfTest, new Float(speedTopic), System.currentTimeMillis());
            if (speedTopic == 0.0f) {
                throw new Exception("Timeout!");
            } 
            getEquipmentMessageSender().sendTagFiltered(topicPerfTest, new Float(speedTopic), System.currentTimeMillis());
            
        } catch (Exception e) {
            String msg = "Cannot aquire topic message perf for bridge: " + e.getMessage();
            getEquipmentMessageSender().sendInvalidTag(queuePerfTest, SourceDataQuality.DATA_UNAVAILABLE, msg);
            getEquipmentLogger().info(msg, e);
        }
    }

}
