<a id="_elasticsearch_module_getting_started"></a>
## Getting started
Since C2MON is a project that can scan the classpath for components, the only action that is required to activate the Elasticsearch persistence module,
is to have it on the classpath, at the initialization of the C2MON environment.


### Adding the module as a dependency
To get started, include the C2MON Elasticsearch to the project dependencies.

#### Using Maven
Add the following lines to your ```pom.xml``` file:
```
<dependency>
    <groupId>cern.c2mon.server</groupId>
    <artifactId>c2mon-server-eslog</artifactId>
    <version>${c2mon-server.version}</version>
</dependency>
``` 

#### Using Gradle
Add the following line to your gradle build script:
```
compile "cern.c2mon.server:c2mon-server-eslog:c2monServerVersion"
```


### Provide the application properties
The Elasticsearch module accepts externalized configuration in the form of property values. The properties play an important role on the module'sgeneral behaviour. 

#### Embedded Elasticsearch
By default (no properties defined), the module will instantiate an embedded Elasticsearch node and use it for all of it's operations.

#### External Elasticsearch
Connecting to external Elasticsearch instance, is possible, by declaring the above properties:
```
# The hostname where the ES instance is located
c2mon.server.eslog.host=myHost

## The ES data transportation port (Not the HTTP)
c2mon.server.eslog.port=9300

# The names of node and cluster
c2mon.server.eslog.node=myNode
c2mon.server.eslog.cluster=myCluster
```


#### Bulk configuration
A more advanced configuration of the module can be achieved by defining properties for the bulk operations.


In general, all the properties can be found [here](/appendices/modules/elasticsearch-module/properties-appendix.md).