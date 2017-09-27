package cern.c2mon.server.common.expression;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;

import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Franz Ritter
 */
@Data
public class DslExpression implements Cacheable, Serializable{

  /**
   * Unique expression identifier.
   */
  private Long id;

  /**
   * The description of the expression.
   */
  private String description;

  /**
   * The data type of the expression result.
   */
  private String dataType;

  /**
   * Collection which indicates if this expression has also an alarm configured alarms.
   * If this list is not empty, the evaluation of the expression will also evaluate this alarms.
   */
  private Collection<Long> alarmIds;

  /**
   * The metadata of this expression.
   */
  private Metadata metadata;

  /**
   * The expression which needs to be resolved.
   */
  private String expression;

  /**
   * The last result of the expression.
   */
  private Object result;

  /**
   * The current version of the expression.
   */
  private long version;

  @Override
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}