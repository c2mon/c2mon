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
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class AlarmHistoryConfig {

  @Autowired
  private Environment environment;

  @Autowired
  @Qualifier("historySqlSessionFactory")
  private SqlSessionFactoryBean historySqlSessionFactory;

  @Bean
  public BatchLogger<Alarm> alarmLogger(AlarmRecordConverter alarmRecordConverter, AlarmListener alarmListener) throws Exception {
    return new DefaultLogger<>(alarmRecordConverter, alarmPersistenceManager(alarmListener));
  }

  @Bean
  public PersistenceManager<AlarmRecord> alarmPersistenceManager(AlarmListener alarmListener) throws Exception {
    String fallbackFile = environment.getRequiredProperty("c2mon.server.history.fallback.alarm");
    return new PersistenceManager<>(alarmLoggerDAO(), fallbackFile, alarmListener, new AlarmRecord());
  }

  @Bean
  public LoggerDAO<AlarmRecord> alarmLoggerDAO() throws Exception {
    return new LoggerDAO<>(historySqlSessionFactory.getObject(), AlarmRecordMapper.class.getCanonicalName(),
        environment.getRequiredProperty("c2mon.server.history.jdbc.url"));
  }
}
