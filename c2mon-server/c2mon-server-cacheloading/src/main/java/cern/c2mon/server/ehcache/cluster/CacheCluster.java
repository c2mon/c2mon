/**
 * Copyright (c) 2021 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.server.ehcache.cluster;

import cern.c2mon.server.lifecycle.ClusterTopologyListener;

public interface CacheCluster {

    /**
     * @param clusterTopologyListener
     */
    void addTopologyListener(ClusterTopologyListener clusterTopologyListener);

}
