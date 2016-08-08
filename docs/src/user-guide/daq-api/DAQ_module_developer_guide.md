<a id="_creating_a_new_daq_module_from_scratch"></a>
## Creating a new DAQ module from scratch

Before starting reading this guide please make sure that you are at least familiar with the following section:

* [Core Concepts](/core-concepts)

At the end of this guide you should be able to write your first own DAQ module.


### Introduction

The following guideline explains the main steps to create your own C2MON DAQ module.
Traditionally a DAQ module was dealing with a certain type of equipment, for example PLC, OPC, etc.
This is the reason why many of the classes and interfaces in the DAQ core contain the word "Equipment" in the name.
But this does not mean that you cannot write a DAQ for a retrieving data from other sources like middle-ware protocols.

An "Equipment" can in fact represent any kind of data source, like for instance:

* JMS broker,
* JMX server,
* JSON messages,
* custom hardware,
* services, ...

> **Please note!**

>Please notice, that this guideline does not cover how to package and deploy your project, neither how to start a DAQ module.
>If you are interested in these topics, please contact c2mon-support@cern.ch


### Creating the Maven POM configuration for your project

Create a new Maven project and add the c2mon-daq-core artifact as dependency.
The c2mon-daq-core contains beside the abstract class EquipmentMessageHandler also the DaqStartup start-up class which contains the `main(String[])` method for launching later your DAQ module.

```xml
<dependency>
    <groupId>cern.c2mon.c2mon-daq</groupId>
    <artifactId>c2mon-daq-core</artifactId>
    <version>RELEASE</version>
</dependency>
```



### Defining a new HardwareAddress class

Before starting with the implementation of a new DAQ module you should first think about the address format.
Which information is required for subscribing to a value of your new data source?
This is very important, since all configuration parameters will later be managed and provided by the C2MON server.

The entire DAQ configuration is sent as XML message, but the DAQ core will de-serialize it back for you into simple Java object Beans.
To do so, C2MON needs to know the configuration class used for your DAQ module, which implements the `HardwareAddress` interface.
Both, server and DAQ core have to have this class in their class-path to properly handle the serialization and de-serialization.
For the de-serialization, the DAQ core makes use of http://docs.oracle.com/javase/tutorial/reflect/[Java reflection].
This is only possible, because the path to the concrete `HardwareAddress` class is provided within the HardwareAddress tag of the XML configuration (see example):

**Example of a DataTag configuration for the OPC protocol, sent by the C2MON server to the DAQ Core:**
```xml
<DataTags>
  <DataTag id="207572" name="FW.MEY.FDED-00049:T_OUT_ED_ECHG_EPF" control="false">
    <data-type>Float</data-type>
    <DataTagAddress>
      <HardwareAddress class="cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl">
        <namespace>0</namespace>
        <opc-item-name>CVCOOLLINAC3TARGET!%MF12294</opc-item-name>
        <command-pulse-length>0</command-pulse-length>
        <address-type>STRING</address-type>
        <command-type>CLASSIC</command-type>
      </HardwareAddress>
      ...
    </DataTagAddress>
  </DataTag>
...
</DataTags>
```



#### Example for a HardwareAddress implementation

Let's have a look at another existing class called `DBHardwareAddressImpl` which is used by the Oracle Database DAQ Module (DB DAQ). To subscribe a tag, the DB DAQ needs only one parameter:

| Parameter | Type |Description |
| --------- | ---- |----------- |
| dbItemName | String | Name of the DB field to which the given tag should subscribe to. |


The corresponding `DBHardwareAddressImpl` class has thus only to define this single field.

**DBHardwareAddressImpl Class**
```java
package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.datatag.address.DBHardwareAddress;

public class DBHardwareAddressImpl extends HardwareAddressImpl implements DBHardwareAddress {

  /** Serial serial version UID */
  private static final long serialVersionUID = 1L;

  /** The name of the DB field to which the given tag should subscribe to */
  protected String dbItemName; // this field needs to be in scope protected because of Java reflection!

  /**
   * Default constructor
   */
  public DBHardwareAddressImpl() {
    // Does nothing
  }

  public DBHardwareAddressImpl(final String dbItemName) {
    this.setDBItemName(dbItemName);
  }

  @Override
  public String getDBItemName() {
    return dbItemName;
  }

  public void setDBItemName(final String dbItemName) {
    this.dbItemName = dbItemName;
  }
}
```


