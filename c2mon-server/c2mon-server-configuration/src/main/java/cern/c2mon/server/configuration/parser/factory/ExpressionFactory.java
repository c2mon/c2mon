package cern.c2mon.server.configuration.parser.factory;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.expression.DslExpression;

/**
 * @author Martin Flamm
 */
@Service
public class ExpressionFactory extends EntityFactory<DslExpression>{

  private final RuleTagCache ruleTagCache;
  private SequenceDAO sequenceDAO;

  @Autowired
  public ExpressionFactory(RuleTagCache ruleTagCache, SequenceDAO sequenceDAO) {
    super(ruleTagCache);
    this.ruleTagCache = ruleTagCache;
    this.sequenceDAO = sequenceDAO;
  }

  @Override
  public List<ConfigurationElement> createInstance(DslExpression expression) {
    return Collections.singletonList(doCreateInstance(expression));
  }

  @Override
  Long createId(DslExpression configurationEntity) {
    if (configurationEntity.getName() != null && ruleTagCache.get(configurationEntity.getName()) != null) {
      throw new ConfigurationParseException("Error creating expression " + configurationEntity.getName() + ": " +
          "Name already exists!");
    } else {
      return configurationEntity.getId() != null ? configurationEntity.getId() : sequenceDAO.getNextTagId();
    }
  }

  @Override
  Long getId(DslExpression expression) {
    Long id;

    if (expression.getId() != null) {
      id = expression.getId();
    } else {
      if (ruleTagCache.get(expression.getName()) != null) {
        id = ruleTagCache.get(expression.getName()).getId();
      } else {
        throw new ConfigurationParseException("Expression " + expression.getName() + " does not exist!");
      }
    }
    return id;
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.EXPRESSION;
  }
}