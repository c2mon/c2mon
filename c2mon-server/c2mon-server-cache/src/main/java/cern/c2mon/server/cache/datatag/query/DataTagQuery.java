package cern.c2mon.server.cache.datatag.query;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;

import java.util.List;

public interface DataTagQuery {

    List<Long> findDataTagIdsByEquipmentId(Long equipmentId) throws CacheElementNotFoundException;

    List<Long> findDataTagIdsBySubEquipmentId(Long subEquipmentId) throws CacheElementNotFoundException;
}
