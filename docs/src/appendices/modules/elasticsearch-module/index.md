<a id="_elasticsearch_module"></a>
# Elasticsearch Module
Enables the ability to use [Elasticsearch](https://www.elastic.co/products/elasticsearch) as _timeseries data storage_ for C2MON metrics.

> Not familiar with the Elasticsearch concepts?
>
> check the [documentation](https://www.elastic.co/guide/index.html) to get started.


## Introduction
This persistence module provides the functionality to the c2mon-server to communicate with an Elasticsearch instance.

Thought this communication it becomes possible to store the continuously produced data, from a configured and running C2MON environment. 
The data, provided by the internal message bus system, are in the form of the core C2MON entities, like [Tags](/core-concepts/tags), [Supervision](/core-concepts/supervision) and [Alarms](/core-concepts/alarms).  


## Usage
Effectively expose, search and visualize the produced data of a C2MON environment, with the help of a state of the art
document search engine, like **Elasticsearch** and it's ecosystem, that provides solution out of the box, in order 
to achieve a more effective monitoring of your processes and equipment configuration.


## Core Concepts
This Module is part of the c2mon-server core. That means that it shares functionality and implements
the core concepts of the C2MON itself. The actions are based on continuously collecting and storing, produced metrics.
In other words, the basic operations, that are also externally configurable are:

* Detection and communication management with the Elasticsearch instance
* Creation of **[time based indices](https://www.elastic.co/guide/en/elasticsearch/guide/current/time-based.html)**
* Generation of persistence mapping schema for the core C2MON entities
* Persistence of the core C2MON entities.


## Detection and communication
The C2MON Elasticsearch module is build with [Spring](https://spring.io/) with Elasticsearch [Java API](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html) integration.
This means that the detection and communication goes directly on the node level using the reliable and extreamely fast TCP [TransportConnector](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-transport.html),
provided by Elasticsearch. That connector is used for all the module's operations, _including single and bulk actions_.


## Time based indices
After the cluster/node discovery part, the module directly creates the indices for the C2MON core objects.
Those indices are created based on 3 creation pattern options:

| Option    |   Value   |    Pattern    |        Example            |
|-----------|-----------|---------------|---------------------------|
| Daily     |   D or d  | yyyy-MM-dd    | ```tag_2016-02-21```      |
| Weekly    |   W or w  | yyyy-'W'ww    | ```alarm_2016-'2'27```    | 
| Monthly   |   M or m  | yyyy-MM       | ```supervision_2016-02``` |

> By _default_, the module uses the **monthly** based index creation pattern.

It can be changed to the other options, by providing in the ```c2mon.server.eslog.config.index.format``` property, one of the above values.


## Mapping of the core entities
In order to store an entity as a document to Elasticsearch, there is a need to provide, the document [mapping](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html) schema.
That Elasticsearch mapping schema, or just mapping, defines how the document and it's fields should be processed and treated by elastic.
 
> One of the core features of C2MON's Elasticsearch module, is the creation of mappings, for all of it's metric entities.

That's why after the initialization of the platform's environment, always with ES module enabled, everything is configured and ready to persist the produced metrics.

### Mapping schema
As mentioned, this module, creates mappings for the C2MON produced metric entities, like [Tags](/core-concepts/tags), [Supervision](/core-concepts/supervision) and [Alarms](/core-concepts/alarms).
Their mappings are presented bellow.
 
#### Tags
[Tags](/core-concepts/tags) are separated by the type of value that they enclose. In other words, the module handles tags of different value data type. That's why, only for the Tag entity,
there is a need to create multiple Elasticsearch mappings. 

**Example:** Mapping for Tag of type spring:

```
  tag_string: {
    properties: {
      c2mon: {
        dynamic: "false",
        properties: {
          daqTimestamp: {
            type: "date",
            format: "epoch_millis"
          },
          dataType: {
            type: "string",
            index: "not_analyzed"
          },
          equipment: {
            type: "string",
            index: "not_analyzed"
          },
          process: {
            type: "string",
            index: "not_analyzed"
          },
          serverTimestamp: {
            type: "date",
            format: "epoch_millis"
          },
          sourceTimestamp: {
            type: "date",
            format: "epoch_millis"
          },
          subEquipment: {
            type: "string",
            index: "not_analyzed"
          }
        }
      },
      id: {
        type: "long"
      },
      metadata: {
        type: "nested",
        dynamic: "true"
      },
      name: {
        type: "string",
        index: "not_analyzed"
      },
      quality: {
        dynamic: "false",
        properties: {
          status: {
            type: "integer"
          },
          statusInfo: {
            type: "string",
            index: "not_analyzed"
          },
          valid: {
            type: "boolean"
          }
        }
      },
      timestamp: {
        type: "date",
        format: "epoch_millis"
      },
      type: {
        type: "string",
        index: "not_analyzed"
      },
      unit: {
        type: "string",
        index: "not_analyzed"
      },
      valueDescription: {
        type: "string",
        index: "not_analyzed"
      },
      valueString: {
        type: "string"
      }
    }
  }
```

> Tag mappings of other data types deffer only at the mapping name declaration,
> ```ex. tag_boolean: { ... }``` and field value types ```ex. valueBoolean: { type: "string" }```


#### Supervision
[Supervision events](/core-concepts/supervision) are the produced data part of the monitoring and health reporting of the C2MON's environment.
They contain straightforward messages and status reports, so the module, uses a universal mapping structure to store the incoming events.  

```
  supervision: {
    properties: {
      entityId: {
        type: "long"
      },
      entityName: {
        type: "string"
      },
      eventTime: {
        type: "long"
      },
      id: {
        type: "long"
      },
      message: {
        type: "string",
        index: "not_analyzed"
      },
      status: {
        type: "string",
        index: "not_analyzed"
      },
      statusName: {
        type: "string"
      },
      timestamp: {
        type: "date",
        format: "epoch_millis"
      }
    }
  }
```


#### Alarms
[Alarms](/core-concepts/alarms) are at the alert and notify part of C2MON's logic, and are produced with smaller frequency that the tags.
In oppose with tags, alarms are not separated by data type, as they just refer to the tag by it's id.
That's why the module, once again, uses a universal mapping structure for all of them.

```
  alarm: {
    properties: {
      active: {
        type: "boolean"
      },
      activeNumeric: {
        type: "double"
      },
      activity: {
        type: "string",
        index: "not_analyzed"
      },
      alarmId: {
        type: "long"
      },
      faultCode: {
        type: "integer"
      },
      faultFamily: {
        type: "string",
        index: "not_analyzed"
      },
      faultMember: {
        type: "string",
        index: "not_analyzed"
      },
      info: {
        type: "string",
        index: "not_analyzed"
      },
      metadata: {
        type: "nested",
        dynamic: "true"
      },
      priority: {
        type: "integer"
      },
      serverTimestamp: {
        type: "date",
        format: "epoch_millis"
      },
      tagId: {
        type: "long"
      },
      timeZone: {
        type: "string"
      }
    }
  }
```


### Getting started
Follow the getting started guide [here](/appendices/modules/elasticsearch-module/getting-started.md).



