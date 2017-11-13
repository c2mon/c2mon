package cern.c2mon.shared.client.expression;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;

import cern.c2mon.shared.client.configuration.api.tag.Tag;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.metadata.Metadata;

/**
 * @author Martin Flamm
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DslExpression extends Tag {

  /**
   * The data type of the expression result.
   */
  private String dataType;

  /**
   * The expression which needs to be resolved.
   */
  private String expression;

  /**
   * Indicates whether this tag's value changes shall be logged to the
   * history.
   */
  @DefaultValue("true")
  private Boolean isLogged;

  public static DslExpression.CreateBuilder create(String name, String description, Class<?> datatype, String expression) {
    Assert.hasText(name, "Expression name is required!");
    Assert.hasText(description, "Expression description is required!");
    Assert.notNull(datatype, "Data type is required!");
    Assert.hasText(expression, "Expression is required!");

    return new CreateBuilder(name, description, datatype, expression);
  }

  public static class CreateBuilder {
    private DslExpression expressionToBuild = new DslExpression();

    private CreateBuilder(String name, String description, Class<?> datatype, String expression) {
      this.expressionToBuild.setDescription(description);
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