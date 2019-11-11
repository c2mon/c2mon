package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.actions.AbstractCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class DataTagService extends AbstractCacheService<DataTag> {

  @Inject
  public DataTagService(C2monCache<DataTag> cache) {
    super(cache, new DefaultC2monCacheFlow<>());
  }

  /**
   * Returns a collection of the ids of all DataTags
   * registered with this equipment (not control tags).
   *
   * @param id of the equipment
   * @return the ids in a collection
   */
  public Collection<Long> getDataTagIdsByEquipmentId(Long equipmentId) {
    return cache.query(dataTag -> dataTag.getEquipmentId().equals(equipmentId))
      .stream().map(Tag::getId).collect(Collectors.toSet());
  }

  /**
   * Returns a collection of the ids of all DataTags
   * registered with this DAQ (not control tags).
   *
   * @param processId the Process id
   * @return the ids in a collection
   */
  public Collection<Long> getDataTagIdsByProcessId(Long processId) {
    return cache.query(dataTag -> dataTag.getProcessId().equals(processId))
      .stream().map(DataTag::getId).collect(Collectors.toSet());
  }
}
