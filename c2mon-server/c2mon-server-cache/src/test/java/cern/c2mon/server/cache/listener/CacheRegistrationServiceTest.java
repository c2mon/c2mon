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
package cern.c2mon.server.cache.listener;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.test.CacheObjectCreation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.Timestamp;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Integration test that checks that registered listeners
 * are indeed notified of the cache updates (the cache module
 * is started without mocks).
 *
 * <p>Does the check both for single and multi-threaded
 * registrations.
 *
 * <p>Currently only check for Tag caches (the only ones updated
 * in the server so far).
 *
 * @author Mark Brightwell
 *
 */
public class CacheRegistrationServiceTest extends AbstractCacheIntegrationTest {

  @Resource
  private CacheRegistrationService cacheRegistrationService;

  @Resource
  private DataTagCache dataTagCache;

  @Resource
  private ControlTagCache controlTagCache;

  @Resource
  private ControlTagFacade controlTagFacade;

  @Resource
  private DataTagFacade dataTagFacade;

  private DataTag dataTag;

  private ControlTag controlTag;

  @Before
  public void insertTestTag() throws IOException {
    dataTag = CacheObjectCreation.createTestDataTag();
    controlTag = CacheObjectCreation.createTestProcessAlive();

    //for this test put in cache
    dataTagCache.putQuiet(dataTag);
    controlTagCache.putQuiet(controlTag);
  }

  /**
   * Tests that an update in the Datatag, Controltag or RuleTag cache (TODO ruletag cache)
   * is picked up by a registered listener (asynchronous listener using ThreadHandler class).
   * This tag is present in the cache for this test.
   * TODO not working correctly at the moment: assertion fails but says tautology is the reason!
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws CloneNotSupportedException
   */
  //@Test //remove for now - using test below instead
  public void testRegisterToAllTags() throws SecurityException, NoSuchMethodException, CloneNotSupportedException {

    //check equals() method is working as expected on the datatag
    //(since a cloned datatag is passed to the listener module)
//    assertTrue(dataTag.clone().equals(dataTag));
    
    //set up mock cache listener    
    //change this on refactoring with generics... check works ok (abstract to interface)
//    C2monCacheListener<Tag> mockCacheListener = createMock(C2monCacheListener.class, C2monCacheListener.class.getMethod("notifyElementUpdated", new Class<?>[] {Object.class}));
//    Lifecycle listenerContainer = cacheRegistrationService.registerToAllTags(mockCacheListener, 1);
//    listenerContainer.start(); //does nothing
//    assertNotNull(listenerContainer);
           
    //record expected call on the listener when an update is made
//    mockCacheListener.notifyElementUpdated(dataTag); //equality of DataTagCacheObject is defined as equality of id's, cloned values are equal...
    
    //check behaviour is as expected
//    replay(mockCacheListener);
//    dataTagFacade.updateAndValidate(dataTag.getId(), Boolean.FALSE, "listener test value", new Timestamp(System.currentTimeMillis()));
//    dataTagCache.notifyListenersOfUpdate(dataTag);
//    listenerContainer.stop();
//    verify(mockCacheListener);
  }

  /**
   * New test of listener registration without using mocks (above test fails for some reason).
   *
   * <p>Registers on single thread.
   *
   * Removed as fails if run together with next test (reason unknown...).
   *
   * @throws InterruptedException if wait is interrupted
   */
  @Test
  @DirtiesContext
  public void testRegisterToAllTagsOutput() throws InterruptedException {
    commonRegisterTest(1);
  }

  /**
   * Same as testRegisterToAllTagsOutput() but on multiple threads (3).
   *
   * @throws InterruptedException
   */
  @Test
  @DirtiesContext
  public void testRegisterOnMultipleThreads() throws InterruptedException {
    commonRegisterTest(3);
  }

  private void commonRegisterTest(int threads) throws InterruptedException {
  //set up listener
    OutputTestListener testListener = new OutputTestListener();
    Lifecycle listenerContainer = cacheRegistrationService.registerToAllTags(testListener, threads);
    listenerContainer.start(); //does nothing
    assertNotNull(listenerContainer);

    //update tag in cache and notify listeners
    dataTagFacade.updateAndValidate(dataTag.getId(), Boolean.FALSE, "listener test value", new Timestamp(System.currentTimeMillis()));

    //sleep to allow separate thread to process

    Thread.sleep(200);

    DataTag cacheCopy = dataTagCache.getCopy(dataTag.getId());
    //check listener received the value
    assertNotNull(testListener.receivedValue);
    assertNotNull(testListener.receivedId);
    assertEquals(cacheCopy.getValue(), testListener.receivedValue);
    assertEquals(cacheCopy.getValue(), Boolean.FALSE); //check is indeed false
    assertEquals(cacheCopy.getId(), testListener.receivedId);

    //do the same for control tags
    controlTagFacade.updateAndValidate(controlTag.getId(), Long.valueOf(100L), "listener test control tag value", new Timestamp(System.currentTimeMillis()));
    ControlTag controlTagFromCache = controlTagCache.getCopy(controlTag.getId());
    //sleep to allow separate thread to process

    Thread.sleep(100);

    //check listener notified
    assertEquals(controlTagFromCache.getValue(), testListener.receivedValue);
    assertEquals(controlTagFromCache.getValue(), Long.valueOf(100)); //check is indeed 100
    assertEquals(controlTag.getId(), testListener.receivedId);
    listenerContainer.stop();

  }

  public class OutputTestListener implements C2monCacheListener<Tag> {

    public Object receivedId;

    public Object receivedValue;

    @Override
    public void notifyElementUpdated(Tag tag) {
      receivedValue = tag.getValue();
      receivedId = tag.getId();
    }

    @Override
    public void confirmStatus(Tag cacheable) {}
  }
}
