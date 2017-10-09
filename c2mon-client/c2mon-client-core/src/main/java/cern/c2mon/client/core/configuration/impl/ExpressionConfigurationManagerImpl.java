package cern.c2mon.client.core.configuration.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.client.core.configuration.ExpressionConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.expression.DslExpression;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.validateIsCreate;

/**
 * @author Martin Flamm
 */
@Service("expressionConfigurationManager")
public class ExpressionConfigurationManagerImpl implements ExpressionConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  public ExpressionConfigurationManagerImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  @Override
  public ConfigurationReport createExpression(String name, String desciption, Class<?> datatype, String expression) {
    return createExpression(DslExpression.create(name, desciption, datatype, expression).build());
  }

  @Override
  public ConfigurationReport createExpression(DslExpression expression) {
    return createExpressions(Collections.singletonList(expression));
  }

  @Override
  public ConfigurationReport createExpressions(List<DslExpression> expressions) {
    validateIsCreate(expressions);
    Configuration config = new Configuration();
    config.setEntities(expressions);
    return configurationRequestSender.applyConfiguration(config, null);
  }
}