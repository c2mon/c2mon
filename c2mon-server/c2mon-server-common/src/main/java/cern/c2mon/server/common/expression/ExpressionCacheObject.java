package cern.c2mon.server.common.expression;

import java.sql.Timestamp;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Martin Flamm
 */
@Data
public class ExpressionCacheObject extends AbstractTagCacheObject implements RuleTag {

  /**
   * Version number of the class used during serialization/deserialization. This
   * is to ensure that minor changes to the class do not prevent us from
   * reading back ExpressionCacheObject we have serialized earlier. If fields are
   * added/removed from the class, the version number needs to change.
   */
  private static final long serialVersionUID = -1458781710956275452L;

  /**
   * The expression which needs to be resolved.
   * <p>
   * The last result of the expression.
   * <p>
   * The current version of the expression.
   * <p>
   * Corresponding rule id in the ruleTagCache. Every Expression has a corresponding rule in the ruleTagCache in order
   * to enable the ruleEngine to treat expression in the same way like rules.
   */
  private String expression;

  /**
   * The current version of the expression.
   */
  private long version;

  /**
   * Used for config loader.
   *
   * @param id
   */
  public ExpressionCacheObject(Long id) {
    super(id);
  }

  public ExpressionCacheObject() {
    super();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    ExpressionCacheObject cacheObject = (ExpressionCacheObject) super.clone();
    return cacheObject;
  }

  @Override
  public Timestamp getTimestamp() {
    return getCacheTimestamp();
  }

  @Override
  public Set<Long> getEquipmentIds() {
    return null;
  }

  @Override
  public Set<Long> getProcessIds() {
    return null;
  }

  @Override
  public Set<Long> getSubEquipmentIds() {
    return null;
  }

  @Override
  public String getRuleText() {
    return this.expression;
  }
}