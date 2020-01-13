---
layout:   post
title:    C2MON documentation
summary:  The official CERN Control and Monitoring Platform (C2MON) documentation
---
{{""}}
Welcome to the official CERN Control and Monitoring Platform (C2MON) documentation!

The CERN Control and Monitoring Platform (C2MON) is an open-source heterogeneous time-series data acquisition/subscription framework with alerting and
control functionalities that can be used for building many different types of system. Check out the potential [use cases]({{ site.baseurl }}{% link docs/overview/index.md %}#use-cases).

This manual includes concepts, instructions and samples to guide you on how to use the C2MON platform.

We do a constant effort to complete our documentation. If you are missing some information, please write an email to [c2mon-support@cern.ch](mailto:c2mon-support@cern.ch).


## Introduction

For an introduction to the architecture and concepts behind C2MON, see [the overview]({{ site.baseurl }}{% link docs/overview/index.md %}).

To find out how to get up and running with your own C2MON instance, check out the [Getting Started]({{ site.baseurl }}{% link docs/getting-started.md %}) guide.


## Source Code

The source code of C2MON is provided under [GNU LGPL Version 3]({{ site.baseurl }}{% link docs/c2mon-license.md %}) on [GitHub](http://github.com/c2mon/c2mon) and on [CERN GitLab](https://gitlab.cern.ch/c2mon/c2mon).


## Download and Install

The **C2MON server** can be either installed from the [distribution tarball] or with [Docker].

We recommend to always use the latest stable version listed in the [CHANGELOG] file.

[distribution tarball]: https://nexus.web.cern.ch/nexus/service/local/artifact/maven/redirect?r=cern-nexus&g=cern.c2mon.server&a=c2mon-server&v=LATEST&e=tar.gz
[Docker]: https://hub.docker.com/r/cern/c2mon
[CHANGELOG]: https://github.com/c2mon/c2mon/blob/master/CHANGELOG.md


## Data Acquisition (DAQ) modules

CERN provides at best effort several ready-to-use DAQ modules. All DAQs can as well be installed from a tarball or Docker container.

| Name                    | Distribution                                                 | Version                                                           |
|-------------------------|--------------------------------------------------------------|-------------------------------------------------------------------|
| [c2mon-daq-hostmetrics] | [tarball][tarball-hostmetrics], [Docker][docker-hostmetrics] | [Click here](https://github.com/c2mon/c2mon-daq-hostmetrics/tags) |
| [c2mon-daq-rest]        | [tarball][tarball-rest], [Docker][docker-rest]               | [Click here](https://github.com/c2mon/c2mon-daq-rest/tags)        |
| [c2mon-daq-opcua]       | [tarball][tarball-opcua], [Docker][docker-opcua]             | [Click here](https://github.com/c2mon/c2mon-daq-opcua/tags)       |

[c2mon-daq-hostmetrics]: https://github.com/c2mon/c2mon-daq-hostmetrics
[c2mon-daq-rest]: https://github.com/c2mon/c2mon-daq-rest
[c2mon-daq-opcua]: https://github.com/c2mon/c2mon-daq-opcua
[tarball-hostmetrics]: https://nexus.web.cern.ch/nexus/service/local/artifact/maven/redirect?r=cern-nexus-snapshot&g=cern.c2mon.daq&a=c2mon-daq-hostmetrics&v=LATEST&e=tar.gz&c=dist
[docker-hostmetrics]: https://gitlab.cern.ch/c2mon/c2mon-daq-hostmetrics/container_registry
[tarball-rest]: https://nexus.web.cern.ch/nexus/service/local/artifact/maven/redirect?r=cern-nexus&g=cern.c2mon.daq&a=c2mon-daq-rest&v=LATEST&e=tar.gz&c=dist
[docker-rest]: https://gitlab.cern.ch/c2mon/c2mon-daq-rest/container_registry
[tarball-opcua]: https://nexus.web.cern.ch/nexus/service/local/artifact/maven/redirect?r=cern-nexus&g=cern.c2mon.daq&a=c2mon-daq-opcua&v=LATEST&e=tar.gz&c=dist
[docker-opcua]: https://gitlab.cern.ch/c2mon/c2mon-daq-opcua/container_registry


## Support

We provide best effort support via <c2mon-support@cern.ch>
