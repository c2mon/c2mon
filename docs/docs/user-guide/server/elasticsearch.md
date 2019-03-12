---
layout:   post
title:    Elasticsearch archiving
summary:  Explains how the C2MON server is storing historical data into Elasticsearch.
---
{{""}}

C2MON comes integrated with [Elasticsearch](https://www.elastic.co/guide/index.html)
out-of-the-box for time series data storage. It uses a combination of the fast
[TransportConnector](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-transport.html)
and the bulk API to achieve highly performant indexing speeds.

# Connecting to Elasticsearch

By default, an embedded Elasticsearch node is started within the JVM. To
connect to an external cluster, set `c2mon.server.elasticsearch.embedded` to `false`
and point `c2mon.server.elasticsearch.host` and `c2mon.server.elasticsearch.port`
to the required values.

# Indexing strategies

Depending on the use case, C2MON can be tweaked to use different time series
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


[Alarms](https://github.com/c2mon/c2mon/tree/master/c2mon-server/c2mon-server-elasticsearch/src/main/resources/mappings/alarm.json)
and [SupervisionEvents](https://github.com/c2mon/c2mon/tree/master/c2mon-server/c2mon-server-elasticsearch/src/main/resources/mappings/alarm.json)
have simple mappings; see their sources for reference.


# Metadata

The mappings allow arbitrary metadata fields to be added dynamically.

All `String` metadata values are explicitly set to be `non_analyzed`. Other types
are determined using the default Elasticsearch type analysis strategy.

# Advanced customisation

Other aspects of the Elasticsearch integration can be customised. See Elasticsearch section in the [c2mon-server.properties](https://github.com/c2mon/c2mon/blob/master/c2mon-server/distribution/tar/conf/c2mon-server.properties#L340) file for details.
