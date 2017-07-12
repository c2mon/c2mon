package cern.c2mon.server.jcacheref;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cern.c2mon.server.jcacheref.alarm.AlarmCacheTestSuite;
import cern.c2mon.server.jcacheref.alivetimer.AliveTimerCacheTestSuite;
import cern.c2mon.server.jcacheref.equipment.EquipmentCacheTestSuite;

/**
 * @author Szymon Halastra
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AlarmCacheTestSuite.class,
        AliveTimerCacheTestSuite.class,
        EquipmentCacheTestSuite.class
})
public class AllTestSuite {
}
