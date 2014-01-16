package cern.c2mon.server.cache.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.shared.daq.command.CommandTag;

/**
 * Integration test of the CommandTagCache with the cache loading
 * and cache DB access modules.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cache/config/server-cache-command-test.xml" })
public class CommandTagCacheTest {

  @Autowired  
  private CommandTagMapper commandTagMapper;
  
  @Autowired
  private CommandTagCacheImpl commandTagCache;
  
  @Test
  public void testCacheLoading() {
    assertNotNull(commandTagCache);
    
    List<CommandTag> commandList = commandTagMapper.getAll(); 
    
    //test the cache is the same size as in DB
    assertEquals(commandList.size(), commandTagCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<CommandTag> it = commandList.iterator();
    while (it.hasNext()) {
      CommandTag currentCommandTag = it.next();
      CacheObjectComparison.equals((CommandTagCacheObject) currentCommandTag, 
                                      (CommandTagCacheObject) commandTagCache.get(currentCommandTag.getId()));
    }
  }
  
}
