# Database
By default C2MON start with an embedded HSQL database for prototyping and development purposes.

!!! warning "Be careful!"
    When C2mon is stopped all configuration- and history data will be lost.

## Setup C2MON with persistent HSQL
To persist the HSQL data on hard disk the following properties have to be set:

**C2MON properties**

```java
c2mon.server.cachedbaccess.jdbc.url=jdbc:hsqldb:hsql://localhost/c2mondb;sql.syntax_ora=true
c2mon.server.history.jdbc.url=jdbc:hsqldb:hsql://localhost/c2mondb;sql.syntax_ora=true
```
These settings can also be set as Java VM options with the ```-D``` parameter.

See also [Configuring the module](user-guide/client-api/history).

!!! warning "Be careful!"
    If the URL contains ``hsql://``, C2MON uses an in-memory HSQL with a hardcoded path at ``/tmp/c2mondb``.
    The open [Issue 158](https://gitlab.cern.ch/c2mon/c2mon/issues/158) addresses this problem.

## Setup C2MON with Oracle
to be documented

## Setup C2MON with MySQL
to be documented