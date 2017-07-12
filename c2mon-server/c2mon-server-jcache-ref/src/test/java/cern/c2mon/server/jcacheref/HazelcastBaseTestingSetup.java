package cern.c2mon.server.jcacheref;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import cern.c2mon.server.common.equipment.Equipment;

/**
 * @author Szymon Halastra
 */

public abstract class HazelcastBaseTestingSetup {

  @BeforeClass
  public static void init() {
    initializeLogger();
    Config config = new ClasspathXmlConfig("hazelcast-test.xml");
    HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
  }

  @AfterClass
  public static void clean() {
    Hazelcast.shutdownAll();
  }

  public abstract void setup();

  private static void initializeLogger() {
    Logger.getLogger("").setLevel(Level.SEVERE);
  }
}
