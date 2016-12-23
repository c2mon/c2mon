///******************************************************************************
// * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
// *
// * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
// * C2MON is free software: you can redistribute it and/or modify it under the
// * terms of the GNU Lesser General Public License as published by the Free
// * Software Foundation, either version 3 of the license.
// *
// * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// * more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
// *****************************************************************************/
//package cern.c2mon.server.elasticsearch.listener;
//
//import java.util.ArrayList;
//import java.util.Collections;
//
//import cern.c2mon.server.elasticsearch.tag.TagDocument;
//import cern.c2mon.server.elasticsearch.tag.TagListener;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import cern.c2mon.pmanager.persistence.IPersistenceManager;
//import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
//import cern.c2mon.server.cache.CacheRegistrationService;
//import cern.c2mon.server.common.datatag.DataTagCacheObject;
//import cern.c2mon.server.common.tag.Tag;
//import cern.c2mon.server.elasticsearch.tag.TagConverter;
//import cern.c2mon.server.test.CacheObjectCreation;
//
//import static org.mockito.Matchers.anyList;
//import static org.mockito.Matchers.eq;
//import static org.mockito.Mockito.*;
//
///**
// * Tests the TagLogCacheListener's utility methods.
// *
// * @author Alban Marguet
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = TagListenerTest.TestConfiguration.class)
//public class TagListenerTest {
//
//  @Configuration
//  public static class TestConfiguration {
//    @Bean
//    public TagConverter elasticsearchTagConverter() {
//      return mock(TagConverter.class);
//    }
//
//    @Bean
//    public CacheRegistrationService cacheRegistrationService() {
//      return mock(CacheRegistrationService.class);
//    }
//
//    @Bean
//    public IPersistenceManager<TagDocument> tagDocumentPersistenceManager() {
//      return mock(IPersistenceManager.class);
//    }
//
//    @Bean
//    public TagListener elasticsearchTagListener() {
//      return new TagListener(
//          elasticsearchTagConverter(),
//          cacheRegistrationService(),
//          tagDocumentPersistenceManager());
//    }
//
//  }
//
//  @Before
//  public void setUp() throws Exception {
//    reset(tagConverter,
//        cacheRegistrationService,
//        tagPersistenceManager);
//  }
//
//  @After
//  public void tearDown() throws Exception {
////    verifyNoMoreInteractions(tagConverter,
////        cacheRegistrationService,
////        tagNumericPersistenceManager,
////        tagStringPersistenceManager,
////        tagBooleanPersistenceManager);
//  }
//
//  private long id = 2L;
//  private boolean logged = true;
//  private boolean notLogged = false;
//
//  @Autowired
//  private TagConverter tagConverter;
//
//  @Autowired
//  private CacheRegistrationService cacheRegistrationService;
//
//
//  @Autowired
//  @Qualifier("tagDocumentPersistenceManager")
//  private IPersistenceManager<TagDocument> tagPersistenceManager;
//
//
//  @Autowired
//  private TagListener tagLogListener;
//
//  @Test
//  public void testTagIsLoggedToES() throws IDBPersistenceException {
//    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
//    tag.setLogged(logged);
//    tagLogListener.notifyElementUpdated(Collections.<Tag>singletonList(tag));
//
//    verify(tagConverter).convert(eq(tag));
//  }
//
//  @Test
//  public void testTagIsNotLoggedToES() throws IDBPersistenceException {
//    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
//    tag.setLogged(notLogged);
//    tagLogListener.notifyElementUpdated(Collections.<Tag>singletonList(tag));
//
//    verify(tagConverter, never()).convert(tag);
//  }
//
//  @Test
//  public void testNotifyElementUpdate() throws IDBPersistenceException {
//    ArrayList<Tag> list = new ArrayList<>();
//
//    DataTagCacheObject tag1 = CacheObjectCreation.createTestDataTag();
//    tag1.setLogged(logged);
//
//    DataTagCacheObject tag2 = CacheObjectCreation.createTestDataTag();
//    tag2.setLogged(logged);
//    tag2.setId(id);
//
//    DataTagCacheObject tag3 = CacheObjectCreation.createTestDataTag();
//    tag3.setLogged(notLogged);
//    list.add(tag1);
//    list.add(tag2);
//    list.add(tag3);
//
//    TagDocument convertedTag1 = new TagDocument(1L, "Boolean");
//    convertedTag1.setValueBoolean((Boolean) tag1.getValue());
//
//    TagDocument convertedTag2 = new TagDocument(2L, Boolean.class.getName());
//    convertedTag2.setValueBoolean((Boolean) tag2.getValue());
//
//    when(tagConverter.convert(tag1)).thenReturn(convertedTag1);
//    when(tagConverter.convert(tag2)).thenReturn(convertedTag2);
//
//    tagLogListener.notifyElementUpdated(list);
//
//    verify(tagConverter).convert(eq(tag1));
//    verify(tagConverter).convert(eq(tag2));
//    verify(tagConverter, atMost(2)).convert(eq(tag3));
//
//    verify(tagPersistenceManager, times(1)).storeData(anyList());
//  }
//}
