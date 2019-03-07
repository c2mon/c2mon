# Supervision

Describes the mechanism by which C2MON is able to monitor (or supervise) parts of the system.

---

To supervise a part of the system is to maintain information about its health, and to react when problems are detected with it.

As mentioned in Core Concept: Tags, C2MON provides three types of ControlTag (the StatusTag, the AliveTag and the CommFaultTag).
These are the tags which represent the health state of an entity within the system.


## The AliveTag

The AliveTag acts like a heartbeat for a particular entity.
If the AliveTag is not received regularly (within a configurable time interval) then C2MON assumes that the entity is not running and that there is a major problem.

## The CommFaultTag

The CommFaultTag can be sent at any time, and indicates that there is some kind of communication problem with the entity.
The CommFaultTag has a Boolean value; true indicates that there is no problem, false indicates a problem.

> **Please note!**

>The false alarm behavior concept comes from controlling logic, were an alarm is raised if the current (1) gets interrupted (0).

## The StatusTag

The StatusTag represents the overall health of the supervised entity, and is derived from the AliveTag and the StatusTag. If either the AliveTag expires or the CommFaultTag indicates a problem, then the StatusTag is set accordingly to a String value representing its state. There are several values that the StatusTag can have:

* **RUNNING** indicates that the entity is running normally.
* **DOWN** indicates that the entity is not running, either due to an expired AliveTag or a received CommFaultTag.
* **STARTUP** indicates that the entity is starting up.
* **STOPPED** indicates that the entity was shut down cleanly.
* **UNCERTAIN** indicates the server is not sure of the status of the entity, for instance after a period of server downtime.

The following diagram shows how the AliveTag and CommFaultTag influence the StatusTag:

![supervision-tags]({{site.baseurl }}/assets/img/overview/supervision-tags.png)

## Supervisable Entities

The entities that are supervisable within C2MON are the Process, the Equipment and the SubEquipment (which were introduced in Core Concept: Process/Equipment).
This means that Processes, Equipments and SubEquipments can all be independently supervised with their own ControlTags.

### Supervision Flow: AliveTags

The following diagram shows how the supervision flow for an AliveTag works.
As you can see, the AliveTag is checked periodically for expiration.
If it has expired, it triggers a change in the StatusTag and the CommFaultTag.
If it is received normally, it also triggers a reset of the StatusTag and CommFaultTag back to normal.

![Screenshot]({{site.baseurl }}/assets/img/overview/alivetag-flow.png)


### Supervision Flow: CommFaultTags

The following diagram shows how the supervision flow for a CommFaultTag works.
When a CommFaultTag is received, its value is checked. If the value is false (which indicates a problem) then the StatusTag is changed to DOWN and the AliveTag is invalidated. If the value is true (which indicates no problem) then the StatusTag is changed to RUNNING and the AliveTag becomes valid.

![Screenshot]({{site.baseurl }}/assets/img/overview/commfaulttag-flow.png)
