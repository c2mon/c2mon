package cern.c2mon.client.core.configuration;

import java.util.List;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.expression.DslExpression;

/**
 * @author Martin Flamm
 */
public interface ExpressionConfigurationManager {

  ConfigurationReport createExpression(String name, String desciption, Class<?> datatype, String expression);

  ConfigurationReport createExpression(DslExpression expression);

  ConfigurationReport createExpressions(List<DslExpression> ruleTags);

//  ConfigurationReport removeExpressionById(Long id);
//  ConfigurationReport updateExpression(DslExpression expression);
}
