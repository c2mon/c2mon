# What is C2MON?

The CERN Control and Monitoring Platform (C2MON) is essentially a heterogeneous data acquisition framework with configuration, persistence, historical browsing, control and alarm functionalities. It has been developed for CERN’s demanding infrastructure monitoring needs and is based on more than 10 years of experience with the Technical Infrastructure Monitoring (TIM) systems at CERN.

It comes with a simple and intuitive data subscription API with integrated history browsing capabilities that can be used to form the basis for industrial dashboards and other graphical monitoring applications. Powerful data filtering and configuration mechanisms help fine tune data flow and prevent data burst situations.

### Designed for high-availability

* Multi server configuration allows for rolling updates and provides a failover mechanism
* Internal communication over [ActiveMQ](http://activemq.apache.org/) JMS ensures that none of your messages get lost
* Operational database independence through [JCACHE (JSR 107)](https://jcp.org/en/jsr/detail?id=107) In-Memory storage solution

### Scalable at all layers

* Add as many load-balanced servers as you need
* Unlimited Data Acquisition processes
* Scale-out your ActiveMQ JMS middleware
* Possibility to increase In-Memory storage e.g. through [Terracotta Server Open Source Kit](http://www.terracotta.org/downloads/open-source/catalog)

### Easy to customise

* Designed to fit multiple monitoring scenarios
* Customisable server based on Java and [Spring](http://projects.spring.io/spring-framework/)
* Write your own [Data Acquisition modules](docs/user-guide/daq-api/daq-module-developer-guide.md)
* Extendible Java and REST client API available

{% include video.html url="hxo8K0lbqos" %}