> **Tip**

>**The visibility scope of all parameters have to be set to protected!**
>
>Please keep in mind, that the visibility scope of a parameter cannot be set to private, since this would disturb class introspection through reflection during the de-serialization phase.
>Attributes of your HardwareAddress class must be of Java primitive type or String. Java wrappers around primitive types are allowed (Integer, Long, Float, Double, ...)





#### Best coding practice

The points listed below should help you defining your `HardwareAddress` class:

* As in the example above, your class should extend from `HardwareAddressImpl`.
Like this you don't have to deal with implementing the `HardwareAddress` interface and you can focus on defining your address format.

* If you don't need a complex address structure and you are happy with only one field you can make use of `cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl` class.
The class contains exactly one String field called `address`.
The corresponding configuration would be:
```xml
<HardwareAddress class="cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl">
  <address>Put whatever you want!</address>
</HardwareAddress>
```
* Your class has to follow the http://docs.oracle.com/javase/tutorial/javabeans/[JavaBeans] standard in order to be compatible with the generic de-serialization procedure.
This means for instance that you have to define getter- and setter-methods for all of your fields. Furthermore, you shall set the visibility of your fields to protected or public.
Otherwise, the class introspection will not work.

* It is also good practice to define an interface for your class implementation.
Later in your DAQ module you should then only use the interface and never access directly the class.




#### Where to store the new `HardwareAddress` class?

All provided `HardwareAddress` class implementations (used by the CERN services TIM and DIAMON) have been placed into the following package in the c2mon-shared-common library: `cern/c2mon/shared/common/datatag/address/impl`

However, nothing prevents to save your `HardwareAddress` child class in another JAR as long as that one is present in the DAQ and server classpath.








### Extending the EquipmentMessageHandler class

The C2MON DAQ Core provides several interfaces that allows you defining the behaviour of your new acquisition module.
Most interfaces are optional and don't need to be implemented straight away, like for instance the `ICommandRunner` interface for supporting commands.

The only mandatory class which you have to extend is the `EquipmentMessageHandler`.
The _abstract_ `EquipmentMessageHandler` class is the general superclass for all DAQs modules.
It provides different methods to access parts of the core like for instance the configuration.

The following four abstract methods have to be implemented within your DAQ module instance:

```java
import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;

public class DemoMessageHandler extends EquipmentMessageHandler {

  /**
   * When this method is called, the EquipmentMessageHandler is expected
   * to connect to its data source. If the connection fails (potentially,
   * after several attempts), an EqIOException must be thrown.
   *
   * @throws EqIOException
   *             Error while connecting to the data source.
   */
  @Override
  public void connectToDataSource() throws EqIOException {
    // TODO Implement here the connection to your data source
  }

  /**
   * When this method is called, the EquipmentMessageHandler is expected
   * to disconnect from its data source. If the disconnection fails,
   * an EqIOException must be thrown.
   *
   * @throws EqIOException
   *             Error while disconnecting from the data source.
   */
  @Override
  public void disconnectFromDataSource() throws EqIOException {
    // TODO Handle data source disconnection
  }

  /**
   * This method should refresh all cache values with the values from the
   * data source and send them to the server.
   */
  @Override
  public void refreshAllDataTags() {
    // TODO Handle here the data refresh request. To communicate with the
    //      server you have to make use of the other methods provided by
    //      this class.
  }

  /**
   * This method should refresh the data tag cache value with the value
   * from the data source and send it to the server.
   *
   * @param dataTagId The id of the data tag to refresh.
   */
  @Override
  public void refreshDataTag(long tagId) {
    // TODO Handle here the data refresh request for the given tag id. To
    //      communicate with the server you have to make use of the other
    //      methods provided by this class.
  }
}
```

> **Please note!**

