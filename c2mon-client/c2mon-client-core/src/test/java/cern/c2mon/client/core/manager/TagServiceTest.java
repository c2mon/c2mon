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
package cern.c2mon.client.core.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import cern.c2mon.client.core.config.C2monAutoConfiguration;
import cern.c2mon.client.core.config.mock.CoreSupervisionServiceMock;
import cern.c2mon.client.core.config.mock.JmsProxyMock;
import cern.c2mon.client.core.config.mock.RequestHandlerMock;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.client.core.jms.RequestHandler;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.core.tag.TagController;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import org.junit.Ignore;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    C2monAutoConfiguration.class,
    JmsProxyMock.class,
    RequestHandlerMock.class,
    CoreSupervisionServiceMock.class
})
public class TagServiceTest {

  /**
   * Component to test
   */
  @Autowired
  private TagService tagService;

  @Autowired
  @Qualifier("coreRequestHandler")
  private RequestHandler requestHandlerMock;

  @Autowired
  private JmsProxy jmsProxyMock;

  @Before
  public void setUp() {
    EasyMock.reset(requestHandlerMock, jmsProxyMock);
  }

  @Test @DirtiesContext
  public void testSubscribeDataTags() throws Exception {
    // Test setup
    Set<Long> tagIds1 = new HashSet<>();
    for (long i = 1; i <= 1000; i++) {
      tagIds1.add(i);
    }

    // Use a CountDownLatch as an update listener to allow the subscription
    // thread to finish. Note that the listener will be called twice per tag.
    final CountDownLatch latch1 = new CountDownLatch(tagIds1.size() * 2);
    BaseTagListener listener1 = tagUpdate -> latch1.countDown();

    Collection<TagUpdate> serverUpdates1 = tagIds1.stream().map(this::createValidTransferTag).collect(Collectors.toCollection(ArrayList::new));
    Collection<TagValueUpdate> serverUpdateValues1 = tagIds1.stream().map(this::createValidTransferTag).collect(Collectors.toCollection(ArrayList::new));

    EasyMock.expect(requestHandlerMock.requestTags(tagIds1)).andReturn(serverUpdates1);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds1)).andReturn(serverUpdateValues1);

    // listener 2
    Set<Long> tagIds2 = new HashSet<>();
    for (long i = 1001; i <= 2000; i++) {
      tagIds2.add(i);
    }

    // Use another latch for the second listener
    final CountDownLatch latch2 = new CountDownLatch(tagIds2.size() * 2);
    BaseTagListener listener2 = tagUpdate -> latch2.countDown();

    Collection<TagUpdate> serverUpdates2 = tagIds2.stream().map(this::createValidTransferTag).collect(Collectors.toCollection(ArrayList::new));
    Collection<TagValueUpdate> serverUpdateValues2 = tagIds2.stream().map(this::createValidTransferTag).collect(Collectors.toCollection(ArrayList::new));

    EasyMock.expect(requestHandlerMock.requestTags(tagIds2)).andReturn(serverUpdates2);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds2)).andReturn(serverUpdateValues2);

    // Run the test
    EasyMock.replay(requestHandlerMock, jmsProxyMock);

    tagService.subscribe(tagIds1, listener1);
    Collection<Tag> cdtValues = tagService.getSubscriptions(listener1);
    Assert.assertEquals(tagIds1.size(), cdtValues.size());
    Assert.assertEquals(tagIds1.size(), tagService.getSubscriptionIds(listener1).size());

    // Wait for onUpdate() to be called for all tags
    latch1.await();

    // second call for second listener
    tagService.subscribe(tagIds2, listener2);
    cdtValues = tagService.getSubscriptions(listener2);
    Assert.assertEquals(tagIds2.size(), cdtValues.size());
    Assert.assertEquals(tagIds2.size(), tagService.getSubscriptionIds(listener2).size());

    // Wait for onUpdate() to be called for all tags
    latch2.await();

    // check test success
    EasyMock.verify(requestHandlerMock, jmsProxyMock);
  }

  @Test @DirtiesContext
  public void testSubscribeDataTagUpdates() throws Exception {
    final List<Boolean> check = new ArrayList<>();
    // Test setup
    final Set<Long> tagIds1 = new HashSet<Long>();
    for (long i = 1; i <= 1000; i++) {
      tagIds1.add(i);
    }

    // Use a CountDownLatch as an update listener to allow the subscription
    // thread to finish
    final CountDownLatch latch1 = new CountDownLatch(tagIds1.size());
    TagListener listener1 = new TagListener() {
      @Override
      public void onUpdate(Tag tagUpdate) {
        latch1.countDown();
      }

      @Override
      public void onInitialUpdate(Collection<Tag> initialValues) {
        Assert.assertEquals(tagIds1.size(), initialValues.size());
        Assert.assertEquals(tagIds1.size(), latch1.getCount());
        check.add(Boolean.TRUE);
      }
    };

    Collection<TagUpdate> serverUpdates1 = tagIds1.stream().map(this::createValidTransferTag).collect(Collectors.toCollection(ArrayList::new));
    Collection<TagValueUpdate> serverUpdateValues1 = tagIds1.stream().map(this::createValidTransferTag).collect(Collectors.toCollection(ArrayList::new));

    EasyMock.expect(requestHandlerMock.requestTags(tagIds1)).andReturn(serverUpdates1);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds1)).andReturn(serverUpdateValues1);

    // listener 2
    final Set<Long> tagIds2 = new HashSet<Long>();
    for (long i = 1001; i <= 2000; i++) {
      tagIds2.add(i);
    }

    final CountDownLatch latch2 = new CountDownLatch(tagIds2.size());
    TagListener listener2 = new TagListener() {
      @Override
      public void onUpdate(Tag tagUpdate) {
        latch2.countDown();
      }

      @Override
      public void onInitialUpdate(Collection<Tag> initialValues) {
        Assert.assertEquals(tagIds2.size(), initialValues.size());
        Assert.assertEquals(tagIds2.size(), latch2.getCount());
        check.add(Boolean.TRUE);
      }
    };

    Collection<TagUpdate> serverUpdates2 = tagIds2.stream().map(this::createValidTransferTag).collect(Collectors.toCollection(ArrayList::new));
    Collection<TagValueUpdate> serverUpdateValues2 = tagIds2.stream().map(this::createValidTransferTag).collect(Collectors.toCollection(ArrayList::new));

    EasyMock.expect(requestHandlerMock.requestTags(tagIds2)).andReturn(serverUpdates2);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds2)).andReturn(serverUpdateValues2);

    EasyMock.replay(requestHandlerMock, jmsProxyMock);

    // run test
    tagService.subscribe(tagIds1, listener1);
    Assert.assertEquals(tagIds1.size(), tagService.getSubscriptionIds(listener1).size());

    latch1.await();

    // second call for second listener
    tagService.subscribe(tagIds2, listener2);

    latch2.await();

    Assert.assertEquals(tagIds2.size(), tagService.getSubscriptionIds(listener2).size());
    Assert.assertEquals(2, check.size());

    // check test success
    EasyMock.verify(requestHandlerMock, jmsProxyMock);
  }

  @Test @DirtiesContext
  @Ignore("This test is flaky!")
  public void testUnsubscribeDataTags() throws JMSException, InterruptedException {
    Set<Long> tagIds1 = new HashSet<>();
    for (long i = 1L; i <= 1000; i++) {
      tagIds1.add(i);
    }

    CountDownLatch latch = new CountDownLatch(tagIds1.size());
    BaseTagListener listener1 = tagUpdate -> latch.countDown();

    Collection<TagUpdate> serverUpdates1 = tagIds1.stream().map(this::createValidTransferTag).collect(Collectors.toCollection(ArrayList::new));
    Collection<TagValueUpdate> serverUpdateValues1 = tagIds1.stream().map(this::createValidTransferTag).collect(Collectors.toCollection(ArrayList::new));

    EasyMock.expect(requestHandlerMock.requestTags(tagIds1)).andReturn(serverUpdates1);
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds1)).andReturn(serverUpdateValues1);

    EasyMock.replay(requestHandlerMock, jmsProxyMock);

    // run test
    tagService.subscribe(tagIds1, listener1);
    Collection<Tag> cdtValues = tagService.getSubscriptions(listener1);
    Assert.assertEquals(tagIds1.size(), cdtValues.size());

    latch.await();

    // unsubscribe
    tagService.unsubscribe(tagIds1, listener1);
    cdtValues = tagService.getSubscriptions(listener1);
    Assert.assertEquals(0, cdtValues.size());

    // check test success
    EasyMock.verify(requestHandlerMock, jmsProxyMock);
  }

  @Test @DirtiesContext
  public void testSubscribeToUnknownDataTag() throws Exception {
    final List<Boolean> check = new ArrayList<>();
    TagListener listener = new TagListener() {
      @Override
      public void onUpdate(Tag tagUpdate) {
        Assert.assertTrue("The Listener should never be called", false);
      }
      @Override
      public void onInitialUpdate(Collection<Tag> initialValues) {
        Assert.assertEquals(1, initialValues.size());
        for (Tag cdtValue : initialValues) {
          Assert.assertFalse(cdtValue.getDataTagQuality().isExistingTag());
          check.add(Boolean.TRUE);
        }
      }
    };

    Set<Long> tagId = new HashSet<>();
    tagId.add(1L);
    EasyMock.expect(requestHandlerMock.requestTags(tagId)).andReturn(new ArrayList<>(0));
    TagController tagController = new TagController(1L, true);
    EasyMock.replay(requestHandlerMock, jmsProxyMock);

    // run test
    tagService.subscribe(1L, listener);
    Thread.sleep(200);
    Assert.assertEquals(1, check.size());
    Assert.assertTrue(check.get(0));
    Assert.assertEquals(1, tagService.getSubscriptionIds(listener).size());

    // check test success
    EasyMock.verify(requestHandlerMock, jmsProxyMock);
  }

  @Test @DirtiesContext
  public void testGetUnknownTag() throws Exception {
    // Test setup
    Collection<Long> tagId = new ArrayList<>();
    tagId.add(1L);
    EasyMock.expect(requestHandlerMock.requestTags(tagId)).andReturn(new ArrayList<>());
    EasyMock.replay(requestHandlerMock);

    // run test
    Tag unknownTag = tagService.get(1L);
    Assert.assertFalse(unknownTag.getDataTagQuality().isExistingTag());

    // check test success
    EasyMock.verify(requestHandlerMock);
  }

  @Test @DirtiesContext
  public void testGetUnknownTags() throws Exception {
    // Test setup
    Collection<Long> tagIds = new ArrayList<>();
    tagIds.add(1L);
    tagIds.add(2L);
    EasyMock.expect(requestHandlerMock.requestTags(tagIds)).andReturn(new ArrayList<TagUpdate>());
    EasyMock.replay(requestHandlerMock);

    // run test
    Collection<Tag> unknownTags = tagService.get(tagIds);
    Assert.assertTrue(unknownTags.size() == 2);
    for (Tag unknownTag : unknownTags) {
      Assert.assertFalse(unknownTag.getDataTagQuality().isExistingTag());
    }

    // check test success
    EasyMock.verify(requestHandlerMock);
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
            null,
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
