package cern.c2mon.client.core.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.jms.JMSException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.listener.DataTagListener;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/client/core/manager/c2mon-tagmanager-test.xml" })
public class TagManagerTest {

  /**
   * Component to test
   */
  @Autowired
  private TagManager tagManager;

  @Autowired
  private RequestHandler requestHandlerMock;

  @Autowired
  private JmsProxy jmsProxyMock;

  @Test @DirtiesContext
  public void testSubscribeDataTags() throws Exception {
    // Test setup
    Set<Long> tagIds1 = new HashSet<Long>();
    for (long i = 1; i <= 1000; i++) {
      tagIds1.add(i);
    }

    // Use a CountDownLatch as an update listener to allow the subscription
    // thread to finish
    final CountDownLatch latch1 = new CountDownLatch(tagIds1.size());
    DataTagUpdateListener listener1 = new DataTagUpdateListener() {
      @Override
      public void onUpdate(ClientDataTagValue tagUpdate) {
        latch1.countDown();
      }
    };
    prepareSubscribeDataTagsMock(tagIds1, listener1);

    // listener 2
    Set<Long> tagIds2 = new HashSet<Long>();
    for (long i = 1001; i <= 2000; i++) {
      tagIds2.add(i);
    }

    // Use another latch for the second listener
    final CountDownLatch latch2 = new CountDownLatch(tagIds2.size());
    DataTagUpdateListener listener2 = new DataTagUpdateListener() {
      @Override
      public void onUpdate(ClientDataTagValue tagUpdate) {
        latch2.countDown();
      }
    };
    prepareSubscribeDataTagsMock(tagIds2, listener2);
    EasyMock.replay(requestHandlerMock, jmsProxyMock);

    // run test
    tagManager.subscribeDataTags(tagIds1, listener1);
    Collection<ClientDataTagValue> cdtValues = tagManager.getAllSubscribedDataTags(listener1);
    Assert.assertEquals(tagIds1.size(), cdtValues.size());
    Assert.assertEquals(tagIds1.size(), tagManager.getAllSubscribedDataTagIds(listener1).size());

    // Wait for onUpdate() to be called for all tags
    latch1.await();

    // second call for second listener
    tagManager.subscribeDataTags(tagIds2, listener2);
    cdtValues = tagManager.getAllSubscribedDataTags(listener2);
    Assert.assertEquals(tagIds2.size(), cdtValues.size());
    Assert.assertEquals(tagIds2.size(), tagManager.getAllSubscribedDataTagIds(listener2).size());

    // Wait for onUpdate() to be called for all tags
    latch2.await();

    Thread.sleep(1000);

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
    DataTagListener listener1 = new DataTagListener() {
      @Override
      public void onUpdate(ClientDataTagValue tagUpdate) {
        latch1.countDown();
      }

      @Override
      public void onInitialUpdate(Collection<ClientDataTagValue> initialValues) {
        Assert.assertEquals(tagIds1.size(), initialValues.size());
        Assert.assertEquals(tagIds1.size(), latch1.getCount());
        check.add(Boolean.TRUE);
      }
    };
    prepareSubscribeDataTagsMock(tagIds1, listener1);

    // listener 2
    final Set<Long> tagIds2 = new HashSet<Long>();
    for (long i = 1001; i <= 2000; i++) {
      tagIds2.add(i);
    }

    final CountDownLatch latch2 = new CountDownLatch(tagIds2.size());
    DataTagListener listener2 = new DataTagListener() {
      @Override
      public void onUpdate(ClientDataTagValue tagUpdate) {
        latch2.countDown();
      }

      @Override
      public void onInitialUpdate(Collection<ClientDataTagValue> initialValues) {
        Assert.assertEquals(tagIds2.size(), initialValues.size());
        Assert.assertEquals(tagIds2.size(), latch2.getCount());
        check.add(Boolean.TRUE);
      }
    };
    prepareSubscribeDataTagsMock(tagIds2, listener2);
    EasyMock.replay(requestHandlerMock, jmsProxyMock);

    // run test
    tagManager.subscribeDataTags(tagIds1, listener1);
    Assert.assertEquals(tagIds1.size(), tagManager.getAllSubscribedDataTagIds(listener1).size());

    // second call for second listener
    tagManager.subscribeDataTags(tagIds2, listener2); 
    
    Assert.assertEquals(tagIds2.size(), tagManager.getAllSubscribedDataTagIds(listener2).size());
    Assert.assertEquals(2, check.size());
    
    Thread.sleep(1000);
    
    // check test success
    EasyMock.verify(requestHandlerMock, jmsProxyMock);
  }

