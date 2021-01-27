---
layout:   post
title:    Configuration API
summary:  Learn how to configure the different entities in C2MON
---


C2MON centrally manages the configuration of data acquisition (DAQ) processes.
A DAQ receives its configuration from the server at startup, which has two main advantages:

* A DAQ only needs to know its unique name to request the configuration.
  No local configuration file or database access is required.
  This makes the deployment very lightweight and simplifies the setup of a cold standby.
* Since the server is orchestrating all DAQs it should never receive updates for unknown tags.
* If a DAQ supports live configuration, changes to its configuration are automatically propagated.

C2MON provides a simple and intuitive Java configuration API via the `ConfigurationService` that allows *creating*, *updating* or
*removing* `Process`, `Equipment` `SubEquipment` `DataTag` `RuleTag` and `Alarm` entities on-the-fly.

To acquire a reference to the `ConfigurationService`:

```java
ConfigurationService configurationService = C2monServiceGateway.getConfigurationService();
```

# Configuring Processes

Here is the simplest way of creating a new `Process`:

```java
ConfigurationReport report = configurationService.createProcess("P_EXAMPLE");
```

The method returns a `ConfigurationReport` which gives detailed information about the outcome of the configuration (as do all of the methods in the
`ConfigurationService`).

Note that an ID for the process will be generated automatically on the server, and an `AliveTag` and a `StatusTag` will be generated automatically.

If you need more control about the created entity (such as setting the ID manually, setting a description, etc.) you can use the
`Process#create` builder method:

```java
Process processToCreate = Process.create("P_EXAMPLE")
        .id(123L)
        .description("A short description")
        .build();

configurationService.createProcess(processToCreate);
```

## Updating a Process

Existing `Process` instances can be updated via the `Process#update(String)` or `Process#update(Long)` builder methods:

```java
Process processToUpdate = Process.update("P_EXAMPLE")
        .description("An updated description")
        .build();

configurationService.updateProcess(processToUpdate);
```

Note that not all properties of a `Process` are updateable, for example the ID. The builder method exposes methods only for those
properties which are updateable.

Also note that if you accidentally pass a `Process` instance created with `Process#create` to the `ConfigurationService#updateProcess` method,
an exception will be thrown. You must use the `update` builders when updating entities.

# Configuring Equipment

The following code shows the simplest way to create an `Equipment`:

```java
configurationService.createEquipment("P_EXAMPLE", "E_EXAMPLE",
        "org.example.MyEquipmentMessageHandler");
```

The first parameter expects the name of an existing `Process`. An equipment ID will be generated automatically, as well as a `CommFaultTag` and a `StatusTag`.

If you need more control about the created entity (such as setting the ID manually, setting a description, etc.) you can use the
`Equipment#create` builder method:

```java
Equipment equipmentToCreate = Equipment.create("E_EXAMPLE", "org.example.MyEquipmentMessageHandler")
        .id(234L)
        .description("A short description")
        .aliveTag(AliveTag.create("E_EXAMPLE:ALIVE").build(), 70000)
        .build();

configurationService.createEquipment("P_EXAMPLE", equipmentToCreate);
```

## Updating an Equipment

Existing `Equipment` instances can be updated via the `Equipment#update(String)` or `Equipment#update(Long)` builder methods:

```java
Equipment equipmentToUpdate = Equipment.update("E_EXAMPLE")
        .description("An updated description")
        .aliveInterval(80000)
        .build();

configurationService.updateEquipment(equipmentToUpdate);
```


# Configuring DataTags

The following code shows the simplest way to create a `DataTag`:

```java
configurationService.createDataTag("E_EXAMPLE", "TAG_EXAMPLE", Integer.class, new DataTagAddress());
```

!!! info "Note"
    A well-chosen naming convention will enable you to make searching for tags easier in the future.
    We suggest using a _folder-like_ structure with `/` as separator.
    Example: `serviceA/computer/mypc1/memory`

An ID for the tag will be generated automatically on the server. The first parameter expects the name of an existing `Equipment`.

If you need more control about the created entity (such as adding metadata, setting a description, etc.) you can use the
`DataTag#create` builder method:

```java
DataTag tagToCreate = DataTag.create("TAG_EXAMPLE", Integer.class, new DataTagAddress())
        .description("An example datatag")
        .metadata(Metadata.builder().addMetadata("myCustomKey", "someValue"))
        .build();

configurationService.createDataTag("E_EXAMPLE", tagToCreate);
```


# Configuring Alarms

The following code shows the simplest way to create an `Alarm`:

```java
configurationService.createAlarm("TAG_EXAMPLE", new RangeAlarmCondition(0, 100, true), "MyfaultFamily", "MyfaultMember", 0);
```

The first parameter expects the name of an already created `Tag`. An alarm ID will be generated automatically on the server.

`Alarm#create` and `Alarm#update` methods also exist in a similar manner as for the `DataTag`.

