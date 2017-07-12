package cern.c2mon.server.jcacheref.alivetimer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Szymon Halastra
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AliveTimerManagerTest.class,
        AliveTimerCacheServiceTest.class
})
public class AliveTimerCacheTestSuite {
}
