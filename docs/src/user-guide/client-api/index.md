# Client API

The C2MON Client API is written in Java and provides various service classes to interact with the server.
All services are accessed via the `C2monServiceGateway` which provides:

* `TagService`: This is the most important service class and most probably the only one you will need in the beginning.
it contains all methods to search and subscribe to tag values.
* `AlarmService`: Allows subscribing to active alarm or retrieve alarm information
* `CommandService`: To execute pre-configured commands.
* `ConfigurationService`: Allows applying new server configurations and to fetch the entire configuration for the configured DAQ Processes.
* `SupervisionService`: Allows registering listeners to get informed about the connection state to the JMS brokers and the heartbeat of the C2MON server.
* `SessionService`: Optional service to secure the command execution.
(Please note that this service requires implementing in addition an `AuthenticationManager`)

## Setup

Add the following lines to your Maven POM file to include the C2MON client API dependency:
```xml
<dependency>
    <groupId>cern.c2mon.c2mon-client</groupId>
    <artifactId>c2mon-client-all</artifactId>
    <version>__insert_version_here__</version>
</dependency>
```

In addition it requires specifying the Java System property below in your application context:

```bash
# URL to C2MON Client properties file
-Dc2mon.client.conf.url=http://example/c2mon-client.properties
```

Alternatively you can also set a link to a file on your local system.

```bash
# URL to C2MON Client properties file
-Dc2mon.client.conf.url=file:///c2mon-client.properties
```

To set the property inside your code, you can use the following command.

```java
System.setProperty("c2mon.client.conf.url", "file:///c2mon-client.properties");
```

The properties file must at least contain the http://activemq.apache.org/uri-protocols.html[JMS broker communication URL] to reach the C2MON server instance.

```bash
# ActiveMQ URL
c2mon.client.jms.url=tcp://activemq-broker-host:61620?wireFormat.tcpNoDelayEnabled=true
```


## Startup

At application startup you have to make once the following call in order to initialise the `C2MONServiceGateway`.
Only then you have access to the different Service instances.

```java
C2monServiceGateway.startC2monClientSynchronous();
```

The startup can take several seconds, so if you want to to load in meantime other parts of your system use the asynchronous call instead.

```java
C2monServiceGateway.startC2monClient();
```
