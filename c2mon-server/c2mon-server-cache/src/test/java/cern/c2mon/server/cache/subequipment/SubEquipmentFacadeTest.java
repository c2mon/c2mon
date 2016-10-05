package cern.c2mon.server.cache.subequipment;

import java.util.Collection;

import cern.c2mon.server.cache.AbstractCacheIntegrationTest;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

/**
 * @author Franz Ritter
 */
public class SubEquipmentFacadeTest extends AbstractCacheIntegrationTest {

  @Autowired
  SubEquipmentFacade subEquipmentFacade;

  @Autowired
  SubEquipmentCache subEquipmentCache;

  @Test
  public void checkLoadingOfDataTags(){
    Collection<Long> tagIds = subEquipmentFacade.getDataTagIds(250L);

    assertTrue(tagIds.size() == 2);
    assertTrue(tagIds.contains(200011L));
    assertTrue(tagIds.contains(200012L));
  }


}
