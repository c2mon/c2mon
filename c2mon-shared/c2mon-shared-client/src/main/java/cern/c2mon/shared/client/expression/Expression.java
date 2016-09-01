package cern.c2mon.shared.client.expression;

import cern.c2mon.shared.common.metadata.Metadata;
import lombok.Data;

/**
 *
 * @author Franz Ritter
 */
@Data
public class Expression {

  /**
   * A flag which indicates if this expression is also an alarm.
   */
  private final Boolean alarm;

  /**
   * The metadata of of this expression. For example fault- code, member, family.
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
   * The datatype of the expression result
   */
  private String dataType;

}
