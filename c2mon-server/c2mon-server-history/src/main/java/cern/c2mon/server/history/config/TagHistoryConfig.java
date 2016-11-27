package cern.c2mon.server.history.config;

import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.history.alarm.AlarmListener;
import cern.c2mon.server.history.dao.LoggerDAO;
import cern.c2mon.server.history.logger.BatchLogger;
import cern.c2mon.server.history.logger.DefaultLogger;
import cern.c2mon.server.history.mapper.ExpressionLogMapper;
import cern.c2mon.server.history.mapper.TagRecordMapper;
import cern.c2mon.server.history.structure.ExpressionLog;
import cern.c2mon.server.history.structure.TagRecord;
import cern.c2mon.server.history.structure.TagRecordConverter;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class TagHistoryConfig {

  @Autowired
  private HistoryProperties properties;

  @Autowired
  @Qualifier("historySqlSessionFactory")
  private SqlSessionFactoryBean historySqlSessionFactory;

  @Bean
  public BatchLogger<Tag> tagLogger(TagRecordConverter tagRecordConverter, AlarmListener alarmListener) throws Exception {
    return new DefaultLogger<>(tagRecordConverter, tagHistoryPersistenceManager(alarmListener));
  }

  @Bean
  public PersistenceManager<TagRecord> tagHistoryPersistenceManager(AlarmListener alarmListener) throws Exception {
    String fallbackFile = properties.getTagFallbackFile();
    return new PersistenceManager<>(tagLoggerDAO(), fallbackFile, alarmListener, new TagRecord());
  }

  @Bean
  public LoggerDAO<TagRecord> tagLoggerDAO() throws Exception {
    return new LoggerDAO<>(historySqlSessionFactory.getObject(), TagRecordMapper.class.getCanonicalName(),
        properties.getJdbc().getUrl());
  }

  @Bean
  public PersistenceManager<ExpressionLog> expressionPersistenceManager(AlarmListener alarmListener) throws Exception {
    return new PersistenceManager<>(expressionLoggerDAO(), properties.getExpressionFallbackFile(), alarmListener, new ExpressionLog());
  }

  @Bean
  public LoggerDAO<ExpressionLog> expressionLoggerDAO() throws Exception {
    return new LoggerDAO<>(historySqlSessionFactory.getObject(), ExpressionLogMapper.class.getCanonicalName(), properties.getJdbc().getUrl());
  }
}
