package cern.c2mon.client.history.playback;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistorySupervisionEvent;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.history.event.HistoryPlayerAdapter;
import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.history.dbaccess.HistoryProviderSimpleImpl;
import cern.c2mon.client.history.testUtil.UncaughtExceptionSetup;
import cern.c2mon.client.history.updates.HistorySupervisionEventImpl;
import cern.c2mon.client.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * This test is testing the {@link HistoryPlayerImpl} class, and checks that the
 * supervision events plays in the correct order relative to a history record.
 * 
 * 
 * @author vdeila
 */
public class HistoryPlayerImplTest2 {

  /** The TIMESPAN which will be used for playback */
  private static final Timespan TIMESPAN = 
    new Timespan(
        new GregorianCalendar(2011, 06, 05, 12, 00).getTime(),
        new GregorianCalendar(2011, 06, 07, 12, 00).getTime());
  
  /** The playback speed multiplier to use */
  private static final double PLAYBACK_SPEED_MULTIPLIER = 24 * 60 * 60 * 1000; // 24 hours per real second
  
  /** The class under test */
  private HistoryPlayerImpl historyPlayer;
  
  /** Counts down when the history is loaded */
  private CountDownLatch historyLoadedLatch;
  
  /** The timeout to wait for the history to load */
  private static final Long HISTORY_LOADING_TIMEOUT = 10000L;
  
  /** The timeout to wait for the playback */
  private static final Long HISTORY_PLAYBACK_TIMEOUT = 10000L; 
  
  @Before
  public void setUp() throws Exception {
    historyPlayer = new HistoryPlayerImpl();
    historyLoadedLatch = new CountDownLatch(1);
    historyPlayer.addHistoryPlayerListener(new HistoryPlayerAdapter() {
      @Override
      public void onHistoryIsFullyLoaded() {
        historyLoadedLatch.countDown();
      }
    });
    
    UncaughtExceptionSetup.setUpUncaughtException();
  }

  @After
  public void tearDown() throws Exception {
    UncaughtExceptionSetup.tearDownUncaughtException();
  }

