---
layout:   post
title:    Writing JUnit tests for DAQ modules
Summary:  Explains how to make use of the DAQ testing framework.
---

Before starting reading this guide please make sure that you are at least familiar with the following guide:

* [Creating a new DAQ module from scratch](DAQ_module_developer_guide.md)

At the end of this guide you should be able to write JUnit tests for your DAQs.

# Introduction

When implementing a DAQ module it is a good practice to write JUnit tests in parallel to assure the code correctness of your new DAQ.
This will also significantly speed up your development, since problems can be detected much earlier.

Most of the functionality of your DAQ can be well tested in total isolation from the C2MOM system, by using the _C2MON DAQ testing framework_.


# DAQ testing framework dependency

In order to use DAQ testing framework for your JUnit tests, you need to include this dependency in your project's `pom.xml` :

```xml
<dependency>
    <groupId>cern.c2mon.daq</groupId>
    <artifactId>c2mon-daq-test</artifactId>
    <version>__insert_version_here__</version>
    <scope>test</scope>
</dependency>
```


# Example test template

The code sample below presents an example skeleton test class of `DemoMessageHandler`.
Please note the class needs to be annotated with the `@UseHandler` annotation, pointing to the class of the handler you want to test.
The presented template can be used as a start point for building more advanced tests, testing various DAQ use-cases.

**Example for test class:**
```java
// you will need the following imports
import cern.c2mon.daq.test.GenericMessageHandlerTest;
import cern.c2mon.daq.test.UseHandler;

// Specify the Handler class that is to be tested using @UseHandler annotation
@UseHandler(DemoMessageHandler.class)
public class DemoMessageHandlerTest extends GenericMessageHandlerTest {

    static Logger log = Logger.getLogger(DemoMessageHandlerTest.class);

    // reference to the instance of the handler to test
    DemoMessageHandler theHandler;

    @Override
    protected void beforeTest() throws Exception {
        log.info("entering beforeTest()..");

        // cast the reference (declared in the parent class) to the expected type
        theHandler = (DemoMessageHandler) msgHandler;

        // do any other initialization (e.g. initialize your mocks, start your services etc.. here)

        log.info("leaving beforeTest()");
    }

    @Override
    protected void afterTest() throws Exception {
        log.info("entering afterTest()..");
        theHandler.disconnectFromDataSource();


        // do the resource cleanup (if any) here

        log.info("leaving afterTest()");
    }

    ....
}
```



# How does the annotation work?

Once you annotate your test class with `@UseHandler` annotation, pointing to the handler class you wish to test, an instance of a handler will be created for
you automatically for every test declared in your test class.
Every test has of course to be annotated with the standard java JUnit `@Test` annotation.

In addition you need to instruct the framework which configuration should it use to initialize the handler at start-up.
You do this by annotating your test method with `@UseConf("your_configuration_file")`.

**Example for annotated test method:**
```java
import cern.c2mon.daq.test.UseConf;
import org.junit.Test;

...

    // e_demohandler_test1.xml configuration will be used for this test.
    @Test
    @UseConf("e_demohandler_test1.xml")
    public void test1() throws Exception {
       ...
    }

...
```


The configuration files are XML files with the format compatible with the one that your handler would normally receive from the C2MON server.
Here the framework injects the configuration for your handler, so that you can run your tests in isolation from any C2MON infrastructure.
Note that the configuration file only needs the `<EquipmentUnit>` section and you don't have to set the handler-class-name inside, since you already declared
 the class of your handler using `@UseHandler` annotation.

The configuration files for testing your handler should be put into /conf subfolder of your MessageHandler's folder.

**Example file structure:**
```xml
src/test/cern/c2mon/daq/demo/DemoMessageHandler.java

src/test/cern/c2mon/daq/demo/conf/e_demohandler_test1.xml
src/test/cern/c2mon/daq/demo/conf/e_demohandler_test2.xml
```

> **Tip: good practice**
>
>Create separate configuration files for each of the tests declared in your testing class, and choose meaningful names for them.




# Testing Use-Cases

Below we present the most frequently used testing scenarios, which should give you an overall idea on how the testing framework can be used, and how you can
build you own tests.


