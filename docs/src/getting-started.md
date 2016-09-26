# Getting Started

Just want to get C2MON up and running to see what it does? Read this page.

# Running the server

The first thing to do is to get a C2MON server up and running. The server is a standalone application that receives data from acquisition processes (DAQs)
and pushes it to client applications.

You can run a server by downloading and executing a tarball distribution, or by running a Docker image.

### Running the tarball distribution

** Coming soon!**

### Using the Docker image

We push a Docker image of the server to the [CERN Docker registry](https://gitlab-registry.cern.ch). To run the image:

```bash
docker run --rm --name c2mon -it -p 0.0.0.0:1099:1099 -p 0.0.0.0:9001:9001 -p 0.0.0.0:61616:61616 -p 0.0.0.0:9200:9200 gitlab-registry.cern.ch/c2mon/c2mon
```

# Publishing data

Once you have your server running, you can use one of several pre-provided acquisition processes (DAQs) that will grab data from some service and send it to
the C2MON server for monitoring. (Alternatively, you can of course write your own DAQ to publish any type of metric using the [DAQ API](daq-api)).

Perhaps the easiest DAQ to understand is the `hostmetrics` DAQ, which uses a simple library to monitor and publish metrics about the host machine on which
the DAQ runs. Again, you can download and execute a tarball or run a Docker image to get it up and running.

### Running the hostmetrics DAQ tarball distribution

**Coming soon!**

### Using the hostmetrics DAQ Docker image

```bash
docker run --rm --name daq-hostmetrics -it gitlab-registry.cern.ch/c2mon/c2mon-daq-hostmetrics bin/C2MON-DAQ-STARTUP.jvm -f $@
```

# Reading data

Once you have a server running and a DAQ process publishing data to the server, it's time to actually look at how we're going to get the data back out again
in order to do something useful with it.

C2MON comes with a web-based application that you can use to do various things such as view metric history, monitor alarms, inspect configurations and
view statistics about C2MON clusters.

## Running the web interface tarball distribution

**Coming soon!**

## Using the Docker image

```bash
docker run --rm --name web-ui -ti --link c2mon:c2mon -p 0.0.0.0:8080:8080 gitlab-registry.cern.ch/c2mon/c2mon-web-ui
```

## Inspecting the data

**TODO**: write a brief section on how to find and interpret metrics using the web interface
