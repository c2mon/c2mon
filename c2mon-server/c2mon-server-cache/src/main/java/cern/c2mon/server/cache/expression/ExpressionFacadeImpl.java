package cern.c2mon.server.cache.expression;

import java.sql.Timestamp;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.ExpressionFacade;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.common.AbstractTagFacade;
import cern.c2mon.server.common.expression.ExpressionCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.daq.config.Change;

/**
 * @author Martin Flamm
 */
@Slf4j
public class ExpressionFacadeImpl extends AbstractTagFacade<RuleTag> implements ExpressionFacade {

  @Autowired
  public ExpressionFacadeImpl(final RuleTagCache ruleTagCache,
                              final AlarmFacade alarmFacade,
                              final AlarmCache alarmCache) {
    super(ruleTagCache, alarmFacade, alarmCache);

  }

  @Override
  public ExpressionCacheObject createCacheObject(Long id, Properties properties) throws IllegalAccessException {
    ExpressionCacheObject expression = new ExpressionCacheObject(id);
    setCommonProperties(expression, properties);

    try {
      String tmpStr;


      // Version of the expression
      if ((tmpStr = properties.getProperty("version")) != null) {
        expression.setVersion(Long.parseLong(tmpStr));
      }

      // Groovy script
      if ((tmpStr = properties.getProperty("expression")) != null) {
        expression.setExpression(tmpStr);
      }

    } catch (Exception e) {
      log.info("Something went wrong while setting the properties for expression #{}", id, e);
    }
    return expression;
  }

  @Override
  protected void invalidateQuietly(RuleTag tag, TagQualityStatus statusToAdd, String statusDescription, Timestamp timestamp) {

  }

  @Override
  protected Change configureCacheObject(RuleTag cacheObject, Properties properties) throws IllegalAccessException {
    return null;
  }

  @Override
  protected void validateConfig(RuleTag cacheObject) {

  }
}
