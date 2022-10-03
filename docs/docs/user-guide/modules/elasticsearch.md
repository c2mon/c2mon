---
layout:   post
title:    Elasticsearch archiving
summary:  Explain how you can use the Elasticsearch publisher to store your data in Elasticsearch
---


C2MON no longer comes integrated with [Elasticsearch](https://www.elastic.co/guide/index.html).
Instead, a seperate module has been developed, which uses a combination of the fast
[TransportConnector](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-transport.html)
and the bulk API to achieve highly performant indexing speeds.

# Setup

First, becuase this publisher relies on the C2MON client API, you will need to provide the following values:
```
c2mon.client.jms.url # link to your local c2mon instance
c2mon.domain         # JMS domain that C2MON is publishing data on
```

After you are able to connect this client to your C2MON instance, then you need to specify how to connect to Elastic/Opensearch
```
elastic.url        - The url to your elasticsearch cluster, defaults to localhost, without suffix. For example, for: ``http://localhost/es`` you should put ``localhost``
elastic.url.scheme - The scheme of your cluster, either http or https.
elastic.username   - Username for Elasticsearch cluster authentication, only needed if using https scheme.
elastic.password   - Password for Elasticsearch cluster authentication, only needed if using https scheme.
elastic.url.suffix - Suffic of url to elasticsearch cluster. For example, for: ``http://localhost`` put ``/``, for ``http://localhost/es`` put ``/es``
```

There are more properties that can be set, but for most usecases the defaults should be adequate.

However, if you want to enable the recovery capabilities of this publisher, or index configuration tags (which are fetched from the database),
then you need to provide the following information, which needs to point to the database used by your instance of c2mon.
```
c2mon.client.history.jdbc.url=jdbc:hsqldb:hsql://localhost/c2mondb;sql.syntax_ora=true
c2mon.client.history.jdbc.username=sa
c2mon.client.history.jdbc.password=
```



# Indexing strategies

Depending on the use case, the Elasticsearch publisher can be tweaked to use different time series
indexing strategies. The strategy can be set using the `c2mon.server.elasticsearch.indexType`
property.

| Option | Value | Pattern | Example |
|--------|-------|---------|---------|
| Daily   | `D` or `d`  | `yyyy-MM-dd` | `tag_2016-02-21`      |
| Weekly  | `W` or `w`  | `yyyy-'W'ww` | `alarm_2016-'2'27`    |
| Monthly | `M` or `m`  | `yyyy-MM`    | `supervision_2016-02` |

# Mappings and types

Since C2MON tags can have arbitrary types multiple fields are used
to store the value depending on its type.

| Java type | Field(s) |
|-----------|----------|
| `Boolean` | `valueBoolean` (`true` or `false`), `value` (`0` or `1`)
| `Short`   | `value` (as double)
| `Integer` | `value` (as double)
| `Float`   | `value` (as double)
| `Double`  | `value` (as double)
| `Long`    | `value` (as double), `valueLong` (as long)
| `String`  | `valueString`
| `Object`  | `valueObject` (as object, dynamic mapping)


# TODO change link to publisher mappings
[Alarms](https://github.com/c2mon/c2mon/tree/master/c2mon-server/c2mon-server-elasticsearch/src/main/resources/mappings/alarm.json)
and [SupervisionEvents](https://github.com/c2mon/c2mon/tree/master/c2mon-server/c2mon-server-elasticsearch/src/main/resources/mappings/alarm.json)
have simple mappings; see their sources for reference.


# Metadata

The mappings allow arbitrary metadata fields to be added dynamically.

All `String` metadata values are explicitly set to be `non_analyzed`. Other types
are determined using the default Elasticsearch type analysis strategy.

# Advanced customisation
## TODO - remaining publisher properties

The publisher can be customized by setting the following properties in your publisher.properties file, in addition to the previously described properties: 

```
## General setup
c2mon.gap.finder=False                           # Specifies whether or not the publisher should attempt to reindex gaps it finds by reading from the database
index.configs=False				 # Specifies whether or not the publisher should attempt to index configuration tags, which are read from the database
index.gap.fixed.delay.ms=10000	 		 # The fixed delay interval between each time the publisher attempts to reindex all found gaps, if reindexing gaps is enabled
index.config.fixed.delay.ms=18000		 # The delay between each time all index tags are read from the database and written to Elasticsearch
tag.index.date.format=m		 		 # The date format for data tags
alarm.index.date.format=m			 # The date format for alarm tags
supervision.index.date.format=m	 		 # The date format for supervision tags
index.prefix=c2mon				 # The prefix assigned to each index in elasticsearch. For example, default is ``c2mon-tag_2022_07``, in July 2022.

## Elastic / Opensearch bulk api settings
bulk.flush.interval.ms=5000                      # The fixed delay between each time the Elasticsearch queue is flushed and written to Elasticsearch
```
