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
package cern.c2mon.server.eslog.listener;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.indexer.EsTagIndexer;
import cern.c2mon.server.eslog.structure.converter.EsTagLogConverter;
import cern.c2mon.server.test.CacheObjectCreation;

/**
 * Tests the TagLogCacheListener's utility methods.
 * @author Alban Marguet
 */

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class EsTagLogListenerTest {
  private long id = 2L;
  private boolean logged = true;
  private boolean notLogged = false;
  @InjectMocks
  private EsTagLogListener cacheListener;

  @Mock
  private EsTagLogConverter esLogConverter;

  @Mock
  private CacheRegistrationService cacheRegistrationService;

  @Mock
  private EsTagIndexer indexer;


  @Test
  public void testTagIsLoggedToES() throws IDBPersistenceException {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setLogged(logged);
    cacheListener.notifyElementUpdated(Collections.<Tag>singletonList(tag));

    verify(esLogConverter).convertToTagES(eq(tag));
  }

  @Test
  public void testTagIsNotLoggedToES() throws IDBPersistenceException {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    tag.setLogged(notLogged);
    cacheListener.notifyElementUpdated(Collections.<Tag>singletonList(tag));

    verify(esLogConverter, never()).convertToTagES(tag);
  }

  @Test
  public void testNotifyElementUpdate() throws IDBPersistenceException {
    ArrayList<Tag> list = new ArrayList<>();

    DataTagCacheObject tag1 = CacheObjectCreation.createTestDataTag();
    tag1.setLogged(logged);

    DataTagCacheObject tag2 = CacheObjectCreation.createTestDataTag();
    tag2.setLogged(logged);
    tag2.setId(id);

    DataTagCacheObject tag3 = CacheObjectCreation.createTestDataTag();
    tag3.setLogged(notLogged);
    list.add(tag1);
    list.add(tag2);
    list.add(tag3);

    cacheListener.notifyElementUpdated(list);
    verify(esLogConverter).convertToTagES(eq(tag1));
    verify(esLogConverter).convertToTagES(eq(tag2));
    verify(esLogConverter, atMost(2)).convertToTagES(eq(tag3));
  }
}