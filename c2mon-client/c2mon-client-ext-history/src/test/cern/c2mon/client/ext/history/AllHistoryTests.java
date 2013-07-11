package cern.c2mon.client.ext.history;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cern.c2mon.client.ext.history.data.filter.DailySnapshotSmartFilterTest;
import cern.c2mon.client.ext.history.dbaccess.AllDbAccessTests;
import cern.c2mon.client.ext.history.playback.HistoryPlayerImplTest;
import cern.c2mon.client.ext.history.playback.HistoryPlayerImplTest2;
import cern.c2mon.client.ext.history.playback.components.ClockTest;

/**
 * All the tests in the package and sub-packages
 * 
 * @author vdeila
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  AllDbAccessTests.class,
  HistoryPlayerImplTest.class,
  HistoryPlayerImplTest2.class,
  ClockTest.class,
  DailySnapshotSmartFilterTest.class
})
public class AllHistoryTests {
}
