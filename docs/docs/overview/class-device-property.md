---
layout:   post
title:    Class/Device/Property
summary:  This concept provides a different, more object-oriented way of structuring monitoring data coming from C2MON DAQs.
---

The Process/Equipment structure is the original concept within C2MON, and has existed from the very beginning.
The Class/Device/Property structure is much newer, and was added as an alternative to the Process/Equipment structure.

In fact, the Class/Device/Property structure cuts across the Process/Equipment structure.
It is simply an abstraction layer provided by the server to client applications.

Please contact for now [c2mon-support@cern.ch](mailto:c2mon-support@cern.ch) to learn how to configure it in C2MON, as the actual configuration is not yet described.


# Class/Device/Property Structure

The Class/Device/Property is conceptually simple to understand:

* A **Device Class** is a template for a particular type of Device to be monitored. A Class has a number of Property templates associated to it.
* A **Device** is a concrete instance of a Device Class, and has concrete instances of the Properties defined in its parent Device Class.
* A **Property** is a data point from the monitored Device. Usually, the property is a DataTag.

The following diagram shows a Device Class with three concrete Devices. Note that the Device does not have to provide instances for all properties.

![Screenshot]({{site.baseurl }}/assets/img/overview/class-device-property.png)


# Constraints

* A Device can only belong to a single Device Class (multiple inheritance not supported).
* A Property belongs to only one Device.
