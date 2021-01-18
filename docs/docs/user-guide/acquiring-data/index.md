---
layout:   post
title:    Acquiring Data
summary:  Learn how to set up a minimal C2MON environment 
---

We have gotten to know the configuration of Tags, Equipments and Processes in the [overview]({{ site.baseurl }}{% link docs/overview/index.md %}).
Let's take a look at how to go about applying these configurations to acquire data from running processes.

C2MON relies on Data Acquisition Processes, or DAQs, to collect data from sources.
These are independent Java processes which fetch their respective configuration from the server layer, and collect data according to their configured sources.
DAQs can be implemented in vastly different ways to meet the requirements of the respective data sources or of user needs - 
you can easily implement your own solution by following the [DAQ module developer guide]({{ site.baseurl }}{% link docs/user-guide/daq-api/daq-module-testing-guide.md %}). 

In this section we will walk-through the setup of a C2MON instance with sample configurations and and data sources.
This walk-through provides a hand's on example by setting up the C2MON environment and goes into detail on how to 
monitor randomly generated data via a custom OPC UA server using the open source [OPC UA DAQ]({{ site.baseurl }}{% link docs/user-guide/acquiring-data/using-opc-ua.md %}).

**You should be familiar with Docker and with basic C2MON terminology before starting this walk-through.**

# The C2MON server and direct dependencies
In order to monitor our data points we must set up the services required for operating C2MON. 
This can be done in different ways as described in [Getting Started]({{ site.baseurl }}{% link docs/getting-started.md %}). 
In this example we will use Docker images.

