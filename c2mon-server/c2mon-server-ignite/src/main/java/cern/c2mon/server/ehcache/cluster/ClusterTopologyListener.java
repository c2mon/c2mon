/**
 * Copyright (c) 2021 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.server.ehcache.cluster;

public interface ClusterTopologyListener {

    /**
     * @param clusterNode
     */
    void nodeJoined(ClusterNode clusterNode);

    /**
     * @param clusterNode
     */
    void nodeLeft(ClusterNode clusterNode);

    /**
     * @param clusterNode
     */
    void clusterOffline(ClusterNode clusterNode);

    /**
     * @param clusterNode
     */
    void clusterOnline(ClusterNode clusterNode);

    /**
     * @param oldNode
     * @param newNode
     */
    void clusterRejoined(ClusterNode oldNode, ClusterNode newNode);

}
