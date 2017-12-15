# Database
By default C2MON start with an embedded HSQL database for prototyping and development purposes.

!!! warning "Be careful!"
    When C2mon is stopped all configuration- and history data will be lost.

## Setup C2MON with persistent HSQL
To persist the HSQL data on hard disk the following properties have to be set:

**C2MON properties**

```shell
c2mon.server.cachedbaccess.jdbc.url=jdbc:hsqldb:hsql://localhost/c2mondb;sql.syntax_ora=true
c2mon.server.history.jdbc.url=jdbc:hsqldb:hsql://localhost/c2mondb;sql.syntax_ora=true
```
These settings can also be set as Java VM options with the ```-D``` parameter or as [Spring Boot environment variables](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

See also [Configuring the module](/user-guide/client-api/history/#configuring-the-module).

!!! warning "Be careful!"
    If the URL contains ``hsql://``, C2MON uses an in-memory HSQL with a hardcoded path at ``/tmp/c2mondb``.
    The open [Issue 158](https://gitlab.cern.ch/c2mon/c2mon/issues/158) addresses this problem.

## Setup C2MON with Oracle

!!! IMPORTANT : Oracle drivers cannot be distributed under an open-source license.
To connect C2MON to an Oracle database, you must download the Oracle JDBC driver libraries (typically ojdbc.jar and orai18n.jar) and copy them under ```/c2mon-server/lib```.  
Oracle drivers are typically available on the [Oracle Tech Network website](http://www.oracle.com/technetwork/database/features/jdbc/index.html).

To persist data in an Oracle instance (v11 or later) the following properties have to be set:

```shell
c2mon.server.cachedbaccess.jdbc.driver-class-name=oracle.jdbc.driver.OracleDriver
c2mon.server.cachedbaccess.jdbc.url=jdbc:oracle:thin:@myhost:1521:orcl
c2mon.server.cachedbaccess.jdbc.username=scott
c2mon.server.cachedbaccess.jdbc.password=tiger
c2mon.server.cachedbaccess.jdbc.default-auto-commit=false
# Spring properties required to keep the session open
c2mon.server.cachedbaccess.jdbc.test-while-idle=true
c2mon.server.cachedbaccess.jdbc.test-on-borrow=true
c2mon.server.cachedbaccess.jdbc.validation-query=SELECT 1 FROM DUAL
 
###############################################################################
# c2mon-server-configuration
###############################################################################
c2mon.server.configuration.jdbc.driver-class-name=oracle.jdbc.driver.OracleDriver
c2mon.server.configuration.jdbc.url=${c2mon.server.cachedbaccess.jdbc.url}
c2mon.server.configuration.jdbc.username=${c2mon.server.cachedbaccess.jdbc.username}
c2mon.server.configuration.jdbc.password=${c2mon.server.cachedbaccess.jdbc.password}
# Spring properties required to keep the session open
c2mon.server.configuration.jdbc.test-while-idle=true
c2mon.server.configuration.jdbc.test-on-borrow=true
c2mon.server.configuration.jdbc.validation-query=${c2mon.server.cachedbaccess.jdbc.validation-query}
 
################################################################################
# c2mon-server-history
################################################################################
c2mon.server.history.jdbc.driver-class-name=oracle.jdbc.driver.OracleDriver
c2mon.server.history.jdbc.url=${c2mon.server.cachedbaccess.jdbc.url}
c2mon.server.history.jdbc.username=${c2mon.server.cachedbaccess.jdbc.username}
c2mon.server.history.jdbc.password=${c2mon.server.cachedbaccess.jdbc.password}
# Spring properties required to keep the session open
c2mon.server.history.jdbc.test-while-idle=true
c2mon.server.history.jdbc.test-on-borrow=true
c2mon.server.history.jdbc.validation-query=${c2mon.server.cachedbaccess.jdbc.validation-query}
```
These settings can also be set as Java VM options with the ```-D``` parameter or as [Spring Boot environment variables](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).



## Setup C2MON with MySQL

To persist data in a MySQL instance (v 5.7 minimum) the following properties have to be set:

```shell
c2mon.server.cachedbaccess.jdbc.driver-class-name=com.mysql.jdbc.Driver
c2mon.server.cachedbaccess.jdbc.url=jdbc:mysql://localhost/tim
c2mon.server.cachedbaccess.jdbc.username=admin
c2mon.server.cachedbaccess.jdbc.password=<your password>
c2mon.server.cachedbaccess.jdbc.default-auto-commit=false
# Spring properties required to keep the session open
c2mon.server.cachedbaccess.jdbc.test-while-idle=true
c2mon.server.cachedbaccess.jdbc.test-on-borrow=true
c2mon.server.cachedbaccess.jdbc.validation-query=SELECT 1
 
###############################################################################
# c2mon-server-configuration
###############################################################################
c2mon.server.configuration.jdbc.driver-class-name=com.mysql.jdbc.Driver
c2mon.server.configuration.jdbc.url=${c2mon.server.cachedbaccess.jdbc.url}
c2mon.server.configuration.jdbc.username=${c2mon.server.cachedbaccess.jdbc.username}
c2mon.server.configuration.jdbc.password=${c2mon.server.cachedbaccess.jdbc.password}
# Spring properties required to keep the session open
c2mon.server.configuration.jdbc.test-while-idle=true
c2mon.server.configuration.jdbc.test-on-borrow=true
c2mon.server.configuration.jdbc.validation-query=${c2mon.server.cachedbaccess.jdbc.validation-query}
 
################################################################################
# c2mon-server-history
################################################################################
c2mon.server.history.jdbc.driver-class-name=com.mysql.jdbc.Driver
c2mon.server.history.jdbc.url=${c2mon.server.cachedbaccess.jdbc.url}
c2mon.server.history.jdbc.username=${c2mon.server.cachedbaccess.jdbc.username}
c2mon.server.history.jdbc.password=${c2mon.server.cachedbaccess.jdbc.password}
# Spring properties required to keep the session open
c2mon.server.history.jdbc.test-while-idle=true
c2mon.server.history.jdbc.test-on-borrow=true
c2mon.server.history.jdbc.validation-query=${c2mon.server.cachedbaccess.jdbc.validation-query}
```

These settings can also be set as Java VM options with the ```-D``` parameter or as [Spring Boot environment variables](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).