As initially explained in the [Alarm Overview section]({{ site.baseurl }}{% link docs/overview/alarms.md %}) C2MON currently provides two alarm condition classes, that is [RangeAlarmCondition](https://github.com/c2mon/c2mon/blob/master/c2mon-shared/c2mon-shared-client/src/main/java/cern/c2mon/shared/client/alarm/condition/RangeAlarmCondition.java) and [ValueAlarmCondition](https://github.com/c2mon/c2mon/blob/master/c2mon-shared/c2mon-shared-client/src/main/java/cern/c2mon/shared/client/alarm/condition/ValueAlarmCondition.java).

## Create custom alarm conditions
To write your own alarm condition class you have to extend the abstract [AlarmCondition](https://github.com/c2mon/c2mon/blob/master/c2mon-shared/c2mon-shared-client/src/main/java/cern/c2mon/shared/client/alarm/condition/AlarmCondition.java) class and implement your custom evaluation logic in `AlarmCondition#evaluateState(Object value)`.

Once you are done you have to add the code in a separate jar to the classpath, both on client and server side. 

# Configuring RuleTags

The following code shows the simplest way to create a `RuleTag`:

```java
configurationService.createRuleTag("RULE_EXAMPLE", "(#1000 < 0)|(#1000 > 200)[1],true[0]", Integer.class);
```

`RuleTag#create` and `RuleTag#update` methods also exist in a similar manner as for the `DataTag`.

# Configuring DeviceClasses

The following code shows the simplest way to create a `DeviceClass`:

```java
configurationService.createDeviceClass("DEV_CLASS_EXAMPLE");
```

A device class ID will be generated automatically. However, this device class will not contain any properties, fields 
or commands. To add these entities and for more control, use the `DeviceClass#create` builder method:

```java

DeviceClass deviceClass = DeviceClass.create("DEV_CLASS_EXAMPLE")
        .id(234L)
        .description("A short description")
        .addProperty("PROPERTY", "Description of the property")
            .addField("FIELD", "This field is added to the property with name PROPERTY")
        .addCommand("COMMAND", "Description of the command")
        .build();

configurationService.createDeviceClass(deviceClass);
```

Fields are always appended to the most recently added Property. So in the example below of `DEV_CLASS_2`, 
only `PROPERTY_2` will have fields, but not `PROPERTY_1` or `PROPERTY_3`:

```java

DeviceClass deviceClass = DeviceClass.create("DEV_CLASS_2")
        .addProperty("PROPERTY_1", "A property without fields")
        .addProperty("PROPERTY_2", "A property with fields")
            .addField("FIELD", "This field is added to the most recently added property, here PROPERTY_2")
        .addProperty("PROPERTY_3", "Another property without fields")
        .build();
```

 
Finally, remember that no two properties or commands may have the same name, and field names must be unique within the
parent property.


## Deleting DeviceClasses

You can easily remove a device class by its name or ID:

```java
configurationService.removeDeviceClass("DEV_CLASS_EXAMPLE");
configurationService.removeDeviceClassById(1245);
```

Take care when executing such a command: Deleting a device class will also remove all devices associated with this class!



# Configuring Devices

The following code shows the simplest way to create a `DeviceClass`:

```java
configurationService.createDevice("DEVICE_EXAMPLE", "PARENT_DEVICE_CLASS");
```

Alternatively, you can reference the parent device class by ID:

```java
configurationService.createDevice("DEVICE_EXAMPLE", 1234);
```

A device ID will be generated automatically. As when configuring the device class, use one of the `Device#create` 
builder methods  in order to add device properties, device commands or property fields.

There are several different types of device properties which can be configured: Properties or fields can reference Tag IDs, 
client rules, or constant values. 
Devices properties which contain fields are called "mapped properties" and do not have a specific value of their own. 
Rather, they can be considered to purely be containers for their fields. 

Device commands, on the other hand, reference Command Tag IDs.
  
The device builder offers dedicated methods for the individual types, as evident from the following example:

```java

DeviceClass deviceClass = DeviceClass.create("DEV_CLASS")
        .addProperty("CPU_LOAD", "The cpu load on server XYZ in percent.")
        .addProperty("SERVER_XYZ", "Information regarding a certain server")
                .addField("RESPONSIBLE_PERSON", "The person responsible for server XYZ")
                .addField("SOME_CALCULATION", "Some calculation on server XYZ")
        .addCommand("LIGHT_OFF", "Turn off the automatic light sensor in lab XYZ")
        .build();

configurationService.createDeviceClass(deviceClass);

Device device = Device.create("DEVICE_EXAMPLE", "DEV_CLASS")
        .addPropertyForTagId("CPU_LOAD", 302254L)
        .createMappedProperty("SERVER_XYZ")
                .addFieldForConstantValue("RESPONSIBLE_PERSON", "Ms. Administrator", ResultType.STRING)
                .addFieldForClientRule("SOME_CALCULATION", "(#123 + #234) / 2", ResultType.INTEGER)
        .addPropertyForConstantValue("constant_value_property: " + date, 2L, ResultType.LONG)
        .addCommand("LIGHT_OFF", 100548L)
        .build();

configurationService.createDevice(device);
```

The type of resulting values relevant for constant values and client rules. Possible result types are `Boolean`, `Double`, `Float`, `Integer`, `Long`, `Numeric` or `String`.

Each device property, device command and property field must correspond by name to a property, command or field of the 
parent device class. However, not all properties, commands and fields or the parent class need to be implemented in all
devices.

## Deleting Devices

You can delete a device by its ID:

```java
configurationService.removeDeviceById(235L);
```