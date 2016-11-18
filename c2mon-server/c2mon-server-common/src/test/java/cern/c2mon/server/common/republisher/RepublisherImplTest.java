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
package cern.c2mon.server.common.republisher;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jms.UncategorizedJmsException;

import cern.c2mon.server.common.republisher.Publisher;
import cern.c2mon.server.common.republisher.RepublisherImpl;

import java.util.concurrent.CountDownLatch;


/**
 * Unit test of RepublisherImpl.
 *
 * @author Mark Brightwell
 *
 */
public class RepublisherImplTest {

  private IMocksControl control = createNiceControl();

  //mocks
  private Publisher<Object> publisher;

  //class to test
  private RepublisherImpl<Object> republisher;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    control.reset();
    publisher = control.createMock(Publisher.class);
    republisher = new RepublisherImpl<Object>(publisher, "event-name");
    republisher.setRepublicationDelay(100);
  }

  @After
  public void afterTest() {
    republisher.stop();
  }

  @Test(expected=IllegalStateException.class)
  public void testNotStartedException() {
    republisher.publicationFailed(new Object());
  }

  /**
   * Test single republication.
   * @throws InterruptedException
   */
  @Test
  @Ignore("This test is flaky!")
  public void testSingleRepublication() throws InterruptedException {
    republisher.start();

    Object publishedObject = new Object();

    //republication succeeds
    publisher.publish(publishedObject);

    control.replay();

    republisher.publicationFailed(publishedObject);

    Thread.sleep(500);

    assertEquals(1, republisher.getNumberFailedPublications()); //the manual publicationFailed call
    assertEquals(0, republisher.getSizeUnpublishedList());

    control.verify();
  }

  /**
   * Test republication if publisher throws 1 exception.
   * @throws InterruptedException
   */
  @Test
  public void testSingleRepublicationAfterException() throws InterruptedException {
    republisher.start();

    Object publishedObject = new Object();
    CountDownLatch latch = new CountDownLatch(2);

    //re-publication fails
    publisher.publish(publishedObject);
    expectLastCall().andAnswer(() -> { latch.countDown(); throw new UncategorizedJmsException(""); });

    //second succeeds
    publisher.publish(publishedObject);
    expectLastCall().andAnswer(() -> { latch.countDown(); return null; });

    control.replay();

    republisher.publicationFailed(publishedObject);

    latch.await();

    assertEquals(2, republisher.getNumberFailedPublications()); //manual call + automatic publication
    assertEquals(0, republisher.getSizeUnpublishedList());

    control.verify();
  }

  @Test
  public void testMultipleRepublication() throws InterruptedException {
    republisher.start();

    Object publishedObject1 = new Object();
    Object publishedObject2 = new Object();
    Object publishedObject3 = new Object();
    CountDownLatch latch = new CountDownLatch(1);

    //republication succeeds
    publisher.publish(publishedObject1);
    publisher.publish(publishedObject2);
    publisher.publish(publishedObject3);
    expectLastCall().andAnswer(() -> { latch.countDown(); return null; });

    control.replay();

    republisher.publicationFailed(publishedObject1);
    republisher.publicationFailed(publishedObject2);
    republisher.publicationFailed(publishedObject3);

    latch.await();

    assertEquals(3, republisher.getNumberFailedPublications()); //the manual publicationFailed calls
    assertEquals(0, republisher.getSizeUnpublishedList());

    control.verify();
  }

  @Test
  public void testMultipleExceptions() throws InterruptedException {
    republisher.start();

    Object publishedObject1 = new Object();
    CountDownLatch latch = new CountDownLatch(4);

    //republication succeeds
    publisher.publish(publishedObject1); // failure
    // should re-attempt publication every 100ms
    expectLastCall().andAnswer(() -> { latch.countDown(); throw new UncategorizedJmsException(""); }).times(3);

    publisher.publish(publishedObject1); // success
    expectLastCall().andAnswer(() -> { latch.countDown(); return null; });

    control.replay();

    republisher.publicationFailed(publishedObject1);

    latch.await();

    assertEquals(4, republisher.getNumberFailedPublications());
    assertEquals(0, republisher.getSizeUnpublishedList());

    control.verify();
  }

  @Test
  public void testMultiplePublicationsWithExceptions() throws InterruptedException {
    republisher.start();

    Object publishedObject1 = new Object();
    Object publishedObject2 = new Object();
    Object publishedObject3 = new Object();
    CountDownLatch latch = new CountDownLatch(8);

    //republication succeeds
    publisher.publish(publishedObject1); // failure
    // should re-attempt publication every 100ms
    expectLastCall().andAnswer(() -> { latch.countDown(); throw new UncategorizedJmsException(""); }).times(3);

    publisher.publish(publishedObject1); //success
    expectLastCall().andAnswer(() -> { latch.countDown(); return null; });

    publisher.publish(publishedObject2); //failure
    // should re-attempt publication every 100ms
    expectLastCall().andAnswer(() -> { latch.countDown(); throw new UncategorizedJmsException(""); }).times(2);

    publisher.publish(publishedObject2); //success
    expectLastCall().andAnswer(() -> { latch.countDown(); return null; });
    publisher.publish(publishedObject3); //success
    expectLastCall().andAnswer(() -> { latch.countDown(); return null; });

    control.replay();

    republisher.publicationFailed(publishedObject1);
    republisher.publicationFailed(publishedObject2);
    republisher.publicationFailed(publishedObject3);

    // Thread.sleep(2000);
    latch.await();

    assertEquals(4 + 3 + 1, republisher.getNumberFailedPublications());
    assertEquals(0, republisher.getSizeUnpublishedList());

    control.verify();
  }

  @Test
  public void testFailToPublish() throws InterruptedException {
    republisher.start();

    Object publishedObject1 = new Object();
    Object publishedObject2 = new Object();
    Object publishedObject3 = new Object();
    CountDownLatch latch = new CountDownLatch(3);

    publisher.publish(publishedObject1); //failure
    expectLastCall().andAnswer(() -> { latch.countDown(); throw new UncategorizedJmsException(""); }).anyTimes();
    publisher.publish(publishedObject2); //failure
    expectLastCall().andAnswer(() -> { latch.countDown(); throw new UncategorizedJmsException(""); }).anyTimes();
    publisher.publish(publishedObject3); //failure
    expectLastCall().andAnswer(() -> { latch.countDown(); throw new UncategorizedJmsException(""); }).anyTimes();

    control.replay();

    republisher.publicationFailed(publishedObject1);
    republisher.publicationFailed(publishedObject2);
    republisher.publicationFailed(publishedObject3);

    latch.await();

    assertTrue(republisher.getNumberFailedPublications() > 0);
    assertEquals(3, republisher.getSizeUnpublishedList());

    control.verify();
  }

  /**
   * Test 2 re-publications in separate tasks.
   * @throws InterruptedException
   */
  @Test
  public void testSuccessiveRepublication() throws InterruptedException {
    republisher.start();

    Object publishedObject = new Object();
    CountDownLatch latch1 = new CountDownLatch(1);
    CountDownLatch latch2 = new CountDownLatch(1);

    //2 republications
    publisher.publish(publishedObject);
    expectLastCall().andAnswer(() -> { latch1.countDown(); return null; });
    publisher.publish(publishedObject);
    expectLastCall().andAnswer(() -> { latch2.countDown(); return null; });

    control.replay();

    republisher.publicationFailed(publishedObject);

    latch1.await();

    //first re-publication has succeeded and task has been cancelled
    assertEquals(1, republisher.getNumberFailedPublications()); //the manual publicationFailed call
    assertEquals(0, republisher.getSizeUnpublishedList());

    //second failure starts new task
    republisher.publicationFailed(publishedObject);
    latch2.await();

    //second re-publication has succeeded
    assertEquals(2, republisher.getNumberFailedPublications()); //successive failed attempts
    assertEquals(0, republisher.getSizeUnpublishedList());

    control.verify();
  }

  /**
   * If a non-JMS exception (spring runtime) is thrown, should result in no re-publication.
   * @throws InterruptedException
   */
  @Test
  public void testNonJmsException() throws InterruptedException {
    republisher.start();

    Object publishedObject = new Object();
    CountDownLatch latch = new CountDownLatch(1);

    //re-publication fails
    publisher.publish(publishedObject);
    expectLastCall().andAnswer(() -> { latch.countDown(); throw new RuntimeException(); });

    //second publication is not attempted as not JMS exception

    control.replay();

    republisher.publicationFailed(publishedObject);

    latch.await();
    Thread.sleep(100);

    assertEquals(2, republisher.getNumberFailedPublications()); //manual call + exception in re-publication thread
    assertEquals(0, republisher.getSizeUnpublishedList()); //removed from list when exception occurs

    control.verify();
  }


}
