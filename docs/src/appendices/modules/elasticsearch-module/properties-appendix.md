<a id="_elasticsearch_module_properties_appendix"></a>
### Elasticsearch module properties

Properties that can be specified inside your _application.properties file, or as command line switches._
The bellow properties can be defined explicitly and overwrite the default ones.

This section provides a list of all the properties that the Elasticsearch module accepts.

```
# ===================================================================
# C2MON ELASTICSEARCH PROPERTIES
#
# This sample file is provided as a guideline. Do NOT copy it and it's
# contents to your own application.               ^^^
#
# Instead, use only the properties that you intend to ovewrite.
# ===================================================================

## The hostname where the ES instance is located
c2mon.server.elasticsearch.host=localhost

## The ES data transportation port (Not the HTTP)
c2mon.server.elasticsearch.port=9300

# Indicates if the ES instance is on the same machine with the module
c2mon.server.elasticsearch.local=true

# The names of node and cluster
c2mon.server.elasticsearch.node=local
c2mon.server.elasticsearch.cluster=elasticsearch

# The directory path of the ES instance installation
c2mon.server.elasticsearch.home= #default /usr/local/elasticsearch

# The time based indices creation strategy
# Strategies Options:
# a) Daily [D or d]
# b) Weekly [W or w]
# c) Monthly [M or m]   <- default value
c2mon.server.elasticsearch.config.index.format=m

# The prefix of each index (one per each c2mon core entity)
c2mon.server.elasticsearch.prefix.index=tag_
c2mon.server.elasticsearch.prefix.supervision=supervision_
c2mon.server.elasticsearch.prefix.alarm=alarm_

# Indices configuration
c2mon.server.elasticsearch.config.index.replica=0
c2mon.server.elasticsearch.config.index.shards=5

# Bulk configuration
c2mon.server.elasticsearch.config.bulk.actions=5600
c2mon.server.elasticsearch.config.bulk.size=5
c2mon.server.elasticsearch.config.bulk.flush=10
c2mon.server.elasticsearch.config.bulk.concurrent=1

# ===================================================================

```
