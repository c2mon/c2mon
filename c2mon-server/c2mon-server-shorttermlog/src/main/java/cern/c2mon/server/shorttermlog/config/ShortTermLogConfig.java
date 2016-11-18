package cern.c2mon.server.shorttermlog.config;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.shorttermlog.alarm.AlarmListener;
import cern.c2mon.server.shorttermlog.dao.LoggerDAO;
import cern.c2mon.server.shorttermlog.structure.ExpressionLog;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@ImportResource("classpath:config/server-shorttermlog.xml")
public class ShortTermLogConfig {

  @Autowired
  Environment environment;

  @Bean(name = "expressionLoggerDAO")
  public LoggerDAO<ExpressionLog> expressionLoggerDAO(
      @Qualifier("stlSqlSessionFactory") final SqlSessionFactory sqlSessionFactory) throws ClassNotFoundException {

    return new LoggerDAO<>(sqlSessionFactory,
        "cern.c2mon.server.shorttermlog.mapper.ExpressionLogMapper",
        environment.getRequiredProperty("c2mon.server.shorttermlog.jdbc.url"));
  }

  @Bean(name = "stlExpressionPersistenceManager")
  public IPersistenceManager<ExpressionLog> stlExpressionPersistenceManager(
      @Qualifier("expressionLoggerDAO") final LoggerDAO<ExpressionLog> persistenceHandler) {

    return new PersistenceManager<>(persistenceHandler,
        environment.getRequiredProperty("c2mon.server.shorttermlog.fallback.expression"),
        new AlarmListener(), new ExpressionLog());
  }

}
