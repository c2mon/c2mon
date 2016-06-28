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
package cern.c2mon.client.core.cache;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jms.JMSException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.manager.CoreSupervisionManager;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * @author Matthias Braeger
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:test-config/c2mon-cache-test.xml" })
@DirtiesContext
public class ClientDataTagCacheImplTest {
  /**
   * Component to test
   */
  @Autowired
  private ClientDataTagCacheImpl cache;
  @Autowired
  private JmsProxy jmsProxyMock;
  @Autowired
  @Qualifier("coreRequestHandler")
  private RequestHandler requestHandlerMock;
  @Autowired
  private CoreSupervisionManager supervisionManagerMock;
  @Autowired
  private CacheSynchronizer cacheSynchronizer;
  @Autowired
  private CacheController cacheController;

  @Before
  public void init() {
    EasyMock.reset(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
    cacheController.getLiveCache().clear();
    cacheController.getHistoryCache().clear();
  }

  @Test
  public void testEmptyCache() {
    assertEquals(0, cache.getAllSubscribedDataTags().size());
  }


  /**
   * Adds two tags into the cache and subscribes them to a <code>DataTagUpdateListener</code>.
   * @throws Exception
   */
  @Test
  public void testAddDataTagUpdateListener() throws Exception {
    // Test setup
    Set<Long> tagIds = new HashSet<Long>();
    tagIds.add(1L);
    tagIds.add(2L);
    Collection<TagUpdate> serverUpdates = new ArrayList<TagUpdate>(tagIds.size());
    for (Long tagId : tagIds) {
      serverUpdates.add(createValidTransferTag(tagId));
      prepareClientDataTagCreateMock(tagId);
    }
    EasyMock.expect(requestHandlerMock.requestTags(tagIds)).andReturn(serverUpdates);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds)).andReturn(new ArrayList<TagValueUpdate>(serverUpdates));
    DataTagUpdateListener listener = EasyMock.createMock(DataTagUpdateListener.class);

    // run test
    EasyMock.replay(jmsProxyMock, requestHandlerMock);
    Collection<Tag> cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(0, cachedTags.size());
    cache.subscribe(tagIds, listener);

    cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(2, cachedTags.size());

    Thread.sleep(500);