## Assuring the handler sends CommfaultTag at start-up as expected

Unless there are configuration problems, at start-up, your DAQ is expected to deliver a `CommfaultTag` value to the server, indicating if you are correctly
connected to the monitored device.
The example code presented below, shows how this scenario can be tested:

**Test class Example:**
```java
// you will need the following imports

// import easymock
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import static org.junit.Assert.assertEquals;

import cern.c2mon.daq.test.GenericMessageHandlerTest;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;

import org.junit.Test;
import org.easymock.Capture;

// Specify the Handler class that is to be tested using @UseHandler annotation
@UseHandler(DemoMessageHandler.class)
public class DemoMessageHandlerTest extends GenericMessageHandlerTest {

  @Test
  @UseConf("e_demohandler_test1.xml")   // e_myhandler_test1.xml configuration will be used for this test.
  public void test1() throws Exception {

     // create junit captures for the tag id, value and message (for the commmfault tag)
     Capture<Long> id = new Capture<Long>();
     Capture<Boolean> val = new Capture<Boolean>();
     Capture<String> msg = new Capture<String>();

     // message sender's sendCommfaultTag is expected to be called
     // the DAQ is expected to send commfault tag once it is initialized
     messageSender.sendCommfaultTag(EasyMock.capture(id), EasyMock.capture(val), EasyMock.capture(msg));

     // it should be called only once
     expectLastCall().once();

     // record the mock
     replay(messageSender);

     // call yout handler's connectToDataSource() - in real operation the DAQ core will do it!
     theHandler.connectToDataSource();

     Thread.sleep(2000);

     // verify that messageSender's interfaces were called according to what has been recorded
     verify(messageSender);

     // check the message of the commfault tag is as expected
     assertEquals(
            "failed to connect to MBean service: "
            + "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi Exception caught: "
            + "Authentication failed! Invalid username or password", msg.getValue());

     // check the id of the commfault tag is correct
     assertEquals(107211L, id.getValue().longValue());
     // ..and the value
     assertEquals(false, val.getValue());
  }
}
```


## Example Configuration:

Below we listed the e_demohandler_test1.xml file as example:

```xml
<EquipmentUnit id="5250" name="E_JMX_JMX1">
  <handler-class-name>automatically-set-by-test</handler-class-name>
  <commfault-tag-id>107211</commfault-tag-id>

  <commfault-tag-value>false</commfault-tag-value>
  <address>service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi;user1;wrongpassword;30</address>
  <SubEquipmentUnits>
  </SubEquipmentUnits>

  <DataTags>
    <DataTag id="54675" name="BE.TEST:TEST1" control="false">
      <data-type>Integer</data-type>
      <min-value data-type="Integer">0</min-value>
      <max-value data-type="Integer">64000</max-value>
      <DataTagAddress>
        <HardwareAddress class="cern.c2mon.shared.common.datatag.address.impl.JMXHardwareAddressImpl">
          <!-- put the fields you need according to the type of the address you are using -->
        </HardwareAddress>
        <time-to-live>3600000</time-to-live>
        <value-deadband-type>1</value-deadband-type>
        <value-deadband>1.0</value-deadband>
        <priority>2</priority>
        <guaranteed-delivery>false</guaranteed-delivery>
      </DataTagAddress>
    </DataTag>

   <DataTag id="54676" name="BE.TEST:TEST2" control="false">
      <data-type>Integer</data-type>
      <min-value data-type="Integer">0</min-value>
      <max-value data-type="Integer">64000</max-value>
      <DataTagAddress>
        <HardwareAddress class="ch.cern.tim.shared.datatag.address.impl.JMXHardwareAddressImpl">
          <!-- put the fields you need according to the type of the address you are using -->
        </HardwareAddress>
        <time-to-live>3600000</time-to-live>
        <value-deadband-type>1</value-deadband-type>
        <value-deadband>1.0</value-deadband>
        <priority>2</priority>
        <guaranteed-delivery>false</guaranteed-delivery>
      </DataTagAddress>
    </DataTag>

  </DataTags>
  <CommandTags>
  </CommandTags>
</EquipmentUnit>
```
