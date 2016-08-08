# Alarms

Tags change frequently within C2MON as data from DAQ processes are received and evaluated by the system.
Sometimes, your sensory equipment may produce values that are worrying, dangerous or incorrect (for whatever reason). C2MON provides a mechanism to alert you when this happens. This page describes the Alarm mechanism.


## The Alarm

In essence, an Alarm is a declaration associated with a Tag that contains some kind of condition specifying the legal values for the Tag. When the Tag value changes, the system will check the Alarm condition. If the new value is outside the legal value range, then the Alarm will be activated and pushed to the client. The main characteristics are:

* An alarm is ALWAYS attached to one and only one Tag, which can be either a RuleTag, ControlTag or DataTag.
* A Tag can be attached to many different alarms
* Alarms are defined by a XML condition in the database


## Alarm Conditions

C2MON supports two Alarm conditions out-of-the-box. These conditions are:

* **ValueCondition:** if the Tag value equals a specific value, the alarm is triggered.
* **RangeCondition:** if the Tag value falls outside the given range, the alarm is triggered.

> **Please note!**

>It is possible to define custom Alarm condition types, but that is out of scope of this section.

The diagram below shows an example how Alarm condition can be assigned to results of rules or tags.

![Screenshot](/images/overview/alarm-evaluation-example.png =500x)