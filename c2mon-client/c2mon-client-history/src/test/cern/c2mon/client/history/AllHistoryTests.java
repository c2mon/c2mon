package cern.c2mon.client.history;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cern.c2mon.client.history.dbaccess.AllDbAccessTests;
import cern.c2mon.client.history.playback.HistoryPlayerImplTest;
import cern.c2mon.client.history.playback.components.ClockTest;

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
  ClockTest.class
})
public class AllHistoryTests {
}
