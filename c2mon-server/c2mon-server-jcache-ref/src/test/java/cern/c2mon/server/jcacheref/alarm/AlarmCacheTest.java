package cern.c2mon.server.jcacheref.alarm;

import javax.cache.Cache;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.jcacheref.IgniteBaseTestingSetup;

import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */

public class AlarmCacheTest {

  Cache<Long, Alarm> alarmTagCache;

  @Before
  public void setup() {

  }
}