  @Test @DirtiesContext
  public void testUnsubscribeDataTags() throws JMSException {
    // Test setup
    Set<Long> tagIds1 = new HashSet<Long>();
    for (long i = 1L; i <= 1000; i++) {
      tagIds1.add(i);
    }
    DataTagUpdateListener listener1 = EasyMock.createMock(DataTagUpdateListener.class);
    prepareSubscribeDataTagsMock(tagIds1, listener1);
    EasyMock.replay(requestHandlerMock, jmsProxyMock);
    
    // run test
    tagManager.subscribeDataTags(tagIds1, listener1);
    Collection<ClientDataTagValue> cdtValues = tagManager.getAllSubscribedDataTags(listener1);
    Assert.assertEquals(tagIds1.size(), cdtValues.size());
    // unsubscribe
    tagManager.unsubscribeDataTags(tagIds1, listener1);
    cdtValues = tagManager.getAllSubscribedDataTags(listener1);
    Assert.assertEquals(0, cdtValues.size());
    
    // check test success
    EasyMock.verify(requestHandlerMock, jmsProxyMock);
  }
  
  @Test @DirtiesContext
  public void testSubscribeToUnknownDataTag() throws JMSException, Exception{
    final List<Boolean> check = new ArrayList<>();
    DataTagListener listener = new DataTagListener() {
      @Override
      public void onUpdate(ClientDataTagValue tagUpdate) {
        Assert.assertTrue("The Listener should never be called", false);
      }
      @Override
      public void onInitialUpdate(Collection<ClientDataTagValue> initialValues) {
        Assert.assertEquals(1, initialValues.size());
        for (ClientDataTagValue cdtValue : initialValues) {
          Assert.assertFalse(cdtValue.getDataTagQuality().isExistingTag());
          check.add(Boolean.TRUE);
        }
      }
    };
    // Test setup
    Set<Long> tagId = new HashSet<Long>();
    tagId.add(1L);
    EasyMock.expect(requestHandlerMock.requestTags(tagId)).andReturn(new ArrayList<TagUpdate>(0));
    ClientDataTagImpl cdt = new ClientDataTagImpl(1L, true);
    EasyMock.expect(jmsProxyMock.isRegisteredListener(cdt)).andReturn(false);
    EasyMock.replay(requestHandlerMock, jmsProxyMock);
    
    // run test
    tagManager.subscribeDataTag(1L, listener);
    Thread.sleep(200);
    Assert.assertEquals(1, check.size());
    Assert.assertTrue(check.get(0));
    Assert.assertEquals(1, tagManager.getAllSubscribedDataTagIds(listener).size());
    
    
    // check test success
    EasyMock.verify(requestHandlerMock, jmsProxyMock);
  }


  /**
   * Prepares all EasyMock calls for doing a <code>tagManager.subscribeDataTags()</code> call
   * @param tagIds list of tag ids to subscribe to
   * @param listener the listener to be subscribed
   * @throws JMSException
   */
  private void prepareSubscribeDataTagsMock(final Set<Long> tagIds, final DataTagUpdateListener listener) throws JMSException {
    Collection<TagUpdate> serverUpdates = new ArrayList<TagUpdate>(tagIds.size());
    for (Long tagId : tagIds) {
      serverUpdates.add(createValidTransferTag(tagId));
    }

    Collection<TagValueUpdate> serverUpdateValues = new ArrayList<>();
    for (Long tagId : tagIds) {
      serverUpdateValues.add(createValidTransferTag(tagId));
    }


    EasyMock.expect(requestHandlerMock.requestTags(tagIds)).andReturn(serverUpdates);
    for (Long tagId : tagIds) {
      ClientDataTagImpl cdt = new ClientDataTagImpl(tagId);
      EasyMock.expect(jmsProxyMock.isRegisteredListener(cdt)).andReturn(false);
      jmsProxyMock.registerUpdateListener(cdt, cdt);
    }
    EasyMock.expect(requestHandlerMock.requestTagValues(tagIds)).andReturn(serverUpdateValues);
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

    return tagUpdate;
  }
}
