---
layout:   post
title:    Accessing historical data
summary:  Lean how to access historical data through the Java API.
---
{{""}}

Client-based access to historical data is made available through the `c2mon-client-ext-history` module. In addition to providing raw history, you can also
use it to go back in time to replay particular periods of time (useful for debugging source issues).

To enable the module, simply add it to your classpath. Using Maven:


```xml
<dependency>
    <groupId>cern.c2mon.client</groupId>
    <artifactId>c2mon-client-ext-history</artifactId>
    <version>__insert_version_here__</version>
</dependency>
```

Or using Gradle:

```
compile "cern.c2mon.client:c2mon-client-ext-history:__insert_version_here__"
```

# Configuring the module

The history module requires a few extra properties in order to work. If you are running a local C2MON server, simply point the module to its database:

```bash
c2mon.client.history.jdbc.url=jdbc:hsqldb:hsql://localhost/c2mondb;sql.syntax_ora=true
c2mon.client.history.jdbc.username=sa
c2mon.client.history.jdbc.password=
```
See also [C2MON properties](/user-guide/server/database).

!!! info "Note"
    C2MON currently only supports Oracle, HSQL and MySQL as backing stores.
    In the near future we are planning to migrate to time series databases to provide better performance.
