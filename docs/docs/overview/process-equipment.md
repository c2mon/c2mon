---
layout:   post
title:    Process/Equipment
summary:  A general introduction to the Process/Equipment data configuration concept within C2MON.
---

# Hierarchical structure

Processes & Equipments are used to structure the configuration of data points.
This concept forms the structural configuration and supervision base, and is indispensable for every setup.

The Process/Equipment structure is conceptually simple:

* A **Process** represents a single Data Acquisition Process (DAQ).
* An **Equipment** represents a piece of physical sensory hardware, or any other kind of data source such as a middleware service or database. A Process can have one or more Equipments.
* A **Sub-Equipment** represents a logically separate data source within an Equipment. An Equipment can optionally contain one or more Sub-Equipments.

The following diagram shows an abstract view of the concept:

![process-equipment]({{site.baseurl }}/assets/img/overview/process-equipment.png)

These three structures provide the flexibility within C2MON to model and monitor a diverse range of data acquisition needs. The following sections describe them in more detail.

## Process

The Process is a representation of a single Data Acquisition Process (DAQ), which is the actual Java process running on a machine and which communicates with the data source.
It has two main functions:

* **Monitoring the connection between the DAQ and server.** This is done via StatusTags, AliveTags and CommFaultTags (Tags and health monitoring are explained in Core Concept: Tags and Core Concept: Supervision).
* **Performing management operations on a DAQ** (such as starting, stopping and restarting it remotely) via CommandTags (Tags are explained in Core Concept: Tags)

## Equipment

The Equipment represents the actual data source which sends data to the DAQ process. The Equipment has a number of DataTags, which correspond to data points being sent from the data source.

The Equipment is also used to monitor the status of the connection between the DAQ process and the data source, again via StatusTags, AliveTags and CommFaultTags.

## Sub-Equipment

The Sub-Equipment is attached to a given Equipment and represents a sub-system of that Equipment.
The Sub-Equipment also has a number of DataTags corresponding to data points being sent from the data source.

The connection between the Sub-Equipment and its parent Equipment is also monitored via StatusTags, AliveTags and CommFaultTags.


<a id="_configuration_structure"></a>
# Configuration structure

The data structure of a DAQ configuration is strictly hierarchical.
The smallest configurable unit is a tag, which is either attached to an Equipment or Sub-Equipment.

![process-data-relation]({{site.baseurl }}/assets/img/overview/process-data-relation.png)

The tags in dark blue are mandatory tags for the Supervision and have to be generated together with every new Process, Equipment or Sub-Equipment (see also Core Concept: Supervision).
