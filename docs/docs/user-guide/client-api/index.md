# The C2MON Client API

Learn how to set up and use the C2MON Client API.

---

The C2MON Client API is written in Java and provides various service classes to interact with the server:

* `TagService`: The most important service class and most probably the only one you will need in the beginning. Provides methods to
search for and subscribe to tags.
* `AlarmService`: Provides methods for subscribing to active alarms or retrieving alarm information.
* `CommandService`: Provides methods for executing commands on source equipment.
* `ConfigurationService`: Provides methods for creating/reading/updating/removing pre-configured entities (such as tags or commands).
* `SupervisionService`: Provides methods for registering heartbeat listeners, to monitor server connection state.
* `SessionService`: Optional service to secure the command execution (requires implementing an `AuthenticationManager`).

## Including the API

To use the API, you need to add a dependency on `c2mon-client-core`.

Remember to replace "__insert_version_here__" with a real version number, such as "1.8.30"

### Maven configuration
If you make use of Maven include the following dependency in you C2MON client project:

```xml
<dependency>
    <groupId>cern.c2mon.client</groupId>
    <artifactId>c2mon-client-core</artifactId>
    <version>__insert_version_here__</version>
</dependency>
```

The C2MON artifacts are hosted on the CERN Nexus repository:

```xml
<repositories>
  <repository>
    <id>cern-nexus</id>
    <name>CERN Central Nexus</name>
    <url>https://nexus.web.cern.ch/nexus/content/groups/public</url>
  </repository>
</repositories>
```

### Gradle configuration

```json
compile "cern.c2mon.client:c2mon-client-core:__insert_version_here__"
```

Declaration of the CERN Nexus repository:

```json
repositories {
  mavenCentral()
  maven { url "https://nexus.web.cern.ch/nexus/content/groups/public" }
}

```

## Changing the default configuration

C2MON comes with reasonable defaults for most settings.
Before you get out to tweak and tune the configuration, make sure you understand what are you trying to accomplish and the consequences.

The primary way of configuring a C2MON client application is via the [c2mon-client.properties] configuration file, which is delivered as example with the [c2mon-web-ui](https://github.com/c2mon/c2mon-web-ui) tarball.
It contains the most important settings and their default values you may want to change for your environment.

To inject a customised properties file to your C2MON client application use this Java system property: `-Dc2mon.client.conf.url`

The properties listed in the file can just as well be set as Java system properties with the `-D` option.


### Connecting to a remote C2MON server

By default, the client tries to connect to a server running at `localhost:61616`.

The [c2mon-client.properties] file must contain at least the `c2mon.client.jms.url` property, which is the [URL of the JMS broker](http://activemq.apache.org/uri-protocols.html) to which the C2MON server is listening for client connections.

For example:

```bash
c2mon.client.jms.url=tcp://jms-broker-host:61616
```

## Using the API

### Spring Boot applications

Applications that use Spring Boot can benefit from integration with the C2MON client. Access to the client service classes can simply be acquired by
`@Autowiring` them, e.g.:

```java
@Autowired
private TagService tagService;
```

### Non-Spring applications

Regular Java applications must use the `C2monServiceGateway` to acquire access to the service classes. At application startup, initialise the client:

```java
C2monServiceGateway.startC2monClientSynchronous();
```

The startup can take several seconds, so to initialise the client asynchronously (letting you load other parts of your system in the meantime):

```java
C2monServiceGateway.startC2monClient();
```

Then the service classes can be acquired via the gateway.

For example:
```java
TagService tagService = C2monServiceGateway.getTagService();
```


## Getting data

Once you have access to the `TagService`, you can start to retrieve data from C2MON. To find and subscribe your data you can either use the unique tag ID,
or the unique tag name. Read more on the [Data Subscription documentation page](data-subscription)
