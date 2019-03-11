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
configurationService.createAlarm("TAG_EXAMPLE", new ValueCondition(Integer.class, 1), "faultFamily", "faultMember", 0);
```

The first parameter expects the name of an already created `Tag`. An alarm ID will be generated automatically on the server.

`Alarm#create` and `Alarm#update` methods also exist in a similar manner as for the `DataTag`.

# Configuring RuleTags

The following code shows the simplest way to create a `RuleTag`:

```java
configurationService.createRuleTag("RULE_EXAMPLE", "(#1000 < 0)|(#1000 > 200)[1],true[0]", Integer.class);
```

`RuleTag#create` and `RuleTag#update` methods also exist in a similar manner as for the `DataTag`.
