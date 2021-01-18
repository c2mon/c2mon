---
layout:   post
title:    Using OPC UA
summary:  Lean to deploy your own C2MON data acquisition module for OPC UA servers.
---

After having setup a basic C2MON environment, let's go through a more advanced example. We will learn how to acquire 
data on OPC UA servers by defining Tags using the [C2MON Client Configuration Shell]({{ site.baseurl }}{% link docs/user-guide/client-api/client-configuration-shell.md %}) 
and the [OPC UA DAQ](https://github.com/c2mon/c2mon-daq-opcua). 
 
 This walk-through is a direct continuation of the [Acquiring data]({{ site.baseurl }}{% link docs/getting-started.md %}) guide. 
**The services we started previously should still be running.** 

# What is OPC UA?

OPC UA is powerful and extensive machine-to-machine standard for process control and automation. The OPC family of 
standards has been widely successful in the automation industry, and remains cutting edge in automation environments. 
OPC UA as the newest addition to these standards offers a number of advantages over its predecessors, including:

* platform independence
* high reliability and redundancy
* enhanced performance
* comprehensive security model
* improved data modeling capabilities

C2MON offers an open source data acquisition module to easily monitor data points through OPC UA. 


## Configuring C2MON via the C2MON Client Configuration Shell

In order to acquire data from the C2MON server, we need to configure DataTags. We will use the interactive 
[C2MON Client Configuration Shell]({{ site.baseurl }}{% link docs/user-guide/client-api/client-configuration-shell.md %}) 
to configure our DataTags for convenience. The shell allows us to easily create and configure DataTags on-the-fly. 
Use cases requiring more fine-grained configuration can be addressed through the Java-based Client API for subscribing 
to tags which is described in detail in [Client API]({{ site.baseurl }}{% link docs/user-guide/client-api/configuration-api.md %}).

We will use an industrial IoT sample [OPC UA server](https://github.com/Azure-Samples/iot-edge-opc-plc) by Microsoft as a data source.
The server offers different nodes generating random data and anomalies which can be explored using any OPC UA browser. 
Lets start the OPC UA server in the background. In your terminal navigate to the folder containing the docker-compose 
file and run the following command:


```bash
docker-compose up -d edge
```

The OPC UA server snippet in the docker-compose file:

```yaml
...
  # A possible data source for 'daq-opc-ua'. Tags on this server must be configured on 'c2mon' before the 'daq-opc-ua' will acquire data.
  edge:
    container_name: edge
    image: mcr.microsoft.com/iotedge/opc-plc
    ports:
      - "50000:50000"
    command: --unsecuretransport
...
```

To use the Client Configuration Shell we must configure the Processes and Equipment we want to create our new Tags under.
This works through regular expressions: A new Tag with an URI matching the `uriPattern` is created under the corresponding
Process and Equipment. We use a simple configuration for the shell of mapping all addresses starting with `opc.tcp`, the 
binary UA scheme, to one Process and Equipment:

```yaml
c2mon:
    client:
        dynconfig:
            mappings:
            -   processName: P_OPC_UA
                processID: 10000
                processDescription: OPC UA Process
                equipmentName: MS_IOT_SERVER
                equipmentDescription: OPC UA Equipment
                uriPattern: ^opc.tcp.*
```

As you can see, we specify the same Process name which we have defined in the arguments to our docker-compose file. 
This allows the C2MON server to associate the Tags with the OPC UA DAQ Process that we will start up later.  

Let's start the shell and run the following commands to subscribe to one data point on the server:

```bash
java -jar c2mon-client-config-shell-1.9.11-SNAPSHOT.jar --spring.config.additional-location=file://<PROPERTIES FILE LOCATION>
get-tags opc.tcp://edge:50000?itemName=RandomSignedInt32?tagName=RandomSignedInt&dataType=java.lang.Integer&setNamespace=2
```

As you can see from the output, the Process `OPC_UA_DAQ` and Equipment `MS_IOT_SERVER` are matched to our URI by the 
`uriPattern` expression, and are created on the server. Let's configure another tag in the shell:

```bash
get-tags opc.tcp://edge:50000?itemName=SpikeData?tagName=SpikeData&dataType=java.lang.Double&setNamespace=2;opc.tcp://edge:50000?itemName=AlternatingBoolean?tagName=AlternatingBoolean&dataType=java.lang.Boolean&setNamespace=2
```

We should now have one Process referring to one Equipment and to DataTags `RandomSignedInt`, `SpikeData` and `AlternatingBoolean`.
Note that no new Process and Equipment are created this time.

We can already have a look around our configuration and our data in the [Web UI](http://localhost:8080/c2mon-web-ui): 
You should find the new Process `P_OPC_UA` in the DAQ Process Viewer dropdown.

Let's exit the Shell by typing `exit`. 

## Starting the OPC UA DAQ

In order to collect data from these DataTags, the `P_OPC_UA` Process needs to be running as well. 
Let's leave the C2MON Client Configuration Shell and start the OPC UA DAQ in the background.

```bash
docker-compose up -d daq-opc-ua
```

The OPC UA DAQ definition in docker-compose:
```yaml
...
  # Collect data from OPC UA servers
  daq-opc-ua:
    container_name: daq-opc-ua
    image: gitlab-registry.cern.ch/c2mon/c2mon-daq-opcua
    ports:
      - "8912:8912"
      - "8913:8913"
    environment:
      - "_JAVA_OPTIONS=-Dc2mon.daq.name=P_OPC_UA -Dc2mon.daq.jms.mode=single -Dc2mon.daq.jms.url=tcp://mq:61616 -Dc2mon.client.jms.url=tcp://mq:61616"
      - C2MON_DAQ_OPCUA_TRUSTALLSERVERS=true
      - C2MON_DAQ_OPCUA_CERTIFIERPRIORITY_NOSECURITY=3
      - C2MON_DAQ_OPCUA_PORTSUBSTITUTIONMODE=NONE
      - C2MON_DAQ_OPCUA_HOSTSUBSTITUTIONMODE=SUBSTITUTE_LOCAL
      - LOG_PATH=/c2mon-daq-opcua-1.9.11-SNAPSHOT/tmp
      - SPRING_JMX_ENABLED=true
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=*,jolokia,prometheus
      - MANAGEMENT_SERVER_PORT=8912
...
```

Let's switch back to Grafana. We previously started [Grafana](http://localhost:3000) with two provisioned dashboards: 
the default Hostmetrics dashboard, and one for the OPC UA DAQ. Navigate there through Dashboards > Manage in the Grafana 
sidebar, and selecting OPC UA DAQ statistic.

We should already receive some data, especially from the panels `Tag`, `Equipment`, and `Supervision status`. 
This data comes from Elastic search, and gives DAQ- and C2MON-specific insight into our data acquisition process.

However, there is one more source of insight. The OPC UA DAQ exposes a range of general metrics as well, for example 
regarding running threads, garbage collection, or network statistics. These metrics can help us identify the source of 
errors, bottlenecks, or performance issues, and to generate insight into the operation of our Processes. 
We use the monitoring tool Prometheus to scrape the C2MON OPC UA DAQ for interesting metrics. 

Let's start Prometheus:

````bash
docker-compose up -d prometheus
````

Note that the Prometheus definition in docker-compose refers to a configuration file `prometheus.yml`. This file defines 
the OPC UA DAQ as a target to scrape for metrics.

```yaml
...
  prometheus:
    # collect relevant metrics about 'daq-opc-ua'
    container_name: prometheus
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    command: --config.file=/etc/prometheus/prometheus.yml
```

We should now see more information on our OPC UA DAQ statistic dashboard. 

## Wrapping up

This concludes our walk-through where we have seen how to easily configure C2MON to read from an OPC UA server as a data 
source. Have a look around, configure a few more data tags, and try to get some insight into the inner workings of the DAQ.
Once you're satisfied, you can stop all containers with the simple command:

````bash
docker-compose up down
````