    // check test success
    EasyMock.verify(jmsProxyMock, requestHandlerMock);
  }


  @Test
  public void testUnsubscribeAllDataTags() throws Exception {
    // test setup
    Set<Long> tagIds = new HashSet<Long>();
    tagIds.add(1L);
    tagIds.add(2L);
    Collection<TagUpdate> serverUpdates = new ArrayList<TagUpdate>(tagIds.size());
    for (Long tagId : tagIds) {
      serverUpdates.add(createValidTransferTag(tagId));
      ClientDataTagImpl cdtMock = prepareClientDataTagCreateMock(tagId);
      jmsProxyMock.unregisterUpdateListener(cdtMock);
      supervisionManagerMock.removeSupervisionListener(cdtMock);
    }
    EasyMock.expect(requestHandlerMock.requestTags(tagIds)).andReturn(serverUpdates);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds)).andReturn(new ArrayList<TagValueUpdate>(serverUpdates));
    DataTagUpdateListener listener1 = EasyMock.createMock(DataTagUpdateListener.class);
    DataTagUpdateListener listener2 = EasyMock.createMock(DataTagUpdateListener.class);

    // run test
    EasyMock.replay(jmsProxyMock, requestHandlerMock);
    cache.subscribe(tagIds, listener1);
    Thread.sleep(200);
    Collection<Tag> cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(2, cachedTags.size());
    // NOT Registered listener
    cache.unsubscribeAllDataTags(listener2);
    Thread.sleep(200);
    cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(2, cachedTags.size());
    // Registered listener
    cache.unsubscribeAllDataTags(listener1);
    Thread.sleep(200);
    cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(0, cachedTags.size());

    Thread.sleep(200);
    //Thread.sleep(2500);

    // check test success
    EasyMock.verify(jmsProxyMock, requestHandlerMock);
  }


  @Test
  public void testContainsTag() throws Exception {
    // Test setup
    Set<Long> tagIds = new HashSet<Long>();
    tagIds.add(1L);
    tagIds.add(2L);
    Collection<TagUpdate> serverUpdates = new ArrayList<TagUpdate>(tagIds.size());
    for (Long tagId : tagIds) {
      serverUpdates.add(createValidTransferTag(tagId));
      prepareClientDataTagCreateMock(tagId);
    }
    EasyMock.expect(requestHandlerMock.requestTags(tagIds)).andReturn(serverUpdates);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds)).andReturn(new ArrayList<TagValueUpdate>(serverUpdates));
    EasyMock.expectLastCall();
    DataTagUpdateListener listener = EasyMock.createMock(DataTagUpdateListener.class);

    // run test
    EasyMock.replay(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
    cache.subscribe(tagIds, listener);
    assertTrue(cache.containsTag(1L));
    assertTrue(cache.containsTag(2L));
    assertFalse(cache.containsTag(23423L));

    Thread.sleep(500);

    // check test success
    EasyMock.verify(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
  }

  @Test
  public void testHistoryMode() throws Exception {
    // Test setup
    Set<Long> tagIds = new HashSet<Long>();
    tagIds.add(1L);
    tagIds.add(2L);
    Collection<TagUpdate> serverUpdates = new ArrayList<TagUpdate>(tagIds.size());
    for (Long tagId : tagIds) {
      serverUpdates.add(createValidTransferTag(tagId));
      prepareClientDataTagCreateMock(tagId);
    }
    EasyMock.expect(requestHandlerMock.requestTags(tagIds)).andReturn(serverUpdates);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds)).andReturn(new ArrayList<TagValueUpdate>(serverUpdates));
    DataTagUpdateListener listener = EasyMock.createMock(DataTagUpdateListener.class);

    // run test
    EasyMock.replay(jmsProxyMock, requestHandlerMock);
    cache.subscribe(tagIds, listener);
    cache.setHistoryMode(true);
    for (Long tagId : tagIds) {
      assertTrue(cache.containsTag(tagId));
    }
    assertFalse(cache.containsTag(23423L));

    assertTrue(cache.isHistoryModeEnabled());
    cache.subscribe(tagIds, listener);
    Collection<Tag> cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(2, cachedTags.size());

    Thread.sleep(500);

    // check test success
    EasyMock.verify(jmsProxyMock, requestHandlerMock);
    cache.setHistoryMode(false);
  }


  private ClientDataTagImpl prepareClientDataTagCreateMock(final Long tagId) throws RuleFormatException, JMSException {
    ClientDataTagImpl cdtMock = new ClientDataTagImpl(tagId);
    cdtMock.update(createValidTransferTag(tagId));
    // In case of a CommFault- or Status control tag, we don't register to supervision invalidations
    if (!cdtMock.isControlTag() || cdtMock.isAliveTag()) {
      supervisionManagerMock.addSupervisionListener(cdtMock, cdtMock.getProcessIds(), cdtMock.getEquipmentIds(), cdtMock.getSubEquipmentIds());
    }
    EasyMock.expect(jmsProxyMock.isRegisteredListener(cdtMock)).andReturn(false);
    jmsProxyMock.registerUpdateListener(cdtMock, cdtMock);

    return cdtMock;
  }

  private TagUpdate createValidTransferTag(final Long tagId) {
    return createValidTransferTag(tagId, Float.valueOf(1.234f));
  }

  private TagUpdate createValidTransferTag(final Long tagId, Object value) {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    tagQuality.validate();
    TagUpdate tagUpdate =
        new TransferTagImpl(
            tagId,
            value,
            "test value desc",
            (DataTagQualityImpl) tagQuality,
            TagMode.TEST,
            new Timestamp(System.currentTimeMillis() - 10000L),
            new Timestamp(System.currentTimeMillis() - 5000L),
            new Timestamp(System.currentTimeMillis()),
            "Test description",
            "My.data.tag.name",
            "My.jms.topic");

    return tagUpdate;
  }
}
