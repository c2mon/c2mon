package cern.c2mon.server.jcacheref;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Szymon Halastra
 */
public abstract class HazelcastBaseTestingSetup {

  @BeforeClass
  public static void init() {
    Config config = new ClasspathXmlConfig("hazelcast-test.xml");
    HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
  }

  @AfterClass
  public static void clean() {
    Hazelcast.shutdownAll();
  }

  public abstract void setup();
}
