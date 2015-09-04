/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.common.listener.DataTagListener;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.AlarmService;
import cern.c2mon.client.core.ConfigurationService;
import cern.c2mon.client.core.StatisticsService;
import cern.c2mon.client.core.cache.CacheSynchronizationException;
import cern.c2mon.client.core.listener.TagSubscriptionListener;
import cern.c2mon.client.core.service.TagServiceImpl;
import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.statistics.TagStatisticsResponse;
import cern.c2mon.shared.client.tag.TagConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * The tag manager implements the <code>C2monTagManager</code> interface. It's main job is to delegate cache requests
 * and to update the cache with new registered tags. Therefore it has to request an initial update from the C2MON
 * server.
 * <p>
 * Please note that the <code>TagServiceImpl</code> is not in charge of registering the <code>ClientDataTags</code> to the
 * <code>JmsProxy</code> nor to the <code>SupervisionManager</code>. This is done directly by the cache.
 *
 * @deprecated Use directly the new Service implementations instead
 * @author Matthias Braeger
 */
@Service @Deprecated @Slf4j
public class TagManager implements CoreTagManager {

  private final TagServiceImpl tagService;
  
  private final AlarmService alarmService;
  
  private final ConfigurationService configurationService;
  
  private final StatisticsService statisticsService;
  
  @Autowired
  public TagManager(final TagServiceImpl tagService,
                    final AlarmService alarmService,
                    final ConfigurationService configurationService,
                    final StatisticsService statisticsService) {
    this.tagService = tagService;
    this.alarmService = alarmService;
    this.configurationService = configurationService;
    this.statisticsService = statisticsService;
  }
  
  @Override
  public void subscribeDataTags(Set<Long> dataTagIds, DataTagUpdateListener listener) throws CacheSynchronizationException {
    tagService.doSubscription(dataTagIds, listener);
  }

  @Override
  public void subscribeDataTag(Long dataTagId, DataTagUpdateListener listener) throws CacheSynchronizationException {
    if (dataTagId == null) {
      String error = "Called with null parameter (id collection).";
      log.warn("subscribeDataTag() : " + error);
      throw new IllegalArgumentException(error);
    }

    Set<Long> id = new HashSet<>(1);
    id.add(dataTagId);
    subscribeDataTags(id, listener);
  }

  @Override
  public void subscribeDataTags(Set<Long> dataTagIds, DataTagListener listener) throws CacheSynchronizationException {
    tagService.doSubscription(dataTagIds, listener);
  }

  @Override
  public void subscribeDataTag(Long dataTagId, DataTagListener listener) throws CacheSynchronizationException {
    if (dataTagId == null) {
      String error = "Called with null parameter (id collection).";
      log.warn("subscribeDataTag() : " + error);
      throw new IllegalArgumentException(error);
    }

    Set<Long> id = new HashSet<>(1);
    id.add(dataTagId);
    subscribeDataTags(id, listener);
  }

  @Override
  public void unsubscribeDataTags(Set<Long> dataTagIds, DataTagUpdateListener listener) {
    tagService.unsubscribeDataTags(dataTagIds, listener);
  }

  @Override
  public void unsubscribeDataTag(Long dataTagId, DataTagUpdateListener listener) {
    if (dataTagId == null) {
      String error = "Called with null parameter (id collection).";
      log.warn("unsubscribeDataTag() : " + error);
      throw new IllegalArgumentException(error);
    }

    Set<Long> id = new HashSet<>(1);
    id.add(dataTagId);
    unsubscribeDataTags(id, listener);
  }

  @Override
  public void unsubscribeAllDataTags(DataTagUpdateListener listener) {
    tagService.unsubscribeAllDataTags(listener); 
  }

  @Override
  public Collection<ClientDataTagValue> getAllSubscribedDataTags(DataTagUpdateListener listener) {
    return tagService.getSubscriptions(listener);
  }

  @Override
  public Set<Long> getAllSubscribedDataTagIds(DataTagUpdateListener listener) {
    return tagService.getAllSubscribedDataTagIds(listener);
  }

  @Override
  public ClientDataTagValue getDataTag(Long tagId) {
    return (ClientDataTagValue) tagService.get(tagId);
  }

  @Override
  public Collection<ClientDataTagValue> getDataTags(Collection<Long> tagIds) {
    Collection<? extends Tag> result =  tagService.get(tagIds);
    return (Collection<ClientDataTagValue>) result;
  }

  @Override
  public Collection<TagConfig> getTagConfigurations(Collection<Long> tagIds) {
    return configurationService.getTagConfigurations(tagIds);
  }

  @Override
  public Collection<AlarmValue> getAlarms(Collection<Long> alarmIds) {
    return alarmService.getAlarms(alarmIds);
  }

  @Override
  public Collection<AlarmValue> getAllActiveAlarms() {
    return alarmService.getAllActiveAlarms();
  }

  @Override
  public ConfigurationReport applyConfiguration(Long configurationId) {
    return configurationService.applyConfiguration(configurationId);
  }

  @Override
  public ConfigurationReport applyConfiguration(Long configurationId, ClientRequestReportListener reportListener) {
    return configurationService.applyConfiguration(configurationId, reportListener);
  }

  @Override
  public Collection<ConfigurationReportHeader> getConfigurationReports() {
    return configurationService.getConfigurationReports();
  }

  @Override
  public Collection<ConfigurationReport> getConfigurationReports(Long id) {
    return configurationService.getConfigurationReports(id);
  }

  @Override
  public String getProcessXml(String processName) {
    return configurationService.getProcessXml(processName);
  }

  @Override
  public Collection<ProcessNameResponse> getProcessNames() {
    return configurationService.getProcessNames();
  }

  @Override
  public int getCacheSize() {
    return tagService.getCacheSize();
  }

  @Override
  public TagStatisticsResponse getTagStatistics() {
    return statisticsService.getTagStatistics();
  }

  @Override
  public void refreshDataTags() throws CacheSynchronizationException {
    tagService.refresh(); 
  }

  @Override
  public void refreshDataTags(Collection<Long> tagIds) throws CacheSynchronizationException {
    tagService.refresh(tagIds);
  }

  @Override
  public void removeAlarmListener(AlarmListener listener) throws JMSException {
    alarmService.removeAlarmListener(listener);
  }

  @Override
  public void addAlarmListener(AlarmListener listener) throws JMSException {
    alarmService.addAlarmListener(listener);
  }

  @Override
  public boolean isSubscribed(DataTagUpdateListener listener) {
    return tagService.isSubscribed(listener);
  }

  @Override
  public void addTagSubscriptionListener(TagSubscriptionListener listener) {
    tagService.addTagSubscriptionListener(listener);
  }

  @Override
  public void removeTagSubscriptionListener(TagSubscriptionListener listener) {
    tagService.removeTagSubscriptionListener(listener);
  } 
}
