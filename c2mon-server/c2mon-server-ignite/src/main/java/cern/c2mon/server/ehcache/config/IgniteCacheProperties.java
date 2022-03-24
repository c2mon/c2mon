package cern.c2mon.server.ehcache.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "c2mon.server.cache.ignite")
public class IgniteCacheProperties {

    private boolean clientMode = false;

    /**
     * Read more : https://ignite.apache.org/docs/latest/clustering/tcp-ip-discovery
     */
    private List<String> ipFinderAddresses = Arrays.asList("localhost", "localhost:47500..47509");

    /**
     * Read more : https://ignite.apache.org/docs/latest/configuring-caches/on-heap-caching
     */
    private boolean onHeapCacheEnabled = false;

    /**
     * Read more : https://ignite.apache.org/docs/latest/data-modeling/data-partitioning#partitionedreplicated-mode
     */
    private String cacheMode = "PARTITIONED";

    /**
     * Read more : https://ignite.apache.org/docs/latest/configuring-caches/configuring-backups
     * Should be at least 1 otherwise data is lost if one node of the cluster becomes unavailable
     */
    private int numberOfBackups = 1;

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
     * Read more : https://ignite.apache.org/docs/latest/monitoring-metrics/configuring-metrics
     */
    private boolean statisticsEnabled = false;

    /**
     * Read more : https://ignite.apache.org/docs/latest/monitoring-metrics/new-metrics-system
     */
    private boolean enableJmxMetrics = false;

    /**
     * Read more : https://ignite.apache.org/docs/latest/configuring-caches/atomicity-modes
     */
    private String atomicityMode = "ATOMIC";

    /**
     * Read more : https://ignite.apache.org/docs/latest/key-value-api/transactions#long-running-transactions-termination
     * Default is 20 seconds
     */
    private Integer txTimeoutOnPartitionMapExchange = 20000;

    /**
     * Default data region name
     * Read more : https://ignite.apache.org/docs/latest/memory-configuration/data-regions#configuring-data-regions
     */
    private String defaultRegionName = "Default_Region";

    /**
     * Default data region initial size (500 MB initial size)
     * Read more : https://ignite.apache.org/docs/latest/memory-configuration/data-regions#configuring-data-regions
     */
    private long defaultRegionInitialSize = 500L * 1024 * 1024;

    /**
     * Default data region max size (20 GB maximum size).
     * Read more : https://ignite.apache.org/docs/latest/memory-configuration/data-regions#configuring-data-regions
     */
    private long defaultRegionMaxSize = 20L * 1024 * 1024 * 1024;

    /**
     * Default data region persistence enabled
     * Read more : https://ignite.apache.org/docs/latest/persistence/native-persistence
     */
    private boolean defaultRegionPersistenceEnabled = false;

    /**
     * Default data region page eviction mode
     * Read more : https://ignite.apache.org/docs/latest/memory-configuration/eviction-policies#eviction-policies
     */
    private String defaultRegionPageEvictionMode = "DISABLED";

    /**
     * Default data region metrics enabled
     * Read more : https://ignite.apache.org/docs/latest/monitoring-metrics/configuring-metrics
     */
    private boolean defaultRegionMetricsEnabled = false;

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

    public boolean isClientMode() {
        return clientMode;
    }

    public void setClientMode(boolean clientMode) {
        this.clientMode = clientMode;
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

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    public boolean isEnableJmxMetrics() {
        return enableJmxMetrics;
    }

    public void setEnableJmxMetrics(boolean enableJmxMetrics) {
        this.enableJmxMetrics = enableJmxMetrics;
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

    public Integer getTxTimeoutOnPartitionMapExchange() {
        return txTimeoutOnPartitionMapExchange;
    }

    public void setTxTimeoutOnPartitionMapExchange(Integer txTimeoutOnPartitionMapExchange) {
        this.txTimeoutOnPartitionMapExchange = txTimeoutOnPartitionMapExchange;
    }

    public String getDefaultRegionName() {
        return defaultRegionName;
    }

    public void setDefaultRegionName(String defaultRegionName) {
        this.defaultRegionName = defaultRegionName;
    }

    public long getDefaultRegionInitialSize() {
        return defaultRegionInitialSize;
    }

    public void setDefaultRegionInitialSize(long defaultRegionInitialSize) {
        this.defaultRegionInitialSize = defaultRegionInitialSize;
    }

    public long getDefaultRegionMaxSize() {
        return defaultRegionMaxSize;
    }

    public void setDefaultRegionMaxSize(long defaultRegionMaxSize) {
        this.defaultRegionMaxSize = defaultRegionMaxSize;
    }

    public boolean isDefaultRegionPersistenceEnabled() {
        return defaultRegionPersistenceEnabled;
    }

    public void setDefaultRegionPersistenceEnabled(boolean defaultRegionPersistenceEnabled) {
        this.defaultRegionPersistenceEnabled = defaultRegionPersistenceEnabled;
    }

    public String getDefaultRegionPageEvictionMode() {
        return defaultRegionPageEvictionMode;
    }

    public void setDefaultRegionPageEvictionMode(String defaultRegionPageEvictionMode) {
        this.defaultRegionPageEvictionMode = defaultRegionPageEvictionMode;
    }

    public boolean isDefaultRegionMetricsEnabled() {
        return defaultRegionMetricsEnabled;
    }

    public void setDefaultRegionMetricsEnabled(boolean defaultRegionMetricsEnabled) {
        this.defaultRegionMetricsEnabled = defaultRegionMetricsEnabled;
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
