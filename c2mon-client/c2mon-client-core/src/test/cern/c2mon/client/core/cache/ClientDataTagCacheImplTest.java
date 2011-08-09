package cern.c2mon.client.core.cache;

import static junit.framework.Assert.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jms.JMSException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.core.manager.CoreSupervisionManager;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.rule.RuleFormatException;

/**
 * Don't forget to add all required environment variables before you start your
 * test! The environment variable are specified in the <code>pom.xml</code>
 *
 * @author Matthias Braeger
 */
public class ClientDataTagCacheImplTest {  
  /**
   * Component to test
   */
  private ClientDataTagCacheImpl cache;
  
  private JmsProxy jmsProxyMock;
  
  private RequestHandler requestHandlerMock;
  
  private CoreSupervisionManager supervisionManagerMock;
  
  private CacheController controller;
  
  /**
   * Creates JmsProxy with Mocks 
   */
  @Before
  public void setUp() {
    jmsProxyMock = EasyMock.createMock(JmsProxy.class);
    requestHandlerMock = EasyMock.createMock(RequestHandler.class);
    supervisionManagerMock = EasyMock.createMock(CoreSupervisionManager.class);
    controller = new CacheControllerImpl();
    
    cache = new ClientDataTagCacheImpl(jmsProxyMock, requestHandlerMock, supervisionManagerMock, controller);
  }
  
  @Test
  public void testEmptyCache() {
    cache.init();
    assertEquals(0, cache.getAllSubscribedDataTags().size());
  }
  
  
  /**
   * Adds two tags into the cache and subscribes them to a <code>DataTagUpdateListener</code>.
   * @throws Exception
   */
  @Test
  public void testAddDataTagUpdateListener() throws Exception {
    // Test setup
    supervisionManagerMock.addConnectionListener(cache);
    supervisionManagerMock.addHeartbeatListener(cache);
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
    EasyMock.replay(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
    cache.init();
    Collection<ClientDataTag> cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(0, cachedTags.size());
    cache.addDataTagUpdateListener(tagIds, listener);
    
    
    cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(2, cachedTags.size());
    
    // check test success
    EasyMock.verify(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
  }
  
  
  @Test
  public void testUnsubscribeAllDataTags() throws Exception {
    // test setup
    supervisionManagerMock.addConnectionListener(cache);
    supervisionManagerMock.addHeartbeatListener(cache);
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
    EasyMock.replay(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
    cache.init();
    cache.addDataTagUpdateListener(tagIds, listener1);
    Collection<ClientDataTag> cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(2, cachedTags.size());
    // NOT Registered listener
    cache.unsubscribeAllDataTags(listener2);
    cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(2, cachedTags.size());
    // Registered listener
    cache.unsubscribeAllDataTags(listener1);
    cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(0, cachedTags.size());
    
    // check test success
    EasyMock.verify(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
  }
  
  
  @Test
  public void testContainsTag() throws Exception {
    // Test setup
    supervisionManagerMock.addConnectionListener(cache);
    supervisionManagerMock.addHeartbeatListener(cache);
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
    EasyMock.replay(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
    cache.init();
    cache.addDataTagUpdateListener(tagIds, listener);
    assertTrue(cache.containsTag(1L));
    assertTrue(cache.containsTag(2L));
    assertFalse(cache.containsTag(23423L));
    
    // check test success
    EasyMock.verify(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
  }
  
  @Test
  public void testHistoryMode() throws Exception {
    // Test setup
    supervisionManagerMock.addConnectionListener(cache);
    supervisionManagerMock.addHeartbeatListener(cache);
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
    EasyMock.replay(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
    cache.init();
    cache.addDataTagUpdateListener(tagIds, listener);
    cache.setHistoryMode(true);
    for (Long tagId : tagIds) {
      assertTrue(cache.containsTag(tagId));
    }
    assertFalse(cache.containsTag(23423L));
    
    assertTrue(cache.isHistoryModeEnabled());
    cache.addDataTagUpdateListener(tagIds, listener);
    Collection<ClientDataTag> cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(2, cachedTags.size());
    
    // check test success
    EasyMock.verify(jmsProxyMock, supervisionManagerMock, requestHandlerMock);
  }
  
  private ClientDataTagImpl prepareClientDataTagCreateMock(final Long tagId) throws RuleFormatException, JMSException {
    ClientDataTagImpl cdtMock = new ClientDataTagImpl(tagId);
    cdtMock.update(createValidTransferTag(tagId));
    supervisionManagerMock.addSupervisionListener(cdtMock, cdtMock.getProcessIds(), cdtMock.getEquipmentIds());
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
          tagQuality,
          TagMode.TEST,
          new Timestamp(System.currentTimeMillis() - 10000L),
          new Timestamp(System.currentTimeMillis()),
          "Test description",
          "My.data.tag.name",
          "My.jms.topic");
    
    return tagUpdate;
  }
}
