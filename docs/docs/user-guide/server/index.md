---
layout:   post
title:    Running the C2MON server
summary:  How to configure and start the C2MON server.
---
{{""}}

How to deploy and start C2MON server with Docker or the distribution tarball is described in the [Getting Started](/getting-started) Guide

However, it is also possible to run C2MON from within your favourite IDE. For this you have to:

- clone [C2MON from GitHub](http://github.com/c2mon/c2mon),
- import it to your IDE (e.g. Eclipse or IntelliJ) as Maven project,
- Run the server main class `cern.c2mon.server.ServerStartup` which is part of the c2mon-server-lifecycle module

All you need is at least **Java 1.8** installed on your machine.

Here, an example how the _run configuration_ looks in Eclipse:

![eclipse-server-config]({{site.baseurl }}/assets/img/user-guide/server/eclipse-server-config.png)

## Changing default configuration

C2MON comes with reasonable defaults for most settings.
Before you get out to tweak and tune the configuration, make sure you understand what are you trying to accomplish and the consequences.

The primary way of configuring a server is via the [`conf/c2mon-server.properties`](https://github.com/c2mon/c2mon/blob/master/c2mon-server/distribution/tar/conf/c2mon-server.properties) configuration file, which is delivered with the tarball.
It contains the most important settings and their default values you may want to change for your environment.

The properties listed in the file can just as well be set as Java system properties with the `-D` option.

If you don't use the default script to start C2MON (e.g. when running from Eclipse or IntelliJ) you can set the location of your customised properties file with `-Dc2mon.server.properties`

Example:
```
-Dc2mon.server.properties="file://$C2MON_HOME/conf/c2mon-server.properties"
```
