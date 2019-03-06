# Introduction to C2MON

A high-level introduction to the concepts of the C2MON platform.

---

After reading this chapter, you should have a good idea about how data flows through C2MON; from the raw data coming from sensory data sources, all the way up to user applications that make use of that data.

To begin with, we will describe the overall architecture of the platform.
Then, in later sections, we will dive deeper into each layer and understand the core concepts that are needed to work with the system.


## What is C2MON?

The Controls and Monitoring Platform C2MON is a toolkit written in Java for building highly complex, distributed and fail-safe monitoring solutions.
Therefore, it uses a 3-tier architecture with the possibility to run multiple servers at the same time, which allows applying patches and updates without affecting the service availability.
C2MON manages centrally the configuration of all running Data Acquisition (DAQ) Processes and handles their reconfiguration online without any downtime or potential data loss.
The modular architecture builds on a core system that aims to be reusable for multiple monitoring scenarios, while keeping each instance as lightweight as possible.

C2MON comes with a modern looking web interface that provides many core functionalities for data browsing, administration and analysis.



## Use Cases

Since C2MON is essentially a heterogeneous data acquisition framework with configuration, persistence, historical browsing, control and alarm functionalities, it can be suitable for building many different types of system.

For example, it is used internally at CERN as an industrial SCADA system; as a network monitoring system; as a central alarm aggregation service; and as a general-purpose data proxy.

Reuse for instance C2MON within your project as open SCADA middleware whilst focusing on client application development.
Some more examples are listed below.

C2MON can be used:

* To acquire and store data from different type of systems,
* To build up a simple or highly distributed or cloud based monitoring and control solution,
* To realise high-availability solutions with on-line reconfiguration,
* To share data from hundred of thousands or even millions of data sensors with multiple types of applications,
* To cluster data acquisition with event notifications and to execute background tasks (e.g. rule or alarm evaluations),
* To centrally manage the subscription configuration from different type of systems,
* To define structured objects (Devices) on top of your data acquisition that can be re-used on the client tier,
* As data analysis framework,
* As simple data proxy,
* As filtering system to reduce for instance the noise of analogue sensors,
* As data recorder,
* As data history player for client applications to replay for instance highly complex synoptic dashboards.


## Architectural Overview

The C2MON platform uses a 3-tier architecture, as displayed in the diagram below.

![c2mon-layer-overview](/assets/img/overview/c2mon-layer-overview.png)

**Data Acquisition (DAQ) Layer**

The data acquisition (DAQ) layer provides solutions for acquiring data from a number of protocols/hardware.
For detailed documentation about the DAQ layer, see C2MON Data Acquisition.

**Server Layer**

The server architecture is designed around a (distributed) cache, which keeps for each configured sensor the latest value In-Memory.
Internally, the server is broken down into a number of modules, including the possibility to write optional modules providing extra functionalities.
The technology stack is based on Java Spring container and is designed to run in a clustered setup.

**Client Layer**

Communication with the client layer is done via a provided C2MON Client API, which is documented here: [Client API](user-guide/client-api).

## Core Concepts

To learn more about the core concepts of the C2MON platform you should continue as follows:

To get started, take a look at the concept of [process/equipment](process-equipment) and [tags](tags).

More advanced topics include [alarms](alarms) and [class/device/property](class-device-property).

> **Please note!**

>These sections describe C2MON data configuration only from a conceptual point of view.
>To learn how to actually configure monitoring data, please please have a look at the User Guide section.
