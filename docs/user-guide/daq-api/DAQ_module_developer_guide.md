# Creating a new DAQ module from scratch

A step-by-step guide to integrate a new communication protocol.

---

This guide assumes that you are familiar with the [core concepts](/overview) of C2MON. At the end of this guide you should be able to write your
own data acquisition module using the C2MON DAQ APIs.

## Including the API

To use the API, you need to add a dependency on `c2mon-daq-core`.

Remember to replace "__insert_version_here__" with a real version number, such as "1.8.30"

### Maven configuration
If you make use of Maven include the following dependency in you DAQ project:

```xml
<dependency>
    <groupId>cern.c2mon.daq</groupId>
    <artifactId>c2mon-daq-core</artifactId>
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
compile "cern.c2mon.daq:c2mon-daq-core:__insert_version_here__"
```

Declaration of the CERN Nexus repository:

```json
repositories {
  mavenCentral()
  maven { url "https://nexus.web.cern.ch/nexus/content/groups/public" }
}

```

## Writing a new equipment message handler

The `EquipmentMessageHandler` is an abstract class that specifies the methods you must implement to provide basic DAQ
functionality, and provides support methods for handing you your pre-defined configuration.

The following four abstract methods must be implemented within your DAQ module instance:

```java
public class DemoMessageHandler extends EquipmentMessageHandler {

  /**
   * Perform the necessary tasks to connect to the underlying data source. The
   * handler is expected to be ready to publish data as soon as this method
   * returns.
   *
   * @throws EqIOException if an error occurs while connecting.
   */
  @Override
  public void connectToDataSource() throws EqIOException {
    // TODO: Implement data source connection
  }

  /**
   * Disconnect and release any resources to allow a clean shutdown.
   *
   * @throws EqIOException if an error occurs while disconnecting.
   */
  @Override
  public void disconnectFromDataSource() throws EqIOException {
    // TODO: Implement data source disconnection
  }

  /**
   * Publish the latest value of all tags on request.
   */
  @Override
  public void refreshAllDataTags() {
    // TODO: Handle the refresh request
  }

  /**
   * Publish the latest value of a single tag on request.
   *
   * @param tagId the id of the data tag to refresh.
   */
  @Override
  public void refreshDataTag(long tagId) {
    // TODO: Handle the refresh request
  }
}
```

!!! info "Note"
    It is necessary to pre-define the specific `EquipmentMessageHandler` implementation that will be used at configuration time (inside an `Equipment`).
    So, the DAQ process will only know at runtime which handler to use for that equipment. This allows a single process to deal with multiple types of
    equipment at the same time.



### Implementing `connectToDataSource()`

The `connectToDataSource()` method is called for you when the DAQ process wants your equipment to connect to the underlying data source and get ready to start
publishing data.

In general, you should perform all initialisation in this method, such as:

- Inspecting the pre-defined configuration (by calling `getEquipmentConfiguration()` to retrieve an `IEquipmentConfiguration` instance)
  - Using the pre-defined equipment address to connect the data source (via `IEquipmentConfiguration#getAddress()`)
  - Parsing the list of `ISourceDataTag` objects and binding them to the data source
  - Starting an event loop to listen for source updates and publishing them (via `IEquipmentMessageSender#sendTagFiltered` and friends)
- Publishing an initial equipment connection health message
- Publishing heartbeats for the equipment
- Registering any other optional handlers, such as `ICommandRunner` or `IEquipmentConfigurationChanger`.

This is of course a lot for a single method. Best practice is therefore to delegate the work to sub classes in order to simplify the main logic.

!!! info "Note"
    `connectToDataSource()`` is only called once per equipment lifecycle. This means at process startup, or after an equipment reconfiguration.


### Inspecting the pre-defined configuration

The first thing your handler should do is acquire and inspect the pre-defined configuration and use it to connect to the underlying data source and bind
individual items within the data source as tags.

#### Using the equipment address

You will have pre-defined the address of the equipment at configuration time. The address is an arbitrary string that contains some data that your handler is
capable of understanding and using to connect to the source. For example, it could contain the address of a remote OPC server.

#### Using the tag address parameters

You will have already pre-defined the address parameters for each tag at configuration time. The tag address is simply a map of strings. Again, it is up to
your handler to know what to do with these parameters in order to bind them to a specific input. For example, it could contain the name of an individual
item in the remote OPC server.


**Example**:
```java
public class DemoMessageHandler extends EquipmentMessageHandler {
  ...

  @Override
  public void connectToDataSource() throws EqIOException {

    IEquipmentConfiguration equipment = getEquipmentConfiguration();
    String address = equipment.getAddress();

    // Use the equipment address to connect to the underlying data source

    for (ISourceDataTag dataTag : equipment.getSourceDataTags().values()) {
      Map<String, String> addressParameters = dataTag.getAddressParameters();

      // Use the tag address paremeters to bind to items within the data source
    }
  }

  ...
}
```


### Supervising equipment health

In order for C2MON to be able to monitor the quality of publications, handlers should provide regular updates about their health. This is done by using
heartbeats and "communication fault" tags.

#### Heartbeats

Heartbeats are usually sent at pre-defined intervals, defined in `IEquipmentConfiguration#getAliveTagInterval()`.

To send a heartbeat, simply call `getEquipmentMessageSender().sendSupervisionAlive()`. Note that equipment heartbeats are optional.

#### Communication faults

Communication faults are sent whenever a handler detects a problem with the connection to the underlying data source, or when a connection problem resolves
itself.

To indicate that the connection state is healthy, use `IEquipmentMessageSender#confirmEquipmentStateOK()`. To indicate that there is a problem, use
`IEquipmentMessageSender#confirmEquipmentStateIncorrect(String)` where the parameter containts a description of the problem.


## Running your DAQ module

Currently, `c2mon-daq-core` provides the `cern.c2mon.daq.DaqStartup` class which contains the `main` method that is responsible for connecting to the C2MON
server and instantiating your `EquipmentMessageHandler` instances.


## Executing commands (optional)

If your source supports executing commands, you can implement an `ICommandRunner` and register it via `IEquipmentCommandHandler#setCommandRunner`.

## Dynamic reconfiguration (optional)

### Tags

To support dynamic reconfiguration of tags, you can implement an `IDataTagChanger` and register it via `IEquipmentConfigurationHandler#setDataTagChanger`. When
the configuration of the set of tags for this handler changes, the methods of your `IDataTagChanger` will be called and you will have to deal with it.

### Commands

To support dynamic reconfiguration of tags, you can implement an `ICommandTagChanger` and register it via
`IEquipmentConfigurationHandler#setCommandTagChanger`.  When the configuration of the set of commands for this handler changes, the methods of your
`ICommandTagChanger` will be called and you will have to deal with it.

### Equipment

Implement and register an `IEquipmentConfigurationChanger` via `IEquipmentConfigurationHandler#setEquipmentConfigurationChanger`.
