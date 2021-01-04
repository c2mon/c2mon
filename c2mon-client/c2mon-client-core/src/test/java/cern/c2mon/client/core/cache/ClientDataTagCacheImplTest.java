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

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.config.C2monAutoConfiguration;
import cern.c2mon.client.core.config.mock.CoreSupervisionServiceMock;
import cern.c2mon.client.core.config.mock.JmsProxyMock;
import cern.c2mon.client.core.config.mock.RequestHandlerMock;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.client.core.jms.RequestHandler;
import cern.c2mon.client.core.service.CoreSupervisionService;
import cern.c2mon.client.core.tag.TagController;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.rule.RuleFormatException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.JMSException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.anyObject;

/**
 * @author Matthias Braeger
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    C2monAutoConfiguration.class,
    JmsProxyMock.class,
    RequestHandlerMock.class,
    CoreSupervisionServiceMock.class
})
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
  private RequestHandler requestHandlerMock;

  @Autowired
  private CoreSupervisionService supervisionManagerMock;

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
    Set<Long> tagIds = new HashSet<>();
    tagIds.add(1L);
    tagIds.add(2L);
    Collection<TagUpdate> serverUpdates = new ArrayList<>(tagIds.size());
    for (Long tagId : tagIds) {
      serverUpdates.add(createValidTransferTag(tagId));
      prepareClientDataTagCreateMock(tagId);
    }
    EasyMock.expect(requestHandlerMock.requestTags(tagIds)).andReturn(serverUpdates);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds)).andReturn(new ArrayList<>(serverUpdates));
    TagListener listener = EasyMock.createMock(TagListener.class);

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
    Set<Long> tagIds = new HashSet<>();
    tagIds.add(1L);
    tagIds.add(2L);
    Collection<TagUpdate> serverUpdates = new ArrayList<>(tagIds.size());
    for (Long tagId : tagIds) {
      serverUpdates.add(createValidTransferTag(tagId));
      TagController cdtMock = prepareClientDataTagCreateMock(tagId);
      supervisionManagerMock.removeSupervisionListener(cdtMock);
    }
    EasyMock.expect(requestHandlerMock.requestTags(tagIds)).andReturn(serverUpdates);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds)).andReturn(new ArrayList<TagValueUpdate>(serverUpdates));
    TagListener listener1 = EasyMock.createMock(TagListener.class);
    TagListener listener2 = EasyMock.createMock(TagListener.class);

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

    // check test success
    EasyMock.verify(jmsProxyMock, requestHandlerMock);
  }


  @Test
  public void testContainsTag() throws Exception {
    // Test setup
    Set<Long> tagIds = new HashSet<>();
    tagIds.add(1L);
    tagIds.add(2L);
    Collection<TagUpdate> serverUpdates = new ArrayList<>(tagIds.size());
    for (Long tagId : tagIds) {
      serverUpdates.add(createValidTransferTag(tagId));
      prepareClientDataTagCreateMock(tagId);
    }
    EasyMock.expect(requestHandlerMock.requestTags(tagIds)).andReturn(serverUpdates);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds)).andReturn(new ArrayList<>(serverUpdates));
    EasyMock.expectLastCall();
    supervisionManagerMock.addSupervisionListener(anyObject(), anyObject(), anyObject(), anyObject());
    EasyMock.expectLastCall().times(2);
    TagListener listener = EasyMock.createMock(TagListener.class);

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
    Set<Long> tagIds = new HashSet<>();
    tagIds.add(1L);
    tagIds.add(2L);
    Collection<TagUpdate> serverUpdates = new ArrayList<>(tagIds.size());
    for (Long tagId : tagIds) {
      serverUpdates.add(createValidTransferTag(tagId));
      prepareClientDataTagCreateMock(tagId);
    }
    EasyMock.expect(requestHandlerMock.requestTags(tagIds)).andReturn(serverUpdates);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds)).andReturn(new ArrayList<>(serverUpdates));
    TagListener listener = EasyMock.createMock(TagListener.class);

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


  private TagController prepareClientDataTagCreateMock(final Long tagId) throws RuleFormatException, JMSException {
    TagController cdtMock = new TagController(tagId);
    cdtMock.update(createValidTransferTag(tagId));

    return cdtMock;
  }

  private TagUpdate createValidTransferTag(final Long tagId) {
    return createValidTransferTag(tagId, Float.valueOf(1.234f));
  }

  private TagUpdate createValidTransferTag(final Long tagId, Object value) {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    tagQuality.validate();
    TransferTagImpl tagUpdate =
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

    if (value != null) {
      tagUpdate.setValueClassName(value.getClass().getName());
    }

    return tagUpdate;
  }
}
