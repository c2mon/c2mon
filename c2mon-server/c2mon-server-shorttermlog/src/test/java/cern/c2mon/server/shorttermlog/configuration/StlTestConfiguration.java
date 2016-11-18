package cern.c2mon.server.shorttermlog.configuration;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.shorttermlog.alarm.AlarmListener;
import cern.c2mon.server.shorttermlog.dao.LoggerDAO;
import cern.c2mon.server.shorttermlog.structure.ExpressionLog;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Franz Ritter
 */
@Configuration
@ComponentScan("cern.c2mon.server.shorttermlog.logger")
public class StlTestConfiguration {

  @Bean(name = "expressionLoggerDAO")
  public LoggerDAO<ExpressionLog> expressionLoggerDAO(
      @Qualifier("stlSqlSessionFactory") final SqlSessionFactory sqlSessionFactory) throws ClassNotFoundException {

    return new LoggerDAO<>(sqlSessionFactory,
        "cern.c2mon.server.shorttermlog.mapper.ExpressionLogMapper", "");
  }

  @Bean(name = "stlExpressionPersistenceManager")
  public IPersistenceManager<ExpressionLog> stlExpressionPersistenceManager(
      @Qualifier("expressionLoggerDAO") final LoggerDAO<ExpressionLog> persistenceHandler) {

    return new PersistenceManager<>(persistenceHandler, "", new AlarmListener(), new ExpressionLog());
  }
}
