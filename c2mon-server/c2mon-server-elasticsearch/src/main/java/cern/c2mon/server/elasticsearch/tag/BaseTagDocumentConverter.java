package cern.c2mon.server.elasticsearch.tag;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.tag.Tag;

/**
 * @author Justin Lewis Salmon
 */
public class BaseTagDocumentConverter implements Converter<Tag, Map<String, Object>> {

  private final ProcessCache processCache;
  private final EquipmentCache equipmentCache;
  private final SubEquipmentCache subEquipmentCache;

  public BaseTagDocumentConverter(final ProcessCache processCache, final EquipmentCache equipmentCache, final SubEquipmentCache subEquipmentCache) {
    this.processCache = processCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
  }

  @Override
  public Map<String, Object> convert(Tag tag) {
    Map<String, Object> map = new HashMap<>();

    map.put("id", tag.getId());
    map.put("name", tag.getName());
    map.put("description", tag.getDescription());
    map.put("unit", tag.getUnit());
    map.put("mode", tag.getMode());
    map.put("metadata", getMetadata(tag));
    map.put("c2mon", getC2monMetadata(tag));

    return map;
  }

  protected Map<String, Object> getMetadata(Tag tag) {
    Metadata metadata = tag.getMetadata();

    if (metadata != null) {
      return metadata.getMetadata().entrySet().stream().collect(Collectors.toMap(
          Map.Entry::getKey,
          e -> e.getValue() == null ? null : e.getValue()
      ));
    }

    return Collections.emptyMap();
  }

  protected Map<String, Object> getC2monMetadata(Tag tag) {
    Map<String, Object> map = new HashMap<>();

    map.put("dataType", tag.getDataType());

    if (!tag.getProcessIds().isEmpty()) {
      Process process = processCache.get(tag.getProcessIds().iterator().next());
      map.put("process", process.getName());
    }

    if (!tag.getEquipmentIds().isEmpty()) {
      Equipment equipment = equipmentCache.get(tag.getEquipmentIds().iterator().next());
      map.put("equipment", equipment.getName());
    }

    if (!tag.getSubEquipmentIds().isEmpty()) {
      SubEquipment subEquipment = subEquipmentCache.get(tag.getSubEquipmentIds().iterator().next());
      map.put("subEquipment", subEquipment.getName());
    }

    return map;
  }
}
