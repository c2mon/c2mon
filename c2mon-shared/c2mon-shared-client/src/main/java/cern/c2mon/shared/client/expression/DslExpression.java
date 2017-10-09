package cern.c2mon.shared.client.expression;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import org.springframework.util.Assert;

import cern.c2mon.shared.client.configuration.api.tag.Tag;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.metadata.Metadata;
import cern.c2mon.shared.common.rule.RuleInputValue;

/**
 * @author Martin Flamm
 */
@Data
public class DslExpression extends Tag implements Serializable {

  /**
   * Unique expression identifier.
   */
  @IgnoreProperty
  private Long id;

  /**
   * The name of the expression.
   */
  private String name;

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

  /**
   * Corresponding rule id in the ruleTagCache. Every Expression has a corresponding rule in the ruleTagCache in order
   * to enable the ruleEngine to treat expression in the same way like rules.
   */
  private long cacheRuleId;

  public static DslExpression.CreateBuilder create(String name, String desciption, Class<?> datatype, String expression) {
    Assert.hasText(name, "Expression name is required!");
    Assert.hasText(desciption, "Expression description is required!");
    Assert.notNull(datatype, "Data type is required!");
    Assert.hasText(expression, "Expression is required!");

    return new CreateBuilder(name, desciption, datatype, expression);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return new CloneNotSupportedException();
  }

  public static class CreateBuilder {
    private DslExpression expressionToBuild = new DslExpression();

    private CreateBuilder(String name, String desciption, Class<?> datatype, String expression) {
      this.expressionToBuild.setDescription(desciption);
      this.expressionToBuild.setName(name);
      this.expressionToBuild.setDataType(datatype.getName());
      this.expressionToBuild.setExpression(expression);
    }

    public DslExpression.CreateBuilder id(Long id) {
      this.expressionToBuild.setId(id);
      return this;
    }

    public DslExpression.CreateBuilder description(String description) {
      this.expressionToBuild.setDescription(description);
      return this;
    }

    public DslExpression.CreateBuilder addMetadata(String key, Object value) {
      if (this.expressionToBuild.getMetadata() == null) {
        Metadata metadata = new Metadata();
        this.expressionToBuild.setMetadata(metadata);
      }
      this.expressionToBuild.getMetadata().addMetadata(key, value);
      return this;
    }

    public DslExpression build() {
      this.expressionToBuild.setCreated(true);
      return this.expressionToBuild;
    }
  }
}