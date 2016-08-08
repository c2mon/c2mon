# Accessing history

If you want to make use of the c2mon-client-ext-history module you have in addition to define the database credentials.
The history module enables you to browse the tag history and to replay entire data sets (History Player).

```bash
# The JDBC driver
# c2mon.jdbc.driver=oracle.jdbc.OracleDriver
c2mon.jdbc.driver=org.hsqldb.jdbcDriver

# HSQL demo db credentials
c2mon.jdbc.ro.url=jdbc:hsqldb:hsql://localhost/stl;sql.syntax_ora=true
c2mon.jdbc.ro.user=sa
c2mon.jdbc.ro.password=
```

!!! info "Please note!"
    C2MON currently only supports Oracle, HSQL and MySQL as backing stores.
    In the near future we are planning to migrate to time series databases to provide better performance.
