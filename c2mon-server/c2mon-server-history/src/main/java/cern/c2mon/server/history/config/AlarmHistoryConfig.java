package cern.c2mon.server.history.config;


import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.history.alarm.AlarmListener;
import cern.c2mon.server.history.dao.LoggerDAO;
import cern.c2mon.server.history.logger.BatchLogger;
import cern.c2mon.server.history.logger.DefaultLogger;
import cern.c2mon.server.history.mapper.AlarmRecordMapper;
import cern.c2mon.server.history.structure.AlarmRecord;
import cern.c2mon.server.history.structure.AlarmRecordConverter;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class AlarmHistoryConfig {

  @Autowired
  private HistoryProperties properties;

  @Autowired
  @Qualifier("historySqlSessionFactory")
  private SqlSessionFactoryBean historySqlSessionFactory;

  @Bean
  public BatchLogger<Alarm> alarmLogger(AlarmRecordConverter alarmRecordConverter, AlarmListener alarmListener) throws Exception {
    return new DefaultLogger<>(alarmRecordConverter, alarmHistoryPersistenceManager(alarmListener));
  }

  @Bean
  public PersistenceManager<AlarmRecord> alarmHistoryPersistenceManager(AlarmListener alarmListener) throws Exception {
    String fallbackFile = properties.getAlarmFallbackFile();
    return new PersistenceManager<>(alarmLoggerDAO(), fallbackFile, alarmListener, new AlarmRecord());
  }

  @Bean
  public LoggerDAO<AlarmRecord> alarmLoggerDAO() throws Exception {
    return new LoggerDAO<>(historySqlSessionFactory.getObject(), AlarmRecordMapper.class.getCanonicalName(),
        properties.getJdbc().getUrl());
  }
}
