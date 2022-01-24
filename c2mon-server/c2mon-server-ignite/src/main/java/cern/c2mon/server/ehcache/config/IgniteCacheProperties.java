package cern.c2mon.server.ehcache.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "c2mon.server.cache.ignite")
public class IgniteCacheProperties {

    private boolean embedded = true;

    /**
     * Read more : https://ignite.apache.org/docs/latest/clustering/tcp-ip-discovery
     */
    private List<String> ipFinderAddresses = Arrays.asList("localhost", "localhost:47500..47509");

    private boolean onHeapCacheEnabled = false;

    /**
     * Read more : https://ignite.apache.org/docs/latest/data-modeling/data-partitioning#partitionedreplicated-mode
     */
    private String cacheMode = "PARTITIONED";

    private int numberOfBackups = 0;

    /**
     * Read more : https://ignite.apache.org/docs/latest/configuring-caches/configuration-overview
     */
    private String cacheRebalanceMode = "ASYNC";

    /**
     * Read more : https://ignite.apache.org/docs/latest/configuring-caches/configuring-backups
     */
    private String cacheWriteSynchronizationMode = "PRIMARY_SYNC";

    /**
     * Read more : https://ignite.apache.org/docs/latest/configuring-caches/partition-loss-policy
     */
    private String partitionLossPolicy = "IGNORE";

    /**
     * https://ignite.apache.org/docs/latest/configuring-caches/atomicity-modes
     */
    private String atomicityMode = "ATOMIC";

    /**
     * Read more : https://ignite.apache.org/docs/latest/key-value-api/transactions#long-running-transactions-termination
     * Default is 20 seconds
     */
    private Integer setTxTimeoutOnPartitionMapExchange = 20000;

    /**
     * Message queue limit is set to 0 (by default) which may lead to potential OOMEs when running cache operations in FULL_ASYNC or
     * PRIMARY_SYNC modes due to message queues growth on sender and receiver sides.
     */
    private Integer messageQueueLimit = 1024;

    /**
     * Sets frequency of metrics log print out.
     * If 0, metrics print out is disabled.
     */
    private long metricsLogFrequency = 0;

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public boolean isOnHeapCacheEnabled() {
        return onHeapCacheEnabled;
    }

    public void setOnHeapCacheEnabled(boolean onHeapCacheEnabled) {
        this.onHeapCacheEnabled = onHeapCacheEnabled;
    }

    public String getCacheMode() {
        return cacheMode;
    }

    public void setCacheMode(String cacheMode) {
        this.cacheMode = cacheMode;
    }

    public int getNumberOfBackups() {
        return numberOfBackups;
    }

    public void setNumberOfBackups(int numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
    }

    public String getCacheRebalanceMode() {
        return cacheRebalanceMode;
    }

    public void setCacheRebalanceMode(String cacheRebalanceMode) {
        this.cacheRebalanceMode = cacheRebalanceMode;
    }

    public String getCacheWriteSynchronizationMode() {
        return cacheWriteSynchronizationMode;
    }

    public void setCacheWriteSynchronizationMode(String cacheWriteSynchronizationMode) {
        this.cacheWriteSynchronizationMode = cacheWriteSynchronizationMode;
    }

    public String getPartitionLossPolicy() {
        return partitionLossPolicy;
    }

    public void setPartitionLossPolicy(String partitionLossPolicy) {
        this.partitionLossPolicy = partitionLossPolicy;
    }

    public List<String> getIpFinderAddresses() {
        return ipFinderAddresses;
    }

    public void setIpFinderAddresses(List<String> ipFinderAddresses) {
        this.ipFinderAddresses = ipFinderAddresses;
    }

    public String getAtomicityMode() {
        return atomicityMode;
    }

    public void setAtomicityMode(String atomicityMode) {
        this.atomicityMode = atomicityMode;
    }

    public Integer getSetTxTimeoutOnPartitionMapExchange() {
        return setTxTimeoutOnPartitionMapExchange;
    }

    public void setSetTxTimeoutOnPartitionMapExchange(Integer setTxTimeoutOnPartitionMapExchange) {
        this.setTxTimeoutOnPartitionMapExchange = setTxTimeoutOnPartitionMapExchange;
    }

    public Integer getMessageQueueLimit() {
        return messageQueueLimit;
    }

    public void setMessageQueueLimit(Integer messageQueueLimit) {
        this.messageQueueLimit = messageQueueLimit;
    }

    public long getMetricsLogFrequency() {
        return metricsLogFrequency;
    }

    public void setMetricsLogFrequency(long metricsLogFrequency) {
        this.metricsLogFrequency = metricsLogFrequency;
    }
}
