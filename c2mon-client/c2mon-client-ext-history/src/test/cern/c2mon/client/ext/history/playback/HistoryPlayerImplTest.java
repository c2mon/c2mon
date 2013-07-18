/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.ext.history.playback;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.HistorySupervisionEvent;
import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.Timespan;
import cern.c2mon.client.ext.history.common.event.HistoryPlayerAdapter;
import cern.c2mon.client.ext.history.dbaccess.HistoryProviderSimpleImpl;
import cern.c2mon.client.ext.history.playback.schedule.HistoryScheduler;
import cern.c2mon.client.ext.history.testUtil.UncaughtExceptionSetup;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.DataTagQualityImpl;

/**
 * This test is testing the {@link HistoryPlayerImpl} class, and checks that the
 * data is played in the correct order.
 * 
 * 
 * @author vdeila
 */
public class HistoryPlayerImplTest {

  /*
   * Static test variables 
   */
  
  /** The TIMESPAN which will be used for playback */
  private static final Timespan TIMESPAN = 
    new Timespan(
        new GregorianCalendar(2011, 06, 05, 12, 00).getTime(),
        new GregorianCalendar(2011, 06, 07, 12, 00).getTime());
  
  /** The number of tags which will be generated */
  private static final int NUMBER_OF_TAGS = 10;
  
  /**
   * The maximum number of milliseconds to wait for the loading to finish
   */
  private static final Long LOADING_TIMEOUT = 15000L;
  
  /** The maximum number of milliseconds to wait for the playback to finish */
  private static final Long PLAYBACK_TIMEOUT = 40000L;
  
  /** The multiplier to have for the playback speed (multiplied by real time) */
  private static final double PLAYBACK_SPEED_MULTIPLIER = 60 * 60 * 6; // playing 6 hours per second
  
  /** The multiplier to have for the playback speed the second time when there are no listeners */
  private static final double PLAYBACK_SPEED_MULTIPLIER_SECOND = 60 * 60 * 24 * 4; // playing 4 days per second
  
  /*
   * Components
   */
  
  /** The class under test */
  private HistoryPlayerImpl historyPlayer;
  
  /** All initial records */
  private List<HistoryTagValueUpdate> initialRecords;
  
  /** All history records */
  private List<HistoryTagValueUpdate> historyRecords;
  
  /** The "real time" tags which is already subscribed to when starting the history player */ 
  private Map<Long, TagUpdateListener> tagUpdateListenersMock;
  
  /** Is true when the history have been loaded */
  private AtomicBoolean historyLoaded;
  
  /** Control to check the order of the calls on the mocks */
  private IMocksControl mockCtrl;
  
  @Before
  public void setUp() throws Exception {
    mockCtrl = EasyMock.createStrictControl();
    historyLoaded = new AtomicBoolean(false);
    initialRecords = new ArrayList<HistoryTagValueUpdate>();
    historyRecords = new ArrayList<HistoryTagValueUpdate>();
    tagUpdateListenersMock = new HashMap<Long, TagUpdateListener>();
    
    // Generates initial records and history records
    
    for (int i = 0; i < NUMBER_OF_TAGS; i++) {
      final long tagId = 10000 + i;
      final TagUpdateListener tagUpdateListenerMock = mockCtrl.createMock(TagUpdateListener.class);

      // Adds it to the subscribed tags
      tagUpdateListenersMock.put(tagId, tagUpdateListenerMock);
      
      // Creates a TagValueUpdate record 
      final HistoryTagValueUpdateImpl initialHistoryRecord = 
        new HistoryTagValueUpdateImpl(
            tagId, 
            new DataTagQualityImpl(), 
            Integer.valueOf(i+40000), 
            new Timestamp(TIMESPAN.getStart().getTime() - 1 * 60 * 60 * 1000), 
            new Timestamp(TIMESPAN.getStart().getTime() - 1 * 60 * 60 * 1000), 
            new Timestamp(TIMESPAN.getStart().getTime() - 1 * 60 * 60 * 1000), 
            null,
            "Test tag", 
            TagMode.OPERATIONAL);
      initialHistoryRecord.setDataType("Integer");
      
      // Adds the initial record to the list of initialization records
      initialRecords.add(initialHistoryRecord);
      
      // Generating history records
      final Random random = new Random(i);
      long currentTime = TIMESPAN.getStart().getTime();
      
      while (true) {
        // Adds a time periode of between one minute and one day.
        currentTime += 60*1000 + (long)(random.nextDouble() * 60.0 * 60.0 * 1000.0);
        if (currentTime > TIMESPAN.getEnd().getTime()) {
          break;
        }
        
        // Creates a TagValueUpdate record 
        final HistoryTagValueUpdateImpl historyRecord = 
          new HistoryTagValueUpdateImpl(
              tagId, 
              new DataTagQualityImpl(), 
              Integer.valueOf((int) (currentTime % 100000)), // Random value
              new Timestamp(currentTime), 
              new Timestamp(currentTime), 
              new Timestamp(currentTime),
              null,
              "Test tag", 
              TagMode.OPERATIONAL);
        historyRecord.setDataType("Integer");
        
        // Adds it to the list of records
        historyRecords.add(historyRecord);
      }
    }
    
    historyPlayer = new HistoryPlayerImpl();
    
    historyPlayer.addHistoryPlayerListener(new HistoryPlayerAdapter() {
      @Override
      public void onHistoryIsFullyLoaded() {
        historyLoaded.set(true);
      }
    });
    
    UncaughtExceptionSetup.setUpUncaughtException();
  }

