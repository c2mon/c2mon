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
package cern.c2mon.shared.daq.republisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jms.UncategorizedJmsException;


/**
 * Unit test of RepublisherImpl.
 *
 * @author Mark Brightwell
 *
 */
public class RepublisherImplTest {

  private IMocksControl control = EasyMock.createNiceControl();

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

    //re-publication fails
    publisher.publish(publishedObject);
    EasyMock.expectLastCall().andThrow(new UncategorizedJmsException(""));

    //second succeeds
    publisher.publish(publishedObject);

    control.replay();

    republisher.publicationFailed(publishedObject);

    Thread.sleep(500);

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

    //republication succeeds
    publisher.publish(publishedObject1);
    publisher.publish(publishedObject2);
    publisher.publish(publishedObject3);

    control.replay();

    republisher.publicationFailed(publishedObject1);
    republisher.publicationFailed(publishedObject2);
    republisher.publicationFailed(publishedObject3);

    Thread.sleep(500);

    assertEquals(3, republisher.getNumberFailedPublications()); //the manual publicationFailed calls
    assertEquals(0, republisher.getSizeUnpublishedList());

    control.verify();
  }

  @Test
  public void testMultipleExceptions() throws InterruptedException {
    republisher.start();

    Object publishedObject1 = new Object();
    CountDownLatch latch = new CountDownLatch(11);

    //republication succeeds
    publisher.publish(publishedObject1); // failure
    // should re-attempt publication every 100ms
    EasyMock.expectLastCall().andAnswer(() -> { latch.countDown(); throw new UncategorizedJmsException(""); }).times(10);

    publisher.publish(publishedObject1); // success
    EasyMock.expectLastCall().andAnswer(() -> { latch.countDown(); return null; });

    control.replay();

    republisher.publicationFailed(publishedObject1);

    //Thread.sleep(2000);
    latch.await();

    assertEquals(11, republisher.getNumberFailedPublications());
    assertEquals(0, republisher.getSizeUnpublishedList());

    control.verify();
  }

  @Test
  public void testMultiplePublicationsWithExceptions() throws InterruptedException {
    republisher.start();

    Object publishedObject1 = new Object();
    Object publishedObject2 = new Object();
    Object publishedObject3 = new Object();
    CountDownLatch latch = new CountDownLatch(18);

    //republication succeeds
    publisher.publish(publishedObject1); // failure
    // should re-attempt publication every 100ms
    EasyMock.expectLastCall().andAnswer(() -> { latch.countDown(); throw new UncategorizedJmsException(""); }).times(10);

    publisher.publish(publishedObject1); //success
    EasyMock.expectLastCall().andAnswer(() -> { latch.countDown(); return null; });

    publisher.publish(publishedObject2); //failure
    // should re-attempt publication every 100ms
    EasyMock.expectLastCall().andAnswer(() -> { latch.countDown(); throw new UncategorizedJmsException(""); }).times(5);

    publisher.publish(publishedObject2); //success
    EasyMock.expectLastCall().andAnswer(() -> { latch.countDown(); return null; });
    publisher.publish(publishedObject3); //success
    EasyMock.expectLastCall().andAnswer(() -> { latch.countDown(); return null; });

    control.replay();

    republisher.publicationFailed(publishedObject1);
    republisher.publicationFailed(publishedObject2);
    republisher.publicationFailed(publishedObject3);

    // Thread.sleep(2000);
    latch.await();

    assertEquals(11 + 6 + 1, republisher.getNumberFailedPublications());
    assertEquals(0, republisher.getSizeUnpublishedList());

    control.verify();
  }

  @Test
  public void testFailToPublish() throws InterruptedException {
    republisher.start();

    Object publishedObject1 = new Object();
    Object publishedObject2 = new Object();
    Object publishedObject3 = new Object();

    publisher.publish(publishedObject1); //failure
    EasyMock.expectLastCall().andThrow(new UncategorizedJmsException("")).anyTimes();
    publisher.publish(publishedObject2); //failure
    EasyMock.expectLastCall().andThrow(new UncategorizedJmsException("")).anyTimes();
    publisher.publish(publishedObject3); //failure
    EasyMock.expectLastCall().andThrow(new UncategorizedJmsException("")).anyTimes();

    control.replay();

    republisher.publicationFailed(publishedObject1);
    republisher.publicationFailed(publishedObject2);
    republisher.publicationFailed(publishedObject3);

    Thread.sleep(1000);

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

    //2 republications
    publisher.publish(publishedObject);
    publisher.publish(publishedObject);

    control.replay();

    republisher.publicationFailed(publishedObject);

    Thread.sleep(1000);

    //first re-publication has succeeded and task has been cancelled
    assertEquals(1, republisher.getNumberFailedPublications()); //the manual publicationFailed call
    assertEquals(0, republisher.getSizeUnpublishedList());

    //second failure starts new task
    republisher.publicationFailed(publishedObject);
    Thread.sleep(500);

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

    //re-publication fails
    publisher.publish(publishedObject);
    EasyMock.expectLastCall().andThrow(new RuntimeException());

    //second publication is not attempted as not JMS exception

    control.replay();

    republisher.publicationFailed(publishedObject);

    Thread.sleep(500);

    assertEquals(2, republisher.getNumberFailedPublications()); //manual call + exception in re-publication thread
    assertEquals(0, republisher.getSizeUnpublishedList()); //removed from list when exception occurs

    control.verify();
  }


}
