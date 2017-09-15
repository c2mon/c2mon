package cern.c2mon.server.common.expression;

import java.io.Serializable;

import lombok.Data;

import cern.c2mon.server.common.metadata.Metadata;

/**
 * @author Franz Ritter
 */
@Data
public class Expression implements Serializable {

  /**
   * The name of the expression. The name has to be unique inside a tag.
   */
  private String name;

  /**
   * The data type of the expression result.
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

  /**
   * The current version of the expression.
   */
  private long version;

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