# Installation

## Server

The C2MON server is a standalone application that communicates either locally or remotely with DAQ processes and client applications.

* [GitHub](http://github.com/c2mon/c2mon)
* [CERN Docker registry](https://docker.cern.ch/registrytags?c2mon-project)
* Tarball distribution: **coming soon!**


## Client API

The C2MON client API is a Java-based API for subscribing to data sources, retrieving historical data, listening for alarms etc.

### Using Maven

Add the following lines to your POM file to include the C2MON client API dependency:
```xml
<dependency>
    <groupId>cern.c2mon.c2mon-client</groupId>
    <artifactId>c2mon-client-all</artifactId>
    <version>__insert_version_here__</version>
</dependency>
```

### Using Gradle

```
compile "cern.c2mon.c2mon-client:c2mon-client-all:__insert_version_here__"
```

Remember to replace "__insert_version_here__" with a real version number, such as "1.7.5".

## DAQ API

**coming soon!**

## Web interface

Tarball distribution: **coming soon!**

Docker image: **coming soon!**
