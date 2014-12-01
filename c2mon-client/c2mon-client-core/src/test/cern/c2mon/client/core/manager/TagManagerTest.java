package cern.c2mon.client.core.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.jms.JMSException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monTagManager;
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
  private C2monTagManager tagManager;

  @Autowired
  private RequestHandler requestHandlerMock;

  @Autowired
  private JmsProxy jmsProxyMock;

  @Test
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
        System.out.println("1: counting down " + latch1.getCount());
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
        System.out.println("2: counting down " + latch2.getCount());
        latch2.countDown();
      }
    };
    prepareSubscribeDataTagsMock(tagIds2, listener1);

    // run test
    EasyMock.replay(requestHandlerMock, jmsProxyMock);
    Assert.assertTrue(tagManager.subscribeDataTags(tagIds1, listener1));
    Collection<ClientDataTagValue> cdtValues = tagManager.getAllSubscribedDataTags(listener1);
    Assert.assertEquals(tagIds1.size(), cdtValues.size());

    // Wait for onUpdate() to be called for all tags
    latch1.await();

    // second call for second listener
    Assert.assertTrue(tagManager.subscribeDataTags(tagIds2, listener2));
    cdtValues = tagManager.getAllSubscribedDataTags(listener2);
    Assert.assertEquals(tagIds2.size(), cdtValues.size());

    // Wait for onUpdate() to be called for all tags
    latch2.await();

    Thread.sleep(1000);

    // check test success
    EasyMock.verify(requestHandlerMock, jmsProxyMock);
  }

  @Test
  public void testUnsubscribeDataTags() throws JMSException {
    // Test setup
    Set<Long> tagIds1 = new HashSet<Long>();
    for (long i = 1; i <= 1000; i++) {
      tagIds1.add(i);
    }
    DataTagUpdateListener listener1 = EasyMock.createMock(DataTagUpdateListener.class);

    // run test
    Assert.assertTrue(tagManager.subscribeDataTags(tagIds1, listener1));
    Collection<ClientDataTagValue> cdtValues = tagManager.getAllSubscribedDataTags(listener1);
    Assert.assertEquals(tagIds1.size(), cdtValues.size());
    // unsubscribe
    tagManager.unsubscribeDataTags(tagIds1, listener1);
    cdtValues = tagManager.getAllSubscribedDataTags(listener1);
    Assert.assertEquals(0, cdtValues.size());
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
