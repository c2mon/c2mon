# C2MON ❤️ Kubernetes

A production-grade C2MON deployment requires rolling out multiple and complex elements, such as messaging brokers, load balanced web servers, data history databases, configuration databases and data acquisition processes (DAQ).

This document provides deployment guidelines and a quickstart applicable to a Kubernetes-based environment, compatible with Openstack, Openshift, Docker Swarm (via kompose) and other cloud container services.

## Nomenclature

C2MON defines the following terms :
- Tag: logical unit of information typically representing a coherent set of data (for example, a temperature reading). A tag can be atomic (single value), or complex (key value associations).
- DAQ Process: A data acquisition process is in charge of continuously collecting information (tags) and forwarding them to the C2MON messaging queues.

## Overview

Deploying C2MON on a cloud infrastructure involves the following architectural layers.

|Layer Name|Purpose  |
|:---|:---|
|Data Acquisition (DAQ) Layer|Processes in this layer collect information through various protocols (OPC-UA, DIP, CMW, SSH, HTTP, S7) and forward them to the Messaging layer.
|Messaging Layer|The messaging layer aggregates tag updates, applies tag data validation and enforces alerting. It also provides caching for increased performance. C2MON uses Apache ActiveMQ for its messaging needs.
|Archiving Layer|Collects information and indexes it (typically by timestamp) to provide fast history querying and long term data storage.|
|Publishing Layer|C2MON can in turn republish tag updates, validity status or aggregation results through standard protocols (DIP, AQMP, HTTP, WebSockets).|
|Presentation Layer|This layer formats data updates into graphical operation panels and offers historical data access. This layer represents the data's intended destination.|

![Screenshot](/img/user-guide/c2mon-layers-overview.png) 

## Running the server

### Prerequisites
This tutorial assumes you have access to a cluster supporting Kubernetes API v1 and that you have one or more working [DNS resolvers in your cluster](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/)


### Setup
Getting a  C2MON server cluster up and running is super simple! Choose one of the options below :
- You can manually import [the individual yaml files](https://github.com/c2mon/c2mon/tree/master/c2mon-server/distribution/kubernetes) for fine grained control, 
- You can import the [single full yaml](https://raw.githubusercontent.com/c2mon/c2mon/master/c2mon-server/distribution/kubernetes/c2mon-kube-single.yaml) for ease, 
- or just run the following  
```shell
kubectl -f https://raw.githubusercontent.com/c2mon/c2mon/master/c2mon-server/distribution/kubernetes/c2mon-kube-single.yaml
```
 
The cluster creates the namespace 'c2mon-dev' and starts the following pods:

- C2MON Server,
- [C2MON hostmetrics DAQ](https://github.com/c2mon/c2mon-daq-hostmetrics),
- [C2mon Web UI](http://github.com/c2mon/c2mon-web-ui),
- [ActiveMQ v5.14.3](http://activemq.apache.org/) JMS broker,
- [Elasticsearch v5.6.0](https://www.elastic.co/products/elasticsearch) single cluster node,
- [MySQL 5.7.15](https://www.mysql.com/) database.
- [Grafana 6.1.2](https://grafana.com/) dashboards UI

_Additionally, there are a number of deployments that orchestrate and services that discover these pods_  

Alternatively to MySQL you can also use [HSQL](http://hsqldb.org/) or [Oracle](http://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html).

> Please note:
All the aforiemented pods, use C2MON images from [CERN's official DockerHub](https://hub.docker.com/u/cern). These images are tuned for a test environment and in some cases (e.g ElasticSearch) will actively __crash on startup__ if they detect themselves being used in a production environment or configuration.

> Please note:
Due to startup sequencing the first instances of c2mon-server or c2mon-web may crash due to unavailable services (e.g if Elasticsearch is taking a long time to load). This is ok; the deployment will regenerate the container after a few seconds and it will pick up the service.

Each container writes to its own _System.Out_. You can peek into that by using:
```shell
kubectl logs -f POD-NAME
```

When C2MON starts successfully you should see the following INFO message:

```
... [main] cern.c2mon.server.ServerStartup : C2MON server is now initialised
```

### Changing default configuration
C2MON comes with reasonable defaults for most settings.
Before you get out to tweak and tune the configuration, make sure you understand what are you trying to accomplish and the consequences.

The primary way of configuring a server is via the [`conf/c2mon-server.properties`](https://github.com/c2mon/c2mon/blob/master/c2mon-server/distribution/tar/conf/c2mon-server.properties) configuration file, which is delivered with the tarball.
It contains the most important settings and their default values you may want to change for your environment.

Additionally, the properties listed in the file can just as well be set as Java system properties with the `-D` option.

However, for development and test Kubernetes environments the simplest way often is to use environment variables on the container.

#### Persisting C2MON data in an Oracle database

Note that Oracle database drivers (unlike MySQL and HSQLDB) are not distributed with C2MON. In order to persist data in an Oracle database, you must [download the Oracle JDBC drivers](http://www.oracle.com/technetwork/database/features/jdbc/index.html) and mount them as volumes in your C2MON server container.


## Publishing sample data

The C22MON cluster comes preconfigured with a [Hostmetrics DAQ](https://github.com/c2mon/c2mon-daq-hostmetrics) which uses a simple library to monitor and publish metrics about the host machine on which the DAQ runs. 

_Implementation Note: Due to VM Driver differences this DAQ behaves differently across different Kubernetes environments - it may log the container's virtual resources, or it may log the host machine's resource values!_


## Consult C2MON Web User Interface

The C2MON cluster comes with a web-based application called [c2mon-web-ui](http://github.com/c2mon/c2mon-web-ui), that you can use to do various things such as view metric history, monitor alarms, inspect configurations and view statistics about C2MON clusters.

This is by default running in: http://your_cluster_ip:31322/c2mon-web-ui/

## Grafana dashboards

The C2MON cluster also comes with a Grafana instance, preconfigured with the MySQL datasource and a dashboard for consulting the data provided by the hostmetrics DAQ.

Check it out in http://your_cluster_ip:31323/ and select the **hostmetrics** dashboard.

To perform any changes visit http://your_cluster_ip:31323/login, use `admin/admin` and then change the password if desired.

<!-- ### Inspecting the data

**TODO**: write a brief section on how to find and interpret metrics using the web interface
 -->


## What's next?
What you achieved with this tutorial is a Hello World demonstration of C2MON to understand the [core concepts](/core-concepts) of the framework.

However, in order to use C2MON for your own use case you have now work on connecting to your data sources. This will require to get more familiar with the C2MON Data Acquisition (DAQ) layer.
Maybe you can use some of the existing [Open Source DAQs](https://github.com/c2mon?utf8=%E2%9C%93&q=c2mon-daq), but most probably you want to write your own DAQ process. Therefore, you should next read about the [DAQ API](/user-guide/daq-api).