package cern.c2mon.client.core.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.cache.ClientDataTagCache;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.elasticsearch.ElasticsearchService;
import cern.c2mon.client.core.jms.RequestHandler;
import cern.c2mon.client.core.service.CoreSupervisionService;

/**
 * Test class for the TagServiceImpl
 * 
 * @author Ivan Prieto Barreiro
 */
public class TagServiceImplTest {
  
  private TagServiceImpl tagService;
  private ClientDataTagCache cache;
  private CoreSupervisionService supervisionService;
  private RequestHandler requestHandler;
  private ElasticsearchService elasticsearch;
  
  @Before
  public void init() {
    supervisionService = EasyMock.createMock(CoreSupervisionService.class);
    cache = EasyMock.createMock(ClientDataTagCache.class);
    requestHandler = EasyMock.createMock(RequestHandler.class);
    elasticsearch = new ElasticsearchService(new C2monClientProperties());
    tagService = new TagServiceImpl(supervisionService, cache, requestHandler, elasticsearch);
  }
  
  @Test
  public void findByNameTest() {
    String tagToSearch = "TEST";
    Collection<String> tagsToSearch = Arrays.asList(tagToSearch);
    Set<String> tagSet = new HashSet<>(tagsToSearch);
    EasyMock.expect(cache.getByNames(tagSet)).andReturn(new HashMap<>());
    PowerMock.replay(cache);
    
    Collection<Tag> tags = tagService.findByName("TEST");
    assertNotNull("The tags collection should not be null", tags);
    assertTrue("The tags collection should be empty", tags.isEmpty());
  }
}
