package cern.c2mon.server.cache.tag.query;

import java.util.List;

public interface TagQuery<T> {

    List<T> findTagsByName(String name, int maxResults);

    List<T> findTagsByWildcard(String wildcard, int maxResults);

    List<T> findTagsByProcessId(Long processId, int maxResults);

    List<T> findTagsByEquipmentId(Long equipmentId, int maxResults);

    List<T> findTagsBySubEquipmentId(Long subEquipmentId, int maxResults);

}
