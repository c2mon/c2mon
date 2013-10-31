/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.jms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrokerConfig {

        public static final long defaultWarnLevel = 100;
        public static final long defaultErrorLevel = 1000;
    
        private final long equipmentId;
        private String equipmentName;
        private String brokerUrl;
        private ServiceTest dataTagIds;
        
        private long warnLevel = defaultWarnLevel;
        private long errorLevel = defaultErrorLevel;

        Map<String, BridgeConfig> bridgeConfig = new HashMap<String, BridgeConfig>();
    
        BrokerConfig(Long equipmentId, String equipmentName) {
            this.equipmentName = equipmentName;
            this.equipmentId = equipmentId;
            this.dataTagIds = new ServiceTest();
        }
        /**
         * @return Returns the equipmentId.
         */
        public long getEquipmentId() {
            return equipmentId;
        }

        /**
         * @return Returns the equipmentName.
         */
        public String getEquipmentName() {
            return equipmentName;
        }

        
        /**
         * @return Returns the brokerUrl.
         */
        public String getBrokerUrl() {
            return brokerUrl;
        }

        /**
         * @param brokerUrl The brokerUrl to set.
         */
        public void setBrokerUrl(String brokerUrl) {
            this.brokerUrl = brokerUrl;
        }

        public boolean hasBridgeConfigured() {
            return bridgeConfig.size() > 0;
        }
        
        public List<BridgeConfig> getBridges() {
            return new ArrayList<BridgeConfig>(bridgeConfig.values());
        }
        
        ServiceTest getDataTagIds() {
            return dataTagIds;
        }
        
        public BridgeConfig getBridgeConfigForBroker(String equipmentName) {
            return bridgeConfig.get(equipmentName);
        }
        public void putBrokerConfig(String equipmentName, BridgeConfig config) {
            bridgeConfig.put(equipmentName, config);
        }
        
        /**
         * @return Returns the warnLevel.
         */
        public long getWarnLevel() {
            return warnLevel;
        }
        /**
         * @param warnLevel The warnLevel to set.
         */
        public void setWarnLevel(long warnLevel) {
            this.warnLevel = warnLevel;
        }
        /**
         * @return Returns the errorLevel.
         */
        public long getErrorLevel() {
            return errorLevel;
        }
        /**
         * @param errorLevel The errorLevel to set.
         */
        public void setErrorLevel(long errorLevel) {
            this.errorLevel = errorLevel;
        }
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("BrokerUrl='" + getBrokerUrl() + "'," + getDataTagIds().toString() + ", ");
            if (hasBridgeConfigured()) {
                for (BridgeConfig bc : getBridges()) {
                    b.append(bc).append(" ");
                }
            }
            return b.toString();
        }
        
        
    static class BridgeConfig {
        private String equipmentUrl = null;
        private ServiceTest dataTagIds = new ServiceTest();
        
        BridgeConfig() {
            
        }
        
        BridgeConfig(String equipmentUrl) {
            this.equipmentUrl = equipmentUrl;
        }
        
        ServiceTest getDataTagIds() {
            return dataTagIds;
        }
        
        public String getRemoteUrl() {
            return equipmentUrl;
        }
        
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("BridgeConfig:' RemoteBrokerUrl='" + getRemoteUrl() + "'," + getDataTagIds().toString());
            return b.toString();
        }
        
    }
    
    
    /**
     * 
     * 
     */
    static class ServiceTest {
        
        Long topicDataTag = null;
        Long queueDataTag = null;
        Long connectionTestDataTag = null;
        
        /**
         * @return Returns the topicDataTag.
         */
        public Long getTopicDataTag() {
            return topicDataTag;
        }


        /**
         * @param topicDataTag The topicDataTag to set.
         */
        public void setTopicDataTag(Long topicDataTag) {
            this.topicDataTag = topicDataTag;
        }


        /**
         * @return Returns the queueDataTag.
         */
        public Long getQueueDataTag() {
            return queueDataTag;
        }


        /**
         * @param queueDataTag The queueDataTag to set.
         */
        public void setQueueDataTag(Long queueDataTag) {
            this.queueDataTag = queueDataTag;
        }
        
        
        
        /**
         * @return Returns the connectionTestDataTag.
         */
        public Long getConnTestDataTag() {
            return connectionTestDataTag;
        }


        /**
         * @param connectionTestDataTag The connectionTestDataTag to set.
         */
        public void setConnTestDataTag(Long connectionTestDataTag) {
            this.connectionTestDataTag = connectionTestDataTag;
        }


        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("TopicDataTag=" + getTopicDataTag() + ",QueueDataTag=" + getQueueDataTag() + ",ConnTestDataTag=" + getConnTestDataTag());
            return b.toString();
        }
    }
}