  @After
  public void tearDown() throws Exception {
    UncaughtExceptionSetup.tearDownUncaughtException();
    
  }

  /** Waits for the history to finish loading */
  private void waitForHistoryToLoad() {
    historyLoaded.set(historyPlayer.getHistoryLoadedUntil().compareTo(historyPlayer.getEnd()) >= 0);
    final Long timeout = System.currentTimeMillis() + LOADING_TIMEOUT;
    while (!historyLoaded.get()
        && System.currentTimeMillis() < timeout) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) { }
    }
    if (historyPlayer.getHistoryLoadedUntil().compareTo(historyPlayer.getEnd()) < 0) {
      Assert.fail("The loading of history have timed out.");
    }
  }
  
  /** Wait for the history player to finish playing */
  private void waitForPlaybackToFinish() {
    final Long playbackTimeout = System.currentTimeMillis() + PLAYBACK_TIMEOUT;
    while (historyPlayer.getPlaybackControl().isPlaying()
        && playbackTimeout > System.currentTimeMillis()) {
      try {
        Thread.sleep(100);
      }
      catch (InterruptedException e) { }
    }
    if (historyPlayer.getPlaybackControl().isPlaying()) {
      Assert.fail(String.format("The playback timed out. (Current playback time: %s)", new Timestamp(historyPlayer.getPlaybackControl().getClockTime())));
    }
    else {
      try {
        // The clock can be x milliseconds behind schedule, plus any time used in an update
        Thread.sleep(HistoryScheduler.BEHIND_SCHEDULE_THRESHOLD + 1000);
      }
      catch (InterruptedException e) { }
    }
  }
  
  @Test
  public void testPlayback() {
    
    final HistoryProvider historyProvider = 
      new HistoryProviderSimpleImpl(
        initialRecords, 
        historyRecords,
        new ArrayList<HistorySupervisionEvent>(),
        new ArrayList<HistorySupervisionEvent>());
    
    // Sort the records to the correct order
    final TagValueUpdate[] orderedRecords = historyRecords.toArray(new TagValueUpdate[0]);
    Arrays.sort(orderedRecords, new Comparator<TagValueUpdate>() {
      @Override
      public int compare(final TagValueUpdate value1, final TagValueUpdate value2) {
        int result = value1.getServerTimestamp().compareTo(value2.getServerTimestamp());
        if (result == 0 && value1 != value2) {
          Assert.fail("Two values have the same timestamp, the order is unknown and the test cannot proceed");
        }
        return result;
      }
    });
    
    //
    // Lets the history player load the data before starting the test
    // 
    mockCtrl.resetToNice();
    
    historyPlayer.configure(historyProvider, TIMESPAN);
    historyPlayer.activateHistoryPlayer();
    
    // Registers update listeners
    for (final Long tagId : tagUpdateListenersMock.keySet()) {
      final TagUpdateListener listenerMock = tagUpdateListenersMock.get(tagId);
      historyPlayer.registerTagUpdateListener(listenerMock, tagId, null);
    }
    
    //
    // Record
    //
    
    mockCtrl.resetToStrict();
    
    // Unordered calls, the initial history
    mockCtrl.checkOrder(false);
    
    for (final TagValueUpdate update : initialRecords) {

      EasyMock.expect(tagUpdateListenersMock.get(update.getId())
          .onUpdate(EasyMock.eq(update))).andReturn(true);
      EasyMock.expectLastCall().atLeastOnce();
    }
    
    // Ordered calls, the history
    mockCtrl.checkOrder(true);
    
    for (final TagValueUpdate update : orderedRecords) {
      
      EasyMock.expect(tagUpdateListenersMock.get(update.getId())
          .onUpdate(EasyMock.eq(update))).andReturn(true);
      EasyMock.expectLastCall().once();
    }
    
    // Stubs
    
    final TagUpdateListener tagUpdateListenerDelegate = new TagUpdateListener() {
      @Override
      public boolean onUpdate(final TagValueUpdate tagValueUpdate) {
        if (tagValueUpdate.getDataTagQuality().isInitialised()) {
          Assert.fail(String.format("Unexpected method call onUpdate(%s)", tagValueUpdate.toString()));
        }
        return true;
      }
    };
    
    for (final TagUpdateListener listener : tagUpdateListenersMock.values()) {
            listener.onUpdate(EasyMock.<TagValueUpdate>anyObject());
      EasyMock.expectLastCall().andStubDelegateTo(tagUpdateListenerDelegate);
    }
    
    //
    // Replay
    //
    
    mockCtrl.replay();
    
    // Loads the data
    historyPlayer.beginLoading();
    waitForHistoryToLoad();
    
    // Plays back the data
    historyPlayer.getPlaybackControl().resume();
    historyPlayer.getPlaybackControl().setPlaybackSpeed(PLAYBACK_SPEED_MULTIPLIER);
    
    // Waiting for the playback to finish
    waitForPlaybackToFinish();
    
    historyPlayer.unregisterTags(this.tagUpdateListenersMock.keySet());
    
    historyPlayer.getPlaybackControl().setClockTime(historyPlayer.getStart().getTime());
    historyPlayer.getPlaybackControl().resume();
    historyPlayer.getPlaybackControl().setPlaybackSpeed(PLAYBACK_SPEED_MULTIPLIER_SECOND);
    
    // Waiting for the playback to finish
    waitForPlaybackToFinish();
    
    historyPlayer.deactivateHistoryPlayer();
    
    // Verifies
    mockCtrl.verify();
  }
  
  @Test
  public void testClockSynchronization() throws InterruptedException {
    // This tests if the clock synchronization works correctly
    
    final double playbackTime = 
          ((TIMESPAN.getEnd().getTime() - TIMESPAN.getStart().getTime()) 
            / (double) PLAYBACK_SPEED_MULTIPLIER);
    
    // Make the playback use at least the double amount of time than the actual
    // playback speed would use
    final double timeToDelayPerUpdate = 
      (2.0 * (playbackTime / (double) (initialRecords.size() + historyRecords.size())));
    
    // Splits the time into milli seconds and nano seconds
    final long millisecondsToDelay = (long) timeToDelayPerUpdate;
    final int nanosecondsToDelay = 1 + (int) ((timeToDelayPerUpdate - millisecondsToDelay) * 1000000.0);
    
    final TagUpdateListener tagUpdateListener = new TagUpdateListener() {
      @Override
      public boolean onUpdate(final TagValueUpdate update) {
        try {
          Thread.sleep(millisecondsToDelay, nanosecondsToDelay);
        }
        catch (InterruptedException e) { }

        return true;
      }
    };
    
    final HistoryProvider historyProvider = 
      new HistoryProviderSimpleImpl(
          initialRecords, 
          historyRecords,
          new ArrayList<HistorySupervisionEvent>(),
          new ArrayList<HistorySupervisionEvent>());

    //
    // Configuring the history player, and registering the mock listeners
    // 
    mockCtrl.resetToNice();
    
    historyPlayer.configure(historyProvider, TIMESPAN);
    historyPlayer.activateHistoryPlayer();
    
    // Registers update listeners
    for (final Long tagId : tagUpdateListenersMock.keySet()) {
      final TagUpdateListener listenerMock = tagUpdateListenersMock.get(tagId);
      historyPlayer.registerTagUpdateListener(listenerMock, tagId, null);
    }
    
    
    //
    // Record
    //
    
    mockCtrl.resetToDefault();
    
    // Unordered calls
    mockCtrl.checkOrder(false);
    
    // Makes expectations about how many calls each listener will have,
    // based on how many records they have.
    for (final Long tagId : tagUpdateListenersMock.keySet()) {
      final TagUpdateListener listenerMock = tagUpdateListenersMock.get(tagId);
      final int numberOfRecords = historyProvider.getHistory(new Long[] { tagId }, TIMESPAN.getStart(), TIMESPAN.getEnd()).size();
      
      // At least the number of records, no maximum value
      listenerMock.onUpdate(EasyMock.<TagValueUpdate>anyObject());
      EasyMock.expectLastCall().andDelegateTo(tagUpdateListener)
        .times(numberOfRecords, Integer.MAX_VALUE);
    }
    
    
    //
    // Replay
    //
    
    mockCtrl.replay();
    
    historyPlayer.beginLoading();
    waitForHistoryToLoad();
    
    // Plays back the data
    historyPlayer.getPlaybackControl().setPlaybackSpeed(PLAYBACK_SPEED_MULTIPLIER);
    historyPlayer.getPlaybackControl().resume();
    
    waitForPlaybackToFinish();
    
    // Verifies that all updates have come.
    // If some updates have not come the clock have not been synchronized and the test will fail. 
    mockCtrl.verify();
  }
}
