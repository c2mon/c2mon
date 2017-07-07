# Database
By default C2MON start with an embedded HSQL database for prototyping and development purposes.

!!! warning "Be careful!"
    When C2mon is stopped all configuration- and history data will be lost.

## Setup C2MON with persistent HSQL
To persist the HSQL data on hard disk the following properties have to be set:

**`C2MON properties**

```java
c2mon.server.cachedbaccess.jdbc.url=jdbc:hsqldb:hsql://localhost/c2mondb;sql.syntax_ora=true
c2mon.server.history.jdbc.url=jdbc:hsqldb:hsql://localhost/c2mondb;sql.syntax_ora=true
```
This settings can also be set as InteliJ VM options option with the ```-D``` parameter.

## Setup C2MON with Oracle
to be documented

## Setup C2MON with MySQL
to be documented