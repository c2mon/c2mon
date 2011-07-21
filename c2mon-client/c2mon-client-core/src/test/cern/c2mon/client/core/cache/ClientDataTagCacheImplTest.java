package cern.c2mon.client.core.cache;

import static junit.framework.Assert.*;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jms.JMSException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.core.listener.DataTagUpdateListener;
import cern.c2mon.client.core.manager.CoreSupervisionManager;
import cern.c2mon.client.core.tag.ClientDataTag;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
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
  private ClientDataTagCache cache;
  
  private JmsProxy jmsProxyMock;
  
  private RequestHandler requestHandlerMock;
  
  private CoreSupervisionManager supervisionManagerMock;
  
  /**
   * Creates JmsProxy with Mocks 
   */
  @Before
  public void setUp() {
    jmsProxyMock = EasyMock.createMock(JmsProxy.class);
    requestHandlerMock = EasyMock.createMock(RequestHandler.class);
    supervisionManagerMock = EasyMock.createMock(CoreSupervisionManager.class);
    
    cache = new ClientDataTagCacheImpl(jmsProxyMock, requestHandlerMock, supervisionManagerMock);
  }
  
  @Test
  public void testEmptyCache() {
    assertEquals(0, cache.getAllSubscribedDataTags().size());
  }
  
  @Test
  public void testCreateAndGet() throws Exception {
    ClientDataTag cdt1 = cache.create(1L);
    assertSame(cdt1, cache.get(1L));
    ClientDataTag cdt2 = cache.create(2L);
    assertSame(cdt2, cache.get(2L));
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
    for (Long tagId : tagIds) {
      prepareClientDataTagCreateMock(tagId);
    }
    DataTagUpdateListener listener = EasyMock.createMock(DataTagUpdateListener.class);
    
    // run test
    EasyMock.replay(jmsProxyMock, supervisionManagerMock);
    ClientDataTag cdt1 = cache.create(1L);
    cdt1.update(createValidTransferTag(1L));
    ClientDataTag cdt2 = cache.create(2L);
    cdt2.update(createValidTransferTag(2L));
    Collection<ClientDataTag> cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(0, cachedTags.size());
    cache.addDataTagUpdateListener(tagIds, listener);
    cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(2, cachedTags.size());
    for (ClientDataTag tag : cachedTags) {
      if (tag.getId() == 1L) {
        assertSame(cdt1, tag);
      }
      else {
        assertSame(cdt2, tag);
      }
    }
    
    // check test success
    EasyMock.verify(jmsProxyMock, supervisionManagerMock);
  }
  
  
  @Test
  public void testUnsubscribeAllDataTags() throws Exception {
    // test setup
    Set<Long> tagIds = new HashSet<Long>();
    tagIds.add(1L);
    tagIds.add(2L);
    for (Long tagId : tagIds) {
      ClientDataTag cdtMock = prepareClientDataTagCreateMock(tagId); 
      jmsProxyMock.unregisterUpdateListener(cdtMock);
      supervisionManagerMock.removeSupervisionListener(cdtMock);
    }
    DataTagUpdateListener listener1 = EasyMock.createMock(DataTagUpdateListener.class);
    DataTagUpdateListener listener2 = EasyMock.createMock(DataTagUpdateListener.class);
    
    // run test
    EasyMock.replay(jmsProxyMock, supervisionManagerMock);
    ClientDataTag cdt1 = cache.create(1L);
    cdt1.update(createValidTransferTag(1L));
    ClientDataTag cdt2 = cache.create(2L);
    cdt2.update(createValidTransferTag(2L));
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
    EasyMock.verify(jmsProxyMock, supervisionManagerMock);
  }
  
  
  @Test
  public void testContainsTag() throws Exception {
    ClientDataTag cdt1 = cache.create(1L);
    cdt1.update(createValidTransferTag(1L));
    ClientDataTag cdt2 = cache.create(2L);
    cdt2.update(createValidTransferTag(2L));
    assertTrue(cache.containsTag(cdt1.getId()));
    assertTrue(cache.containsTag(cdt2.getId()));
    assertFalse(cache.containsTag(23423L));
  }
  
  @Test
  public void testHistoryMode() throws Exception {
    // test setup
    Set<Long> tagIds = new HashSet<Long>();
    tagIds.add(1L);
    tagIds.add(2L);
    for (Long tagId : tagIds) {
      prepareClientDataTagCreateMock(tagId); 
    }
    DataTagUpdateListener listener = EasyMock.createMock(DataTagUpdateListener.class);
    
    // run test
    EasyMock.replay(jmsProxyMock, supervisionManagerMock);
    ClientDataTag cdt1 = cache.create(1L);
    cdt1.update(createValidTransferTag(1L));
    cache.setHistoryMode(true);
    ClientDataTag cdt2 = cache.create(2L);
    cdt2.update(createValidTransferTag(2L));
    assertTrue(cache.containsTag(cdt1.getId()));
    assertTrue(cache.containsTag(cdt2.getId()));
    assertFalse(cache.containsTag(23423L));
    
    assertTrue(cache.isHistoryModeEnabled());
    cache.addDataTagUpdateListener(tagIds, listener);
    Collection<ClientDataTag> cachedTags = cache.getAllSubscribedDataTags();
    assertEquals(2, cachedTags.size());
    
    // check test success
    EasyMock.verify(jmsProxyMock, supervisionManagerMock);
  }
  
  private ClientDataTag prepareClientDataTagCreateMock(final Long tagId) throws RuleFormatException, JMSException {
    ClientDataTag cdtMock = new ClientDataTagImpl(tagId);
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
