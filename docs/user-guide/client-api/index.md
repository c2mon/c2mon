# Data subscription

Learn how to subscribe to your data with the C2MON Client API.

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
or the unique tag name.

> **Please note!**

> A well-chosen naming convention will enable you to make searching for tags easier in the future.
> We suggest using a _folder-like_ structure with `/` as separator.

> Example: `serviceA/computer/mypc1/memory`




### Searching for tags by name

The `TagService` offers multiple possibilities to get data by name from the server. You can:

- Give the explicit tag name (or a list of names);
- Give a wildcard expression (or multiple expressions)

**Tag names are *always case insensitive*.**

The following special characters are supported in wildcard expressions:

- ? - match any one single character
- \* - match any multiple character(s) (including zero)

The supported wildcard characters can be escaped with a backslash `\`, and a literal backslash can be included with '\\'

!!! warning "Be careful!"
    Expressions starting with a leading wildcard character are potentially very expensive (ie. full scan) for indexed caches.

**Example:** Get the latest value of a tag by explicit name
```java
Tag tag = tagService.findByName("host1:cpu.avg");
```

**Example:** Get the latest value of the `cpu.avg` metric for all hosts:
```java
Collection<Tag> tags = tagService.findByName("host*:cpu.avg");
```




### Subscribing to tag updates

A near real-time stream of tag updates can be acquired through the use of a `TagListener`.

**Example:** Subscribe to a set of tags
```java
...
TagService tagService = C2monServiceGateway.getTagService();
tagService.subscribeByName("host*:cpu.avg", new TagUpdateListener());
...

public class TagUpdateListener implements TagListener {

  /**
   * Called every time a new value update is received
   */
  @Override
  public void onUpdate(final Tag tagUpdate) {
    System.out.println(String.format("Update for tag %s (%d): %s",
                        tagUpdate.getName(), tagUpdate.getId(), tagUpdate.getValue()));
  }

  /**
   * Called once during subscription to pass the initial values
   */
  @Override
  public void onInitialUpdate(final Collection<Tag> initialValues) {
    System.out.println(String.format("\nFound %d matching tags", initialValues.size()));

    for (Tag tag : initialValues) {
      System.out.println(String.format("Initial value for tag %s (%d): %s",
                          tag.getName(), tag.getId(), tag.getValue()));
    }
  }
}
```

### Subscription by tag ID

In addition to its name, each tag has a unique ID. In certain cases you may prefer to use the ID directly instead of the name, in particular if
you have already a listener subscribed to a given tag. In that case, the client does not have to contact the server as for a wildcard search and can
directly use the local cache, which is of course significantly faster.

**Example:** Get the latest value of a tag by ID:
```java
Tag tag = tagService.get(1234L);
```

Every other aspect of subscribing to tags by ID is identical to that of subscribing by name.






[c2mon-client.properties]: https://github.com/c2mon/c2mon-web-ui/blob/master/src/dist/tar/conf/c2mon-client.properties