>**How does it work internally?**

>You have to be aware that your `EquipmentMessageHandler` class will be part of the DAQ configuration.
>So, only at runtime and *after* having received the XML configuration from the server, the DAQ Core will know where to find your handler implementation.
>
>The reason behind this is that on single DAQ process can deal with many different types of `EquipmentMessageHandler` classes at the same time.
>Moreover, those can be changed for the given process simply by changing its configuration:
```xml
...
  <EquipmentUnit id="1234" name="E_DEMO_DEMO1">
    <handler-class-name>cern.c2mon.driver.demo.DemoMessageHandler</handler-class-name>
    ...
  </EquipmentUnit>
...
```



#### How to implement connectToDataSource() from EquipmentMessageHandler?

The `connectToDataSource()` method is one of the _abstract_ methods provided by the `EquipmentMessageHandler.
The method is called when the core wants your module to start up and to connect to the data source.
In fact the name is therefore maybe a bit misleading, because in practice this method is the main initialization point of your module.

So, this method does normally the following steps:

. retrieves the data source configuration from the DAQ Core by calling getEquipmentConfiguration(),
. handles the connection to the data source,
. parses the list of ISourceDataTag objects and creates for each of them a subscription to the data source,
. registers the other equipment handlers (if implemented), like for instance the `ICommandRunner` or the `IEquipmentConfigurationChanger`.

This is of course a lot for a single method.
Best practice is therefore to delegate the work to sub classes in order to simplify the main logic.


> **Please note!**

>**When is this method called?**
>
>The `connectToDataSource()`` is only called once per equipment lifestyle.
>This means at the very beginning during the DAQ process start-up phase or after an equipment reconfiguration.




#### How to parse the configured list of data tags?

Theoretically every tag can be configured with a different `HardwareAddress` type (see also Defining a new HardwareAddress class), even if this is not really done in practice.

**Example, how to parse all configured tags for subscription:**
```java
@Override
public void connectToDataSource() throws EqIOException {
  // TODO Implement here the connection to your data source

  HardwareAddress harwareAddress;
  DemoHardwareAddress demoHardwareAddress;
  // parse HardwareAddress of each registered dataTag
  for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
    harwareAddress = dataTag.getHardwareAddress();
    if (dataTag.getHardwareAddress() instanceof DemoHardwareAddress) {
      demoHardwareAddress = (DemoHardwareAddress) dataTag.getHardwareAddress();

      // TODO Handle the subscription of this tag

    }
    else {
      String errorMsg = "Unsupported HardwareAddress: " + dataTag.getHardwareAddress().getClass();

      // Informs the server that this equipment has a problem
      getEquipmentMessageSender().confirmEquipmentIncorrect(errorMsg);

      throw new EqIOException(errorMsg);
    }
  }

  // Informs the server about the successful Equipment startup
  getEquipmentMessageSender().confirmEquipmentStateOK();
}
```




### Optional Equipment handler interfaces

The table below describes the different optional interfaces that can be implemented by a DAQ module in order to provide more functionality.
However, they are not required for a correct run of your DAQ module.
Any request made by the server for a non-provided functionality will simply be rejected.

| Interface | Description | Registration class |
| --------- | ----------- | ------------------ |
| IDataTagChanger | This interface should be implemented to react on Tag configuration changes sent from the server. | `IEquipmentConfigurationHandler` which can be retrieved by the `EquipmentMessageHandler`
| ICommandRunner |Provides one method `runCommand()` which is called, whenever the server is asking your DAQ module for executing a specific command | `IEquipmentConfigurationHandler` which can be retrieved by the `EquipmentMessageHandler`
| ICommandTagChanger | Provides three methods to deal with changes in the command configuration (add/remove/update) | `IEquipmentConfigurationHandler`, which can be retrieved by the `EquipmentMessageHandler` |
| IEquipmentConfigurationChanger | Provides one method `onUpdateEquipmentConfiguration()`, which is called when a configuration change for your DAQ module occurred. | `IEquipmentConfigurationHandler`, which can be retrieved by the `EquipmentMessageHandler` |