  @Test
  public void testPlaybackSupervisionEvents() throws InterruptedException {
    final IMocksControl mockCtrl = EasyMock.createStrictControl();
    final TagUpdateListener tagUpdateListener = mockCtrl.createMock(TagUpdateListener.class);
    final SupervisionListener supervisionListener = mockCtrl.createMock(SupervisionListener.class);
    
    final List<HistoryTagValueUpdate> initialTagRecords = new ArrayList<HistoryTagValueUpdate>();
    final List<HistoryTagValueUpdate> tagRecords = new ArrayList<HistoryTagValueUpdate>();
    final List<HistorySupervisionEvent> initialSupervisionRecords = new ArrayList<HistorySupervisionEvent>();
    final List<HistorySupervisionEvent> supervisionRecords = new ArrayList<HistorySupervisionEvent>();
    
    //
    // Creates initial tag records
    //
    final HistoryTagValueUpdateImpl initialHistoryRecord = 
      new HistoryTagValueUpdateImpl(
          10000L, 
          new DataTagQualityImpl(), 
          Integer.valueOf(40000), 
          new Timestamp(TIMESPAN.getStart().getTime() - 2 * 60 * 60 * 1000), 
          new Timestamp(TIMESPAN.getStart().getTime() - 2 * 60 * 60 * 1000), 
          new Timestamp(TIMESPAN.getStart().getTime() - 2 * 60 * 60 * 1000), 
          null,
          "Test tag", 
          TagMode.OPERATIONAL);
    initialTagRecords.add(initialHistoryRecord);
    
    //
    // Creates tag records
    //
    final long historyRecordTime = (long) (TIMESPAN.getStart().getTime() + (TIMESPAN.getEnd().getTime() - TIMESPAN.getStart().getTime()) * 0.5);
    final HistoryTagValueUpdateImpl historyRecord = 
      new HistoryTagValueUpdateImpl(
          10000L, 
          new DataTagQualityImpl(), 
          Integer.valueOf(100000), // Random value
          new Timestamp(historyRecordTime), 
          new Timestamp(historyRecordTime), 
          new Timestamp(historyRecordTime), 
          null,
          "Test tag", 
          TagMode.OPERATIONAL);
    historyRecord.setDataType("Integer");
    tagRecords.add(historyRecord);
    
    //
    // Creates initial supervision record
    //
    final long initialSupervisionEventTime = (long) (TIMESPAN.getStart().getTime() - 1 * 60 * 60 * 1000);
    final HistorySupervisionEvent initialSupervisionEvent = 
      new HistorySupervisionEventImpl(
          SupervisionEntity.EQUIPMENT, 
          5400L, 
          SupervisionStatus.DOWN, 
          new Timestamp(initialSupervisionEventTime), 
          "Testing. Is down..");
    initialSupervisionRecords.add(initialSupervisionEvent);
    
    //
    // Creates supervision record
    //
    final long supervisionEventTime = (long) (TIMESPAN.getStart().getTime() + (TIMESPAN.getEnd().getTime() - TIMESPAN.getStart().getTime()) * 0.75);
    final HistorySupervisionEvent supervisionEvent = 
      new HistorySupervisionEventImpl(
          SupervisionEntity.EQUIPMENT, 
          5400L, 
          SupervisionStatus.DOWN, 
          new Timestamp(supervisionEventTime), 
          "Testing. Is down..");
    supervisionRecords.add(supervisionEvent);
    
    final HistoryProvider historyProvider = 
      new HistoryProviderSimpleImpl(
          initialTagRecords, 
          tagRecords,
          initialSupervisionRecords,
          supervisionRecords);
    
    mockCtrl.resetToNice();
    
    historyPlayer.configure(historyProvider, TIMESPAN);
    historyPlayer.activateHistoryPlayer();
    
    historyPlayer.registerTagUpdateListener(
        tagUpdateListener, 
        historyRecord.getId(), 
        null);
    
    historyPlayer.registerSupervisionListener(
        supervisionEvent.getEntity(), 
        supervisionListener,
        Arrays.asList(supervisionEvent.getEntityId()));

    //
    // Records
    //
    mockCtrl.resetToStrict();
    mockCtrl.checkOrder(false);
    
    supervisionListener.onSupervisionUpdate(EasyMock.eq(initialSupervisionEvent));
    EasyMock.expectLastCall().atLeastOnce();
    tagUpdateListener.onUpdate(EasyMock.eq(initialHistoryRecord));
    EasyMock.expectLastCall().atLeastOnce();
    
    
    mockCtrl.checkOrder(true);
    
    tagUpdateListener.onUpdate(EasyMock.eq(historyRecord));
    supervisionListener.onSupervisionUpdate(EasyMock.eq(supervisionEvent));
    
    //
    // Adding stubs for the initial invalidations
    //
    
    // Stub for the tagUpdateListener
    final TagUpdateListener tagUpdateListenerDelegate = new TagUpdateListener() {
      @Override
      public void onUpdate(final TagValueUpdate tagValueUpdate) {
        if (tagValueUpdate.getDataTagQuality().isInitialised()) {
          Assert.fail(String.format("Unexpected method call onUpdate(%s)", tagValueUpdate.toString()));
        }
      }
    };
    tagUpdateListener.onUpdate(EasyMock.<TagValueUpdate>anyObject());
    EasyMock.expectLastCall().andStubDelegateTo(tagUpdateListenerDelegate);
    
    // Stub for the supervisionListener
    final SupervisionListener supervisionListenerDelegate = new SupervisionListener() {
      @Override
      public void onSupervisionUpdate(final SupervisionEvent supervisionEvent) {
        if (supervisionEvent.getEventTime().compareTo(new Timestamp(10)) > 0) {
          Assert.fail(String.format("Unexpected method call onSupervisionUpdate(%s)", supervisionEvent.toString()));
        }
      }
    };
    supervisionListener.onSupervisionUpdate(
        EasyMock.and(
            EasyMock.not(EasyMock.eq(initialSupervisionEvent)), 
            EasyMock.<SupervisionEvent>anyObject()));
    EasyMock.expectLastCall().andStubDelegateTo(supervisionListenerDelegate);
    
    
    //
    // Replays
    //
    mockCtrl.replay();
    
    final CountDownLatch playbackStoppedLatch = new CountDownLatch(1);
    final CountDownLatch playbackStartedLatch = new CountDownLatch(1);
    
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          playbackStartedLatch.await(HISTORY_LOADING_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        while (historyPlayer.getPlaybackControl().isPlaying()) {
          try {
            playbackStoppedLatch.await(10, TimeUnit.MILLISECONDS);
          }
          catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
        playbackStoppedLatch.countDown();
      }
    }).start();
    
    historyPlayer.beginLoading();
    if (!historyLoadedLatch.await(HISTORY_LOADING_TIMEOUT, TimeUnit.MILLISECONDS)) {
      Assert.fail("The loading timed out.");
    }
    
    historyPlayer.getPlaybackControl().setPlaybackSpeed(PLAYBACK_SPEED_MULTIPLIER);
    historyPlayer.getPlaybackControl().resume();
    playbackStartedLatch.countDown();
    
    if (!playbackStoppedLatch.await(HISTORY_PLAYBACK_TIMEOUT, TimeUnit.MILLISECONDS)) {
      historyPlayer.getPlaybackControl().pause();
      playbackStoppedLatch.countDown();
      Assert.fail("The playback timed out.");
    }
    
    // If the schedule slower than the clock
    Thread.sleep(500);
    
    mockCtrl.verify();
  }

}
