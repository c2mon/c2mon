package cern.c2mon.server.elasticsearch.tag;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.tag.Tag;
import org.springframework.core.convert.converter.Converter;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Justin Lewis Salmon
 */
public class BaseTagDocumentConverter<T extends Map<String, Object>> implements Converter<Tag, Optional<T>> {

    final ProcessCache processCache;
    final EquipmentCache equipmentCache;
    final SubEquipmentCache subEquipmentCache;
    private final Supplier<T> containerSupplier;

    public BaseTagDocumentConverter(final ProcessCache processCache, final EquipmentCache equipmentCache, final SubEquipmentCache subEquipmentCache, final Supplier<T> containerSupplier) {
        this.processCache = processCache;
        this.equipmentCache = equipmentCache;
        this.subEquipmentCache = subEquipmentCache;
        this.containerSupplier = containerSupplier;
    }

    @Override
    public Optional<T> convert(Tag tag) {
        T map = containerSupplier.get();
        map.put("id", tag.getId());
        map.put("name", tag.getName());
        map.put("description", tag.getDescription());
        map.put("unit", tag.getUnit());
        map.put("mode", tag.getMode());
        map.put("metadata", getMetadata(tag));
        map.put("c2mon", getC2monMetadata(tag));
        return Optional.of(map);
    }

    private T getMetadata(Tag tag) {
        Metadata metadata = tag.getMetadata();
        if (metadata != null) {
            return metadata.getMetadata().entrySet().stream()
                    .filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                            containerSupplier
                    ));
        }
        return containerSupplier.get();
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
