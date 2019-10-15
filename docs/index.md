---
layout:   post
title:    C2MON
summary:  The Open Source solution to your data and process monitoring needs
---

The CERN Control and Monitoring Platform (C2MON) is a heterogeneous data acquisition framework with configuration, persistence, historical browsing, control and alarm functionalities. It has been developed for CERN’s demanding infrastructure monitoring needs and is based on more than 10 years of experience with the Technical Infrastructure Monitoring (TIM) systems at CERN.

It comes with a simple and intuitive data subscription API with integrated history browsing capabilities that can be used to form the basis for industrial dashboards and other graphical monitoring applications. Powerful data filtering and configuration mechanisms help fine tune data flow and prevent data burst situations.

---

## Enjoy high-availability

* Multi server configuration allows for rolling updates and provides a failover mechanism
* Internal communication over [ActiveMQ](http://activemq.apache.org/) JMS ensures that none of your messages get lost
* Go fast, go safe; Operational database independence through [JCACHE (JSR 107)](https://jcp.org/en/jsr/detail?id=107) in-memory storage solution
* The results and experience of years in CERN production systems at your fingertips

---

## Scale at all layers

* Add as many load-balanced servers as you need
* Run unlimited Data Acquisition processes
* Scale-out your ActiveMQ JMS middleware
* Increase In-Memory storage e.g. using [Terracotta Server Open Source Kit](http://www.terracotta.org/downloads/open-source/catalog)

---

## Customise with no hassle

* Use your data with [Elasticsearch](https://www.elastic.co/), [Oracle DB](https://www.oracle.com/database/), [MySQL](https://www.mysql.com/), [HSQL](http://hsqldb.org/) or anything else you choose
* Deliver value to your end users with intuitive [Grafana dashboards](https://grafana.com/)
* Tweak a server based on Java and [Spring](http://projects.spring.io/spring-framework/)
* Write your own [Data Acquisition Modules](docs/user-guide/daq-api/daq-module-developer-guide.md)
* Extendable Java and REST client API available (check a sample below)

{% include video.html url="hxo8K0lbqos" %}
