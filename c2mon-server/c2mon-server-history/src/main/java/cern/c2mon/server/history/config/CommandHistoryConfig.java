package cern.c2mon.server.history.config;

import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.history.alarm.AlarmListener;
import cern.c2mon.server.history.dao.LoggerDAO;
import cern.c2mon.server.history.mapper.CommandRecordMapper;
import cern.c2mon.shared.client.command.CommandRecord;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class CommandHistoryConfig {

  @Autowired
  private HistoryProperties properties;

  @Autowired
  @Qualifier("historySqlSessionFactory")
  private SqlSessionFactoryBean historySqlSessionFactory;

  @Bean
  public PersistenceManager<CommandRecord> commandHistoryPersistenceManager(AlarmListener alarmListener) throws Exception {
    String fallbackFile = properties.getCommandFallbackFile();
    return new PersistenceManager<>(commandLoggerDAO(), fallbackFile, alarmListener, new CommandRecord());
  }

  @Bean
  public LoggerDAO<CommandRecord> commandLoggerDAO() throws Exception {
    return new LoggerDAO<>(historySqlSessionFactory.getObject(), CommandRecordMapper.class.getCanonicalName(),
        properties.getJdbc().getUrl());
  }
}
