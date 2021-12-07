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
}
