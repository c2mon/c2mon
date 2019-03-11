---
layout:   post
title:    Getting Started
summary:  A Hello World guide to get C2MON up and running with some sample data.
---

# Running the server

The first thing to do is to get a C2MON server up and running. The server is a standalone application that receives data from acquisition processes (DAQs) and pushes it to client applications.

You can run a server by downloading and executing a tarball distribution, or by running a Docker image.

For convenience and fast development the server is by default starting with:

- embedded [ActiveMQ v5.15.2](http://activemq.apache.org/) JMS broker,
- embedded [Elasticsearch v5.6.0](https://www.elastic.co/products/elasticsearch) node,
- embedded [HSQLDB v2.3.2](http://hsqldb.org/) database.

**For production usage we strongly advice to run all these products separately!** In particular Elasticsearch is very resource hungry and can cause memory problems when running for longer together with C2MON in the same JVM.  

Alternatively to HSQLDB you can also use [MySQL v5.1.38](https://www.mysql.com/) or [Oracle v11.2.0.3](http://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html).


## Running the tarball distribution

To run the C2MON tarball distribution you need at least Java 1.8 installed on your machine.

The [C2MON server distribution tarball](https://nexus.web.cern.ch/nexus/#nexus-search;gav%7Ecern.c2mon.server%7Ec2mon-server%7E%7Etar.gz%7E) can be downloaded from [here](https://nexus.web.cern.ch/nexus/content/groups/public/cern/c2mon/server/c2mon-server/).

We recommend to always use the latest stable version listed in the [CHANGELOG](/about/CHANGELOG/) file.

Extract the tarball on your local file system and change into the `c2mon-server-1.8.xx/bin/` directory.

> **Please note!** <br>
The scripts provided with the tarball are Linux bash scripts. We recommend this environment also for production usage. Windows users should use [Docker](#using-the-docker-image) instead.

To start the server execute the following script:

```
$ ./c2mon.sh start

Starting a C2MON server:     [  OK  ]
```

For the help page just run the script without any options:

```
$ ./c2mon.sh

Usage: ./c2mon.sh {start|stop|restart|status|run} [-d|--debug] [-r|--recover]

start   - Starts the C2MON server on this host, if it is not running.
stop    - Stops the C2MON server on this host, if it is running.
          If a gentle shutdown fails, the process is killed after 20 seconds.
restart - Restarts the C2MON server.
status  - Checks the status (running/stopped) of the C2MON server.
run     - Starts the C2MON server in the foreground without logging to a file.

-d, --debug
         Allows attaching a remote debugger to the C2MON Java process.
-r, --recover
         Recover after a server crash.
```

It is also advisable to take a look into the `log/c2mon.log` file. When C2MON starts successfully you should see the following INFO message:

```
... [main] cern.c2mon.server.ServerStartup : C2MON server is now initialised
```


## Using the Docker image

We push a Docker container of the server to the [CERN Docker registry](https://gitlab.cern.ch/c2mon/c2mon/container_registry).
To run the image:

```bash
docker run --rm --name c2mon -it -p 0.0.0.0:1099:1099 -p 0.0.0.0:9001:9001 -p 0.0.0.0:61616:61616 -p 0.0.0.0:9200:9200 \
  gitlab-registry.cern.ch/c2mon/c2mon
```


## Changing default configuration
C2MON comes with reasonable defaults for most settings.
Before you get out to tweak and tune the configuration, make sure you understand what are you trying to accomplish and the consequences.

The primary way of configuring a server is via the [`conf/c2mon-server.properties`](https://github.com/c2mon/c2mon/blob/master/c2mon-server/distribution/tar/conf/c2mon-server.properties) configuration file, which is delivered with the tarball.
It contains the most important settings and their default values you may want to change for your environment.

The properties listed in the file can just as well be set as Java system properties with the `-D` option.


## Persisting C2MON data in an Oracle database

Note that Oracle database drivers (unlike MySQL and HSQLDB) are not distributed with C2MON. In order to persist data in an Oracle database, you must [download the Oracle JDBC drivers](http://www.oracle.com/technetwork/database/features/jdbc/index.html) and mount them as volumes in your Docker container.
For example, if the JDBC driver libraries ```ojdbc.jar``` and ```orai18n.jar``` are available in the current folder, you can run :

```bash
docker run --rm --name c2mon -it -p 0.0.0.0:1099:1099 -p 0.0.0.0:9001:9001 -p 0.0.0.0:61616:61616 -p 0.0.0.0:9200:9200 \
  -v `pwd`/ojdbc.jar:/c2mon-server/lib/ojdbc.jar:z -v `pwd`/orai18n.jar:/c2mon-server/lib/orai18n.jar:z \
  gitlab-registry.cern.ch/c2mon/c2mon
```


# Publishing sample data

Once the server is running it's time to send some metrics to it!

You can use one of several [pre-provided acquisition processes (DAQs)](https://github.com/c2mon?utf8=%E2%9C%93&q=c2mon-daq) and configure them to grab data from your equipments and services. Alternatively, you can of course write your own DAQ to publish any type of metric using the [DAQ API](/user-guide/daq-api).

The easiest way to get a first _Hello World_ scenario with C2MON is to make use of the [hostmetrics DAQ](https://github.com/c2mon/c2mon-daq-hostmetrics), which uses a simple library to monitor and publish metrics about the host machine on which the DAQ runs. This will help you to quickly explore the core features of the system.

Again, you can download and execute a tarball or run a Docker image to get it up and running.

## Running the hostmetrics DAQ tarball distribution

**Coming soon!**

## Using the hostmetrics DAQ Docker image

```bash
docker run --rm --name daq-hostmetrics -it --net=host -e "C2MON_DAQ_JMS_URL=failover:tcp://localhost:61616" \
  gitlab-registry.cern.ch/c2mon/c2mon-daq-hostmetrics bin/C2MON-DAQ-STARTUP.jvm -f P_HOST01
```


# Start C2MON Web User Interface

Once you have a server running and a DAQ process publishing data to the server, it's time to actually look at how we're going to get the data back out again in order to do something useful with it.

C2MON comes with a web-based application called [c2mon-web-ui](http://github.com/c2mon/c2mon-web-ui), that you can use to do various things such as view metric history, monitor alarms, inspect configurations and view statistics about C2MON clusters.


## Running the tarball distribution (Linux only)

The C2MON Web UI distribution tarball can be downloaded from [CERN Nexus repository](https://nexus.web.cern.ch/nexus/content/groups/public/cern/c2mon/web/c2mon-web-ui/).

Extract the tarball on your local file system and change into the `c2mon-web-ui-0.0.xx/bin/` directory.

To start c2mon-web-ui server execute the following script:

```shell
$ ./c2mon-web-ui.sh start

Starting C2MON web UI:     [  OK  ]
```

You can now consult the C2MON Web User Interface on http://localhost:8080/c2mon-web-ui/

## Using the Docker image

We push the c2mon-web-ui Docker images to the [CERN Docker registry](https://gitlab.cern.ch/c2mon/c2mon-web-ui/container_registry).
To run the image:

```bash
docker run --rm --name web-ui -ti --link c2mon:c2mon -p 0.0.0.0:8080:8080 gitlab-registry.cern.ch/c2mon/c2mon-web-ui
```

You can now consult the C2MON Web User Interface on http://localhost:8080/c2mon-web-ui/

<!-- ## Inspecting the data

**TODO**: write a brief section on how to find and interpret metrics using the web interface
 -->

# What's next?
What you achieved with this tutorial is a Hello World demonstration of C2MON to understand the [core concepts](/core-concepts) of the framework.

However, in order to use C2MON for your own use case you have now work on connecting to your data sources. This will require to get more familiar with the C2MON Data Acquisition (DAQ) layer.
Maybe you can use some of the existing [Open Source DAQs](https://github.com/c2mon?utf8=%E2%9C%93&q=c2mon-daq), but most probably you want to write your own DAQ process. Therefore, you should read as next about the [DAQ API](/user-guide/daq-api).