<!----LINK TO PUBLIC RESOURCE REPO ---->
We will use the [docker-compose](https://docs.docker.com/compose) tool to define and run these services in containers.
You can find a reference docker-compose configuration in our [C2MON repository](https://github.com/c2mon/c2mon/tree/master/c2mon-server/distribution/compose).

Not all services are strictly required: they represent a selection of services from the rich infrastructure of C2MON 
  applications. To start with, let's take a look at the services that our C2MON server depends on:
* [MySQL](https://www.mysql.com/de) is a database we use here to store our data
* [ActiveMQ](http://activemq.apache.org) is used as a message broker in between the different tiers of the C2MON 
  infrastructure: client applications, server, and data acquisition processes.
* We use an external [Elasticsearch](https://www.elastic.co) service as a search and analytics engine to allow us a 
  quicker and more convenient access to our data later in this walk-through. You can choose to use an Elasticsearch 
  engine that is embedded in C2MON by setting `C2MON_SERVER_ELASTICSEARCH_EMBEDDED` to `true`, or to disable it completely 
  by setting `C2MON_SERVER_ELASTICSEARCH_ENABLED` to `false`.

The relevant service definitions in our docker-compose file:
```yaml
...
  # the C2MON server
  c2mon:
    container_name: c2mon
    image: cern/c2mon:${C2MON_TAG}
    ports:
      - "9001:9001"
    environment:
      - C2MON_SERVER_ELASTICSEARCH_ENABLED=true
      - C2MON_SERVER_ELASTICSEARCH_HOST=elasticsearch
      - C2MON_SERVER_ELASTICSEARCH_PORT=9200
      - C2MON_SERVER_ELASTICSEARCH_EMBEDDED=false
      - C2MON_SERVER_ELASTICSEARCH_CLIENT=rest
      - C2MON_SERVER_ELASTICSEARCH_SCHEME=http
      - C2MON_SERVER_JMS_EMBEDDED=false
      - C2MON_SERVER_JMS_URL=tcp://mq:61616
      - C2MON_SERVER_CACHEDBACCESS_JDBC_VALIDATION-QUERY=SELECT 1
      - C2MON_SERVER_JDBC_DRIVER-CLASS-NAME=com.mysql.jdbc.Driver
      - C2MON_SERVER_JDBC_URL=jdbc:mysql://db/tim
      - C2MON_SERVER_JDBC_USERNAME=user
      - C2MON_SERVER_JDBC_PASSWORD=user-pwd
      - C2MON_SERVER_CACHEDBACCESS_JDBC_JDBC-URL=jdbc:mysql://db/tim
      - C2MON_SERVER_HISTORY_JDBC_JDBC-URL=jdbc:mysql://db/tim
      - C2MON_SERVER_CONFIGURATION_JDBC_JDBC-URL=jdbc:mysql://db/tim
      - C2MON_SERVER_TESTMODE=false
    restart: on-failure

  # a search and analytics engine with integrated persistence
  elasticsearch:
    container_name: elasticsearch
    image: gitlab-registry.cern.ch/c2mon/c2mon/es:${ELASTICSEARCH_TAG}
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - cluster.name=c2mon
      - discovery.type=single-node
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - TAKE_FILE_OWNERSHIP="1"
    ulimits:
      memlock:
        soft: -1
        hard: -1
    # uncomment to persist your data
    # volumes:
      # - ./elasticsearch/data:/usr/share/elasticsearch/data

  # a message broker providing communication in between the different levels of the C2MON tiers (client applications, daqs, and server)
  mq:
    container_name: mq
    image: gitlab-registry.cern.ch/c2mon/c2mon/mq:${ACTIVEMQ_TAG}
    ports:
      - "61616:61616"
      - "61614:61614"
      - "1883:1883"
      - "8086:8086"
      - "8161:8161"

  # simple MySQL database for C2MON to store its data
  db:
    container_name: db
    image: gitlab-registry.cern.ch/c2mon/c2mon/mysql:${MYSQL_TAG}
    environment:
      - MYSQL_ROOT_PASSWORD=root-pwd
      - MYSQL_DATABASE=tim
      - MYSQL_USER=user
      - MYSQL_PASSWORD=user-pwd
    # uncomment to persist your data
    # volumes:
      # - ./mysql/data:/var/lib/mysql

...
```

You may note that the images we use for these service that are published under a C2MON domain: they are preconfigured 
to integrate smoothly with our C2MON server.

Navigate to the directory containing the docker-compose file in your terminal, and start these dependencies using:  

```bash
docker-compose up -d mq db elasticsearch 
```

Once these containers are ready, let's start out C2MON server as well: 

```bash
docker-compose up -d c2mon 
```

# Starting the Hostmetrics DAQ

Acquiring data through C2MON usually required to configure the corresponding Process, Equipment and Tags beforehand.
There is one exception to this rule: The [Hostmetrics DAQ](https://github.com/c2mon/c2mon-daq-hostmetrics/) can 
configure its own Process on the server on startup. It is a simple, exemplary data acquisition module which collects 
metrics about the current host using the [OSHI](https://github.com/oshi/oshi) library and allows a quick and easy 
demonstration of C2MON.

The Hostmetrics DAQ snippet in the docker-compose file:
```yaml
...
  # A simple, exemplary C2MON DAQ module for publishing metrics about the current host 
  daq-hostmetrics:
    container_name: daq-hostmetrics
    image: gitlab-registry.cern.ch/c2mon/c2mon-daq-hostmetrics:latest
    environment:
      - "_JAVA_OPTIONS=-Dc2mon.daq.name=P_HOSTMETRICS -Dc2mon.daq.jms.url=tcp://mq:61616 -Dc2mon.client.jms.url=tcp://mq:61616"
...
```

Let's start the Hostmetrics DAQ in the background as well:

```bash
docker-compose up -d daq-hostmetrics 
```

# Visualizing our C2MON configuration and data

Now we already have a minimal setup including the C2MON server, its direct dependencies Elasticsearch, ActiveMQ and MySQL, 
as well as a running DAQ Process. Let's explore two client applications:

* The [C2MON Web UI](https://github.com/c2mon/c2mon-web-ui) is a graphical web interface for C2MON. It allows to browse 
our Process, Equipment and Tag configuration, can query Tag history and display value trends, and it can display the current
Tag value, status and properties. 

* [Grafana](https://grafana.com) is generic solution for data visualization and anaysis. We will use Grafana to inspect some of the metrics 
delivered to us by the Hostmetrics DAQ.

The C2MON Web UI and Grafana definitions in the docker-compose file:
```yaml
...
 # simple interface to browse your C2MON configuration and data
  web-ui:
    container_name: web-ui
    image: cern/c2mon-web-ui:0.1.14-SNAPSHOT:${WEB_UI_TAG}
    ports:
      - "8080:8080"
    environment:
      - C2MON_CLIENT_JMS_URL=tcp://mq:61616 
      - C2MON_CLIENT_HISTORY_JDBC_URL=jdbc:mysql://db/tim
      - C2MON_CLIENT_HISTORY_JDBC_USERNAME=root
      - C2MON_CLIENT_HISTORY_JDBC_PASSWORD=root-pwd
      - C2MON_CLIENT_HISTORY_JDBC_VALIDATION-QUERY=SELECT 1

  # visualize your data and metrics
    grafana:
      container_name: grafana
      image: grafana/grafana:latest
      ports:
        - "3000:3000"
      environment:
        - GF_AUTH_ANONYMOUS_ENABLED=true
        - GF_AUTH_ANONYMOUS_ORG_NAME=Main Org.
        - GF_AUTH_ANONYMOUS_ORG_ROLE=Editor
        - GF_INSTALL_PLUGINS=grafana-piechart-panel
        - GF_DASHBOARDS_DEFAULT_HOME_DASHBOARD_PATH=/etc/grafana/provisioning/dashboards/dashboards.json
        - GF_DASHBOARDS_DEFAULT_HOME_DASHBOARD_PATH=/etc/grafana/provisioning/dashboards/dashboard-hostmetrics.json
      volumes:
        # 'elasticsearch', 'prometheus' and 'db' as data sources
        # default dashboards for 'daq-opc-ua' and 'daq-hostmetrics'
        - ./grafana/provisioning:/etc/grafana/provisioning
...
```

Take a look at the `./grafana/provisioning` folder: it contains the necessary files to start Grafana with Elasticsearch 
and MySQL as preconfigured data sources. It also refers to Prometheus as a data source, which we will discuss in more 
detail when exploring the OPC UA DAQ.

We have also included two dashboards in Grafana: a default dashboard for the Hostmetrics DAQ, and one specific to the OPC UA DAQ.  

Let's fire up the C2MON Web UI and Grafana:

```bash
docker-compose up -d web-ui grafana  
```

The C2MON Web UI now starts under the URL [localhost:8080/c2mon-web-ui](localhost:8080/c2mon-web-ui), and Grafana under [localhost:3000](localhost:3000).
Have a look around! You can for example inspect the latest values the DataTags we just defined by navigating through the 
"DAQ Process Viewer" to our Process, Equipment, and DataTags, and to "View Trend", and try to the trend of running threads 
as shown in the C2MON Web UI to Grafana. 

![C2MON Web UI trend viewer]({{ site.baseurl }}{% link assets/img/user-guide/acquiring-data/web-ui-numthreads.png %})
![Grafana Hostmetrics dashboard]({{ site.baseurl }}{% link assets/img/user-guide/acquiring-data/grafana-hostmetrics.png %})

# Wrapping up

We have set up a minimal C2MON environment including the C2MON server, MySQL database, Elasticsearch,  ApacheMQ message 
broker, and the Hostmetrics DAQ. We have then explored the configuration and data we collected through the C2MON Web UI
and Grafana Client Applications.

Next, let's configure new DataTags which we will monitor through the OPC UA DAQ. 