/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.core.service.impl;

import java.util.*;

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    elasticsearch = new ElasticsearchService(new C2monClientProperties(), "c2mon");
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
