package cern.c2mon.server.common.util;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

/**
 * @author Justin Lewis Salmon
 */
public class HsqlDatabaseBuilder {
  private String url;
  private String username;
  private String password;
  private List<Resource> scripts = new ArrayList<>();

  public HsqlDatabaseBuilder url(String url) {
    this.url = url;
    return this;
  }
  
  public HsqlDatabaseBuilder username(String username) {
    this.username = username;
    return this;
  }
  
  public HsqlDatabaseBuilder password(String password) {
    this.password = password;
    return this;
  }

  public HsqlDatabaseBuilder addScript(Resource resource) {
    this.scripts.add(resource);
    return this;
  }

  public DataSource build() {
    DataSource dataSource;

    if (url == null || url.contains("hsqldb:mem")) {
      // Start an in-process, in-memory HSQL server
      dataSource = new EmbeddedDatabaseBuilder().setType(HSQL).setName("c2mondb").build();
    } else if (url.contains("hsql://")) {
      // Start an externally visible, file-based HSQL server
      HsqlServer.start("file:///tmp/c2mondb", "c2mondb");
      url += ";sql.syntax_ora=true;hsqldb.default_table_type=cached;hsqldb.cache_rows=1000;hsqldb.result_max_memory_rows=2000;hsqldb.cache_size=100";
      dataSource = DataSourceBuilder.create().url(url).username(username).password(password).build();

    } else {
      throw new RuntimeException("The given URL was not a valid HSQL JDBC URL!");
    }

    if (!scripts.isEmpty()) {
      ResourceDatabasePopulator populator = new ResourceDatabasePopulator(scripts.toArray(new Resource[scripts.size()]));
      populator.setContinueOnError(true);
      DatabasePopulatorUtils.execute(populator, dataSource);
    }

    return dataSource;
  }
}
