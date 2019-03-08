---
layout:   post
title:    Tags
Summary:  An overview of the C2MON Tag concept.
---

# Introduction

Almost all information in C2MON is stored and flows through the system in the form of Tags.
In this section, we describe what Tags are and what their role is in the C2MON architecture.

The Tag interface embodies the common structure of 3 data types in the C2MON architecture: the [DataTag](#the-datatag), the [ControlTag](#the-controltag) and the [RuleTag](#the-ruletag).

Tags should be thought of as follows: objects implementing this interface are destined to be updated, logged, published to clients and used in data views.
They will be able to use all the provided infrastructure of the C2MON architecture, including all the functionalities of the Client API.

Every Tag includes a [quality](#tag-quality) object that indicates whether the received value is trustable or not.
The Tag quality is hence either ok (valid) or bad (invalid).
The cause for a Tag being marked as invalid can have multiple reasons which is further discussed [below](#tag-quality).

Recall that each Equipment/Subequipment can have a set of associated Tags.
On the other hand, Tags always belong to one and only one Equipment/SubEquipment.

Tags can also have Alarms attached. to understand Alarms, please read the section about [Alarms](alarms).


# The DataTag

The DataTag is a Tag coming from an external data source.
It corresponds to a single external data point, represented by a Java primitive type (String, Float, Integer, etc.).
It is attached to an Equipment, which embodies the type of data source it originates from.

Every DataTag has to provide a so-called "hardware address", which contains the information needed for subscribing to this data point.
C2MON leaves it completely free how that hardware address is formatted.
More information about that topic is provided in [Creating a new DAQ module from scratch](/user-guide/daq-api/DAQ_module_developer_guide).


> **Please note!**

>    Many functionalities in the C2MON server are only available for data points with primitive type values, such as Floats or Strings.
    However, the core C2MON could handle more complex data type without too many changes. DataTag values are stored as simple Java Objects.


# The RuleTag

The RuleTag is a Tag internal to C2MON built on top of other Tags.
Its value is specified by a Rule expression given in a specific C2MON rule grammar.
This expression specifies how the Rule value is derived from the value of the Tags that feed into it.
Changes to these Tags will result in an update of the Rule, based on the Rule expression.

> **Please note!**

>For more information about how to create rule expressions for RuleTags, please read the chapter about the [Rule Engine](/user-guide/server/rule-engine).


# The ControlTag

The ControlTag is any Tag used by the C2MON system for monitoring/publishing internal states of the system itself (we include here Equipment as part of the system, even if it may be shared with others). In other words, ControlTags are used by C2MON for self-monitoring of some component of the system. Making use of the functionalities available to all Tags, these values can then be logged and sent to C2MON clients for self-monitoring purposes.
Alarms may also be attached to detect critical problems.

There are three types of ControlTag:

* The StatusTag;
* The AliveTag;
* The CommFaultTag.

For a detailed description of these three tags and the role they play in monitoring the status of Processes and Equipments, read the section about
[supervision](supervision).

Equipment AliveTags are a good example of a ControlTag, since they are used to monitor the status of a supervised Equipment.
In a similar way, an Equipment StatusTag is used to publish the current status (running or down) of an Equipment.


# The CommandTag

The CommandTag is a special type of Tag that is used to send commands from the client layer to the DAQ layer.
It is the only Tag that travels in this direction; all other Tags travel from the DAQ layer to the client layer.

CommandTags are usually used to send start/stop/restart commands to a DAQ process.


# Tag Quality

The Tag quality is a fundamental concept of C2MON, used to indicate the health state of the sensor communication.
In case of communication problems between the source and C2MON the Tag quality is giving very precise information about what went wrong (see table below).

Another reason why the Tag quality is absolutely needed comes from the fact that C2MON is filtering out all redundant value updates coming from the source.
But many systems do exactly that and keep on sending over and over again in regular intervals their data like heartbeats.
C2MON replaces this by the Equipment and Sub-Equipment heartbeat (see also [Supervision](supervision)).

A Tag update notification can hence be triggered by a change of the quality state.
It is therefore absolutely necessary to always check the validity of the received Tag update.

> **Please note!**

>A tag can accumulate multiple invalidation state.
>A subscribed client will be notified at every change, independent if an invalidation quality state is added or removed.

The following table lists all quality flags that a Tag can receive.

| Quality |Description |
--------- | ------------
| OK | This is the standard quality flag indicating that the value is VALID and no error occurred during data transmission. |
| UNDEFINED_TAG | A tag with this identifier/name is not known to the system. |
| JMS_CONNECTION_DOWN | The Client API lost the connection to the JMS broker. |
| SERVER_HEARTBEAT_EXPIRED | The Client API did not receive a valid server heartbeat. |
| PROCESS_DOWN | The server has marked the **DAQ** process responsible for the given Tag's data acquisition as down. This is for instance also the case when the DAQ gets restarted. This flag will be removed from all tags once the DAQ is detected as running again. |
| EQUIPMENT_DOWN | All Tag's from a given Equipment (or service) will receive this invalidation flag, in case that the communication to the underlying device got interrupted. |
| SUBEQUIPMENT_DOWN | Tag's being assigned to a Sub-Equipment will receive this quality flag e.g. in case the given sub-equipment heartbeat expires. |
| UNDEFINED_VALUE | A Tag's value cannot be determined. This is normally a value cast problem, since C2MON will always try to cast a received value to the configured value type. |
| VALUE_OUT_OF_BOUNDS | The Tag configuration allows defining value ranges. If your tag is for instance representing a percentage (%) value, it should always stay between 0 and 100. So, in case the value is not inside the defined boundaries the Tag will be flagged as INVALID and the quality object will indicate this quality code. |
| INACCESSIBLE | The server will mark all Tags as inaccessible, for which it never has received an initial value. |
| UNKNOWN_REASON | Reason for invalidity could not clearly be identified |
