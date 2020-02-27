package cern.c2mon.server.common.control;

import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.server.common.tag.AbstractInfoTagCacheObject;
import cern.c2mon.server.common.tag.InfoTag;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.Set;

import static cern.c2mon.server.common.util.Java9Collections.setOf;
import static cern.c2mon.server.common.util.KotlinAPIs.apply;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class ControlTag extends AbstractInfoTagCacheObject implements InfoTag {
  /**
   * Id of the {@link Supervised} object
   */
  Long supervisedId;

  /**
   * Type of the {@link Supervised} object
   */
  SupervisionEntity supervisedEntity;

  // always FALSE in TIM; TRUE not supported
  private final Boolean faultValue = Boolean.FALSE;

  public ControlTag(Long id, Long supervisedId, SupervisionEntity supervisedEntity) {
    super(id);
    setDataType("java.lang.Boolean");
    setValue(faultValue);
    this.supervisedId = supervisedId;
    this.supervisedEntity = supervisedEntity;
  }

  /**
   * Will set to this object the source and DAQ timestamps of the given other Control tag
   */
  public void setTimeStampsFrom(ControlTag other) {
    setSourceTimestamp(other.getSourceTimestamp());
    setDaqTimestamp(other.getDaqTimestamp());
  }

  @Override
  public Boolean getValue() {
    return (Boolean) super.getValue();
  }

  @Override
  public Set<Long> getEquipmentIds() {
    return supervisedEntity == SupervisionEntity.EQUIPMENT ? setOf(supervisedId) : Collections.emptySet();
  }

  @Override
  public Set<Long> getProcessIds() {
    return supervisedEntity == SupervisionEntity.PROCESS ? setOf(supervisedId) : Collections.emptySet();
  }

  @Override
  public Set<Long> getSubEquipmentIds() {
    return supervisedEntity == SupervisionEntity.SUBEQUIPMENT ? setOf(supervisedId) : Collections.emptySet();
  }

  public static DataTag convertToDataTag(ControlTag control) {
    return apply(new DataTagCacheObject(control.getId()), dataTag -> {

      dataTag.setSourceTimestamp(control.getSourceTimestamp());
      dataTag.setCacheTimestamp(control.getCacheTimestamp());
      dataTag.setName(control.getName());
      dataTag.setDescription(control.getDescription());
      dataTag.setDataType(control.getDataType());
      dataTag.setMode(control.getMode());
      dataTag.setValue(control.getValue());
      dataTag.setValueDescription(control.getValueDescription());
      dataTag.setDataTagQuality(control.getDataTagQuality());
      dataTag.setUnit(control.getUnit());
      dataTag.setMetadata(control.getMetadata());
      dataTag.setRuleIds(control.getRuleIds());
      dataTag.setAlarmIds(control.getAlarmIds());
      dataTag.setDipAddress(control.getDipAddress());
      dataTag.setAddress(control.getAddress());
    });
  }
}
