package cern.c2mon.server.cache.dbaccess;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cachepersistence/config/server-cachepersistence-hsqldb-test.xml"})
public class HsqldbSchemaTest {

  @Test
  public void loadSchema() {
    
  }
  
}
