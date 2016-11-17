package cern.c2mon.shared.client.expression;

import java.io.Serializable;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.metadata.Metadata;
import lombok.Data;

/**
 * @author Franz Ritter
 */
@Data
public class Expression implements Serializable {

  /**
   * Id of the overlying tag. This information is needed for the stl storing.
   */
  private transient Long tagId;

  /**
   * The name of the expression. The name has to be unique inside a tag.
   */
  private String name;

  /**
   * The data type of the expression result
   */
  private String dataType;

  /**
   * A flag which indicates if this expression is also an alarm.
   * If this flag is true, the result of the expression has to be a boolean.
   */
  private Boolean alarm;

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

  private long lastCompileCounter;

  private Expression() {
    this.dataType = Boolean.class.getName();
    this.alarm = true;
  }

  public Expression(String name, String expression) {
    this();
    this.name = name;
    this.expression = expression;
  }

}
