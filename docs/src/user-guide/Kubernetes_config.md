# C2MON Kubernetes Configuration

C2MON ❤️ Kubernetes

A Hello World guide to get C2MON up and running in [Kubernetes](https://kubernetes.io/) environments with sample data and a Web UI.

## Prerequisites
This tutorial assumes you have access to a cluster supporting Kubernetes API v1 and that you have one or more working [DNS resolvers in your cluster](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/) 

## Running the server

The first thing to do is to get a C2MON server cluster up and running. You can manually import [the individual yaml files](https://github.com/c2mon/c2mon/tree/master/c2mon-server/distribution/kubernetes), the [single full yaml](https://raw.githubusercontent.com/c2mon/c2mon/master/c2mon-server/distribution/kubernetes/c2mon-kube-single.yaml), or just run the following:  
```shell
kubectl -f https://raw.githubusercontent.com/c2mon/c2mon/master/c2mon-server/distribution/kubernetes/c2mon-kube-single.yaml
```
 
The cluster starts off with the following pods:

- C2MON Server,
- [C2MON hostmetrics DAQ](https://github.com/c2mon/c2mon-daq-hostmetrics),
- [C2mon Web UI](http://github.com/c2mon/c2mon-web-ui),
- [ActiveMQ v5.15.2](http://activemq.apache.org/) JMS broker,
- [Elasticsearch v5.6.0](https://www.elastic.co/products/elasticsearch) single cluster node,
- [MySQL 5.7.15]( ) database.

_Additionally, there are a number of deployments that orchestrate and services that discover these pods_  

Alternatively to MySQL you can also use [HSQL](http://hsqldb.org/) or [Oracle](http://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html).

> Please note:
All the aforiemented pods, use C2MON images from [CERN's official DockerHub](https://hub.docker.com/u/cern). These images are tuned for a test environment and in some cases (e.g ElasticSearch) will actively __crash on startup__ if they detect themselves being used in a production environment.

<!--- Update
It is also advisable to take a look into the `log/c2mon.log` file. When C2MON starts successfully you should see the following INFO message:

```
... [main] cern.c2mon.server.ServerStartup : C2MON server is now initialised
```
--->

## Architecture

// Some diagram showing the different stuff available?

### Changing default configuration
C2MON comes with reasonable defaults for most settings.
Before you get out to tweak and tune the configuration, make sure you understand what are you trying to accomplish and the consequences.

The primary way of configuring a server is via the [`conf/c2mon-server.properties`](https://github.com/c2mon/c2mon/blob/master/c2mon-server/distribution/tar/conf/c2mon-server.properties) configuration file, which is delivered with the tarball.
It contains the most important settings and their default values you may want to change for your environment.

The properties listed in the file can just as well be set as Java system properties with the `-D` option.

// ENV variables?
// Kube-friendly way?

#### Persisting C2MON data in an Oracle database

Note that Oracle database drivers (unlike MySQL and HSQLDB) are not distributed with C2MON. In order to persist data in an Oracle database, you must [download the Oracle JDBC drivers](http://www.oracle.com/technetwork/database/features/jdbc/index.html) and mount them as volumes in your Docker container.
For example, if the JDBC driver libraries ```ojdbc.jar``` and ```orai18n.jar``` are available in the current folder, you can run :

```bash
docker run --rm --name c2mon -it -p 0.0.0.0:1099:1099 -p 0.0.0.0:9001:9001 -p 0.0.0.0:61616:61616 -p 0.0.0.0:9200:9200 \
  -v `pwd`/ojdbc.jar:/c2mon-server/lib/ojdbc.jar:z -v `pwd`/orai18n.jar:/c2mon-server/lib/orai18n.jar:z \
  gitlab-registry.cern.ch/c2mon/c2mon
```


## Publishing sample data

Once the server is running it's time to send some metrics to it!

You can use one of several [pre-provided acquisition processes (DAQs)](https://github.com/c2mon?utf8=%E2%9C%93&q=c2mon-daq) and configure them to grab data from your equipments and services. Alternatively, you can of course write your own DAQ to publish any type of metric using the [DAQ API](/user-guide/daq-api).

The easiest way to get a first _Hello World_ scenario with C2MON is to make use of the [hostmetrics DAQ](https://github.com/c2mon/c2mon-daq-hostmetrics), which uses a simple library to monitor and publish metrics about the host machine on which the DAQ runs. This will help you to quickly explore the core features of the system.


## Start C2MON Web User Interface

Once you have a server running and a DAQ process publishing data to the server, it's time to actually look at how we're going to get the data back out again in order to do something useful with it.

C2MON comes with a web-based application called [c2mon-web-ui](http://github.com/c2mon/c2mon-web-ui), that you can use to do various things such as view metric history, monitor alarms, inspect configurations and view statistics about C2MON clusters.


You can now consult the C2MON Web User Interface on http://localhost:8080/c2mon-web-ui/

<!-- ### Inspecting the data

**TODO**: write a brief section on how to find and interpret metrics using the web interface
 -->


## What's next?
What you achieved with this tutorial is a Hello World demonstration of C2MON to understand the [core concepts](/core-concepts) of the framework.

However, in order to use C2MON for your own use case you have now work on connecting to your data sources. This will require to get more familiar with the C2MON Data Acquisition (DAQ) layer.
Maybe you can use some of the existing [Open Source DAQs](https://github.com/c2mon?utf8=%E2%9C%93&q=c2mon-daq), but most probably you want to write your own DAQ process. Therefore, you should next read about the [DAQ API](/user-guide/daq-api).