/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.lifecycle;

import java.net.InetAddress;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.cluster.CacheCluster;
import cern.c2mon.server.ehcache.cluster.ClusterNode;
import cern.c2mon.server.ehcache.cluster.ClusterScheme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This bean listens for Terracotta cluster topology events (e.g. nodes joining
 * and leaving the cluster).
 *
 * Currently, no action is taken here when the connection to the cluster is
 * lost, as any attempt to access cache elements during that time will block.
 *
 * If the main Terracotta node goes down, the standby node should switch into
 * active state within a few seconds. No effect should be seen on the client
 * side in this case.
 *
 * @author Mark Brightwell
 * @author Justin Lewis Salmon
 *
 */
@Slf4j
public class ClusterTopologyListener implements cern.c2mon.server.ehcache.cluster.ClusterTopologyListener {

  private final static Logger SMS_LOGGER = LoggerFactory.getLogger("AdminSmsLogger");

  @Autowired
  public CacheManager cacheManager;

  @PostConstruct
  public void init() {
    CacheCluster cluster = cacheManager.getCluster(ClusterScheme.TERRACOTTA);
    cluster.addTopologyListener(this);
  }

  /**
   * A node has joined the cluster
   *
   * @param clusterNode The joining node
   */
  @Override
  public void nodeJoined(final ClusterNode clusterNode) {
    log.info("Detected Terracotta node joined cluster: " + clusterNode.getId());
  }

  /**
   * A node has left the cluster
   *
   * @param clusterNode The departing node
   */
  @Override
  public void nodeLeft(final ClusterNode clusterNode) {
    log.info("Detected Terracotta node left cluster: " + clusterNode.getId());
  }

  /**
   * This node has lost contact (possibly temporarily) with the cluster and
   * cannot execute clustered operations
   *
   * @param clusterNode The current node
   */
  @Override
  public void clusterOffline(ClusterNode clusterNode) {
    // Send a warning message that the node lost contact with the cluster
    log.warn("Detected Terracotta cluster offline event for node: " + clusterNode.getId());

    String hostname;
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      log.error("Unable to get local hostname", e);
      hostname = "#unknown host#";
    }

    // Send an SMS warning
    SMS_LOGGER.warn("Initiating C2MON server shutdown of " + System.getProperty("c2mon.server.name") + " on " + hostname);
  }

  /**
   * This node has established contact with the cluster and can execute
   * clustered operations.
   *
   * @param clusterNode The current node
   */
  @Override
  public void clusterOnline(ClusterNode clusterNode) {
    log.info("Detected Terracotta cluster online event for node: " + clusterNode.getId());
  }

  /**
   * This node lost contact and rejoined the cluster again. This event is only
   * fired in the node which rejoined and not to all the connected nodes
   *
   * @param oldNode - The old node which got disconnected
   * @param newNode - The new node after rejoin
   */
  @Override
  public void clusterRejoined(ClusterNode oldNode, ClusterNode newNode) {
    log.info("Detected Terracotta cluster rejoined event. Old node: " + oldNode.getId() + " New node: " + newNode.getId());
  }
}
