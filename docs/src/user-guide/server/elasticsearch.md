# Elasticsearch

C2MON comes integrates with [Elasticsearch](https://www.elastic.co/guide/index.html) 
out-of-the-box for time series data storage. It uses a combination of the fast 
[TransportConnector](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-transport.html)
and the bulk API to achieve highly performant indexing speeds.

## Connecting to Elasticsearch

By default, an embedded Elasticsearch node is started within the JVM. To 
connect to an external cluster, set `c2mon.server.elasticsearch.embedded` to `false`
and point `c2mon.server.elasticsearch.host` and `c2mon.server.elasticsearch.port`
to the required values.

## Indexing strategies

Depending on the use case, C2MON can be tweaked to use different time series 
indexing strategies. The strategy can be set using the `c2mon.server.elasticsearch.indexType`
property.

| Option | Value | Pattern | Example |
|--------|-------|---------|---------|
| Daily   | `D` or `d`  | `yyyy-MM-dd` | `tag_2016-02-21`      |
| Weekly  | `W` or `w`  | `yyyy-'W'ww` | `alarm_2016-'2'27`    |
| Monthly | `M` or `m`  | `yyyy-MM`    | `supervision_2016-02` |

## Mappings and types

Since C2MON tags can have arbitrary types, each type has its own Elasticsearch 
type mapping that extends from the 
[base mapping](https://gitlab.cern.ch/c2mon/c2mon/tree/master/c2mon-server/c2mon-server-elasticsearch/src/main/resources/mappings/tag.json).

The following table describes the corresponding Elasticsearch type mapping name 
and the extra fields that are added to the base mapping for that type:

| Java type | Elasticsearch type | Field(s) |
|-----------|--------------------|----------|
| `Boolean` | `type_boolean` | `valueBoolean` (`true` or `false`), `value` (`0` or `1`)
| `Short`   | `type_short`   | `value` (as double)
| `Integer` | `type_integer` | `value` (as double)
| `Float`   | `type_float`   | `value` (as double)
| `Double`  | `type_double`  | `value` (as double)
| `Long`    | `type_long`    | `value` (as double), `valueLong` (as long)
| `String`  | `type_string`  | `valueString`
| `Object`  | `type_object`  | `valueObject` (as object, dynamic mapping)


[Alarms](https://gitlab.cern.ch/c2mon/c2mon/tree/master/c2mon-server/c2mon-server-elasticsearch/src/main/resources/mappings/alarm.json) 
and [SupervisionEvents](https://gitlab.cern.ch/c2mon/c2mon/tree/master/c2mon-server/c2mon-server-elasticsearch/src/main/resources/mappings/alarm.json) 
have simple mappings; see their sources for reference. 


## Metadata

The mappings allow arbitrary metadata fields to be added dynamically. 

All `String` metadata values are explicitly set to be `non_analyzed`. Other types
are determined using the default Elasticsearch type analysis strategy.

## Advanced customisation

Other aspects of the Elasticsearch integration can be customised. See [ElasticsearchProperties](
https://gitlab.cern.ch/c2mon/c2mon/blob/master/c2mon-server/c2mon-server-elasticsearch/src/main/java/cern/c2mon/server/elasticsearch/config/ElasticsearchProperties.java)
for details.
