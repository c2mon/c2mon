---
layout:   post
title:    One platform for multiple monitoring scenarios
summary:  Examples of C2MON in action
---

One of the major CERN monitoring systems is based on C2MON and contributing features to the platform. Come and join our community and see how C2MON will meet also your monitoring requirements.

{% include video.html url="OKisSUCCWLQ" %}
> Video demonstrating TIM in action!

---

# TIM - Technical Infrastructure Monitoring

TIM is a 24/7 service at CERN to supervise and control a variety of infrastructure spread across the CERN sites.

TIM provides different types of Java and Web applications allowing operators for instance reacting to alarms, supervising maintenance operations, or monitoring access to the accelerator complex.

The main application is called "TIM Viewer", a generic Dashboard viewer for animating data coming from C2MON. Currently, the TIM Viewer hosts +200 Dashboards used by different user groups at CERN.

![Tim Viewer: a client application build on top of C2MON](assets/img/about/tim_viewer_1.png)

> Views showing the access status to the LHC tunnel and a CERN water Monitoring station ( SPS Accelerator ).

![Water monitoring station screen as shown on the Tim Viewer](assets/img/about/tim_viewer_2.png)

__TIM in numbers:__

* ~200'000 data tags, ~170'000 alarms, ~ 1'000 rules
* Up to 400 million raw data values per day
* After filtering ~ 2.2 million values are treated and stored by the C2MON server

---

# Your use case?

Since C2MON is essentially a heterogeneous data acquisition framework with configuration, persistence, historical browsing, control and alarm functionalities, it can be suitable for building many different types of system.

For example, it is used internally at CERN as an industrial SCADA system; as a network monitoring system; as a central alarm aggregation service; and as a general-purpose data proxy.

We hope that makes C2MON also interesting for your business, since you could reuse it as an open SCADA middleware whilst focusing on client application development.
C2MON could be used :

* To acquire and store data from different type of systems,
* To build up a simple or highly distributed or cloud based SCADA solution,
* To realise high-availability solutions with on-line reconfiguration,
* To share data from hundreds of thousands or even millions of data sensors with multiple types of applications,
* To cluster data acquisition with event notifications and to execute background tasks (e.g. rule or alarm evaluations),
* To centrally manage the subscription configuration from different type of systems,
* To define structured objects (Devices) on top of your data acquisition that can be re-used on the client tier,
* To create a data analysis framework,
* As simple data proxy,
* As filtering system to reduce for instance the noise of analogue sensors,
* As data recorder,
* As data history player for client applications to replay for instance highly complex synoptic dashboards.
