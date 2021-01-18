---
layout:   post
title:    OPC UA DAQ
summary:  Provides an overview over the OPC UA DAQ
---

The C2MON teams published and maintains some open source DAQs for your basic data acquisition needs, including the [OPC UA DAQ](https://github.com/c2mon/c2mon-daq-opcua).
This Module allows collect data from a range of industrial SCADA systems via OPC UA.
The modern and flexible design of this new technology has many advantages over its predecessors including platform independence, higher reliability, and a comprehensive security model.

The OPC UA DAQ offers rich configurability to suit the varying needs of different OPC UA servers, and exposes relevant metrics and endpoints for monitoring.
It relies on the [Eclipse Milo](https://github.com/eclipse/milo) library (Eclipse Public License 1.0).

The latest tarball release can be downloaded from [CERN Nexus Repository](https://nexus.web.cern.ch/nexus/#nexus-search;gav~cern.c2mon.daq~c2mon-daq-opcua~~tar.gz~).

# Configuration

The `EquipmentMessageHandler` class to be specified during the Equipment creation is: `cern.c2mon.daq.opcua.OPCUAMessageHandler`. 

The DAQ can be configured externally through Spring Boot as described [here](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config). 
These options are valid for all Equipments of a DAQ Process and can can be overwritten for a particular Equipment by appending the respective option to the EquipmentAddress. 
For example, `URI=opc.tcp://hostname:2020;trustAllServers=true` will skip validation of the incoming server certificate against the stored certificate authority **only** for the Equipment with that address.

| Category          | Property                  | Description                                                                                                                                                                                                                                                                                                                                                                                           |
|-------------------|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **General**       | restartDelay              | The delay in milliseconds before restarting the DAQ after an Equipment change if the change affected the EquipmentAddress.                                                                                                                                                                                                                                                                            |
|                   | requestTimeout            | The timeout in milliseconds indicating for how long the client is willing to wait for a server response on a single transaction in milliseconds. The maximum value is 5000.                                                                                                                                                                                                                           |
|                   | queueSize                 | The maximum number of values which can be queued in between publish intervals of the subscriptions. If more updates occur during the time frame of the DataTags’ time deadband, these values are added to the queue. The fastest possible sampling rate for the server is used for each MonitoredItem.                                                                                                |
|                   | aliveWriterEnabled        | The AliveWriter ensures that the SubEquipments connected to the OPC UA server are still running, and sends regular AliveTags to the C2MON Core.                                                                                                                                                                                                                                                       |
| **Redundany**     | redundancyMode            | The redundancy handler mode to use (Part of the FailoverMode enum). A ConcreteController will be resolved (within ControllerFactory) according to this value, instead of querying the the server’s AddressSpace for the appropriate information. Can be for speedup to avoid querying the server for its redundancy mode upon each new connection, and to support vendor-specific redundancy modes.   |
|                   | redundantServerUris       | URIs of redundant servers to use instead of the reading the URIs from the server’s address space.                                                                                                                                                                                                                                                                                                     |
|                   | failoverDelay             | The delay before triggering a failover after a Session deactivates. Set to -1 to not use the Session status as a trigger for a failover.                                                                                                                                                                                                                                                              |
|                   | connectionMonitoringRate  | The publishing rate for the subscriptions to Nodes monitoring the connection in redundant server sets in seconds.                                                                                                                                                                                                                                                                                     |
| **Security**      | trustAllServers           | The client will make no attempt to validate server certificates, but trust servers. If disabled, incoming server certificates are verified against the certificates listed in pkiBaseDir.                                                                                                                                                                                                             |
|                   | pkiBaseDir                | Specifies the path to the PKI directory of the client. If the“trusted” subdirectory in pkiBaseDir contains either a copy of either the incoming certificate or a certificate higher up the Certificate Chain, then the certificate is deemed trustworthy.                                                                                                                                             |
|                   | certifierPriority         | [NO_SECURITY, GENERATE, LOAD] <br> Connection with a Certifier associated with the element will be attempted in decreasing order of the associated value until successful. If the value is not given then that Certifier will not be used.                                                                                                                                                            |
| **Certification** | applicationName                                                                           | The name of the application to specify in the connection request and a generated certificate.                                                                                                                                                                                                                                         |
|                   | applicationUri                                                                            | Must match the applicationUri of a loaded certificate exactly, if applicable.                                                                                                                                                                                                                                                         |
|                   | organization  <br> organizationalUnit <br> localityName <br> stateName <br>  countryCode  | Properties to use in the generation of a self-signed certificate.                                                                                                                                                                                                                                                                     |
|                   | keystore.type <br> keystore.path <br> keystore.password <br> keystore.alias               | Properties required to load a certificate and private key from a keystore file.                                                                                                                                                                                                                                                       |
|                   | pki.privateKeyPath <br> pki.certificatePath                                               | Paths to the PEM-encoded certificate and private key files respecively.                                                                                                                                                                                                                                                               |
| **Modifying server information**  | hostSubstitutionMode                                                      | [NONE, SUBSTITUTE_LOCAL, APPEND_LOCAL, SUBSTITUTE_GLOBAL, APPEND_GLOBAL] OPC UA servers are commonly configured to return a local host address in EndpointDescriptions returned on discovery that may not be resolvable to the client (e.g. “127.0.0.1” if the endpoint resides in the same server. Substituting the hostname allows administrators to such server configurations. `Global` refers to the configured `globalHostName`, while `local` uses the hostname within the address used for discovery. |
|                                   | portSubstitutionMode                                                      | [NONE, LOCAL, GLOBAL] The port can be substituted with the port in the discovery address, or by the configured `globalPort`.                                                                                                                                                                                                          |
|                                   | globalHostName                                                            | The hostname to append or substitute if the `hostSubstitutionMode` is set to a global option. If the `hostSubstitutionMode` is global no `globalHostName` is set, the host is not substituted.                                                                                                                                        |
|                                   | globalPort                                                                | The port to substitute if the `portSubstitutionMode` is `global`.                                                                                                                                                                                                                                                                     |
|                                   | timeRecordMode                                                            | [SERVER, SOURCE, CLOSEST] With every value update, the OPC UA server may return a server timestamp, a source timestamp, or both. SERVER and SOURCE prefer the corresponding timestamp,and fall back to the other in case that the value is not set. CLOSEST uses the timestamp that is closer to the system time.                     |
| **Retry**         | retryDelay                | The initial delay before retrying a failed service call. The time in between retries is multiplied by retryMultiplier on every new failure, until reaching the maximum time of `maxRetryDelay`.                                                                                                                                                                                                       |
|                   | retryMultiplier           | On each new failed attempt, the delay time before another call is multiplied by `retryMulitplier` starting with "retryDelay" and up to a maximum of `maxRetryDelay`.                                                                                                                                                                                                                                  |
|                   | maxRetryDelay             | The maximum delay when retrying failed service calls.                                                                                                                                                                                                                                                                                                                                                 |
|                   | maxRetryAttempts          | The maximum amount of attempts to retry failed service calls in milliseconds. This does NOT include recreating subscriptions, and the failover process in redundant server setups.                                                                                                                                                                                                                    |

# Architecture

The OPC UA DAQ is structured into a set of components: independent subsystems with the ability to interface with the rest of the system. 
The components correspond conceptually to the project's software packages. 
A high-level overview over the components and their interactions is given below.

![opc-ua-daq-architechture]({{ site.baseurl }}{% link assets/img/user-guide/daq-api/opc-ua-architecture.png %})

| Component | Responsibility | Interface |
|-----------|----------------|---------- |
| <<**external**>> DAQ Core | Manages the life cycle of a DAQ Process within a C2MON deployment, performs common actions through *EquipmenntMessageHandler* and handles connection to the C2MON server layer. There is one DAQ Core instance per Process. | **EquipmentMessageSender:**  Notifies the DAQ Core about the state of the equipment, connection and new tag values. |
| <<**external**>> Application Properties | Contains the application properties passed along through a Spring properties file, or as system and environment variables. | **SpEL:** Spring wires the information directly into Config on system startup. While Spring allows all components to access the application properties directly through Spring Expression Language [SpEL](docs.spring.io/spring/docs/3.0.x/reference/expressions.html), all access should occur through the Config component. |
| <<**external**>> OPC UA Server | Gateway to the data source or sources. | **OPC UA Service Calls:** Access through the Eclipse Milo SDK. | 
| OPCUAMessageHandler | Entry point and coordinator for the OPC UA DAQ. It is instantiated and called by the DAQ Core to start or stop the data collection process, change the Equipment configuration, or to run commands. It initializes the *Controller* and communicates Data- or CommandTags to the *TagHandlers*. It notified the *MessageSender* if the innitial connection to the data source failed. | **EquipmentMessageHandler:** a composite of the dedicated interfaces *ICommandTagRunner* and *IEquipmentConfigurationChanger* as well as the abstract *EquipmentMessageHandler* class, all in the DAQ Core project. |
| OPCUAMessageSender |Relays messages to the DAQ Core's EquipmentMessageSender in a normalized form with suspicious messages appropriately filtered and logged. | **MessageSender:** Sends updates regarding EquipmentState and Tag activity. |
| Config | Stores all user or system properties for the application and provides fallback values. It is used by various other components in several use cases such as certificate handling, timeout definitions, and more. | **AppConfigProperties:** Access configuration values. |
| TagHandling | The only application layer beyond *OPCUAMessageHandler* directly concerned with the C2MON Tag format. The *TagHandling* component is a behavioral unit composed of subcomponents  dealing with DataTags, CommandTags, and ControlTags respectively. | **TagHandlers:** A composite interface of the external *IDataTagChanger* and *ICommandTagChanger* as well as the internal *DataTagHandler*, *CommandTagHandler* and *AliveWriter*. |
| Mapping | Associates the C2MON Tag format with the custom *ItemDefinition* format, and maintains an internal state of subscribed Tags. | **TagSubscriptionManager:** Add or remove the association of DataTags and OPC UA clientHandles and manage the internal state of Subscriptions. **TagSubscriptionReader:** Access currently subscribed DataTags and retrieve the associated *ItemDefinition* or vice versa. |
| Control | Represents a one-to-many mapping to *Endpoint*. Handles the initializing, failover and monitoring actions for standalone servers or servers in Redundant Server Sets. | **Controller:** Execute life cycle management, subscription, read, write or method actions on the appropriate controller for a server architecture. **SessionActivityListener:** This listener is notified when the Session activates or deactivates. This information may trigger Failovers. |
| Connection | Handles all interaction with a single server including the consolidation of security settings and retries of unsuccessful service calls. Maintains a mapping between the per-server time deadbands and associated subscription IDs. Informs the *MessageSender* of the state of connection and new values, mapping from OPC UA nodes to DataTags via *TagSubscriptionReader*. | **Endpoint:** Trigger read, write, subscribe or unsubscribe operations on the associated server Endpoint. *OPC UA Callbacks:*  receive notifications by the OPC UA Server regarding connection or Subscription states and value updates. This composite interface includes *SessionActivityListener*. |
| Security | Loads a certificate or generates a self-signed certificate on the fly as prompted by the Connection component. Certifies the initial request to establish a connection through a SecureChannel appropriately. | **Certifier:** certify the SecureChannel request with a loaded or generated certificate, or without certification. |

# Redundancy

OPC UA redundancy is supported in Cold Failover mode, which can be used as a fallback for higher redundancy modes. 
By default, the OPC UA DAQ  will only reconnect to a redundant server if the previously active server's ServiceLevel node shows a value below 200, or if its ServerState shows a value other than `Running` or `Unknown`. If the configuration parameter By default, the OPC UA DAQ  will only reconnect to a redundant server if the previously active server's ServiceLevel node shows a value below 200, or if its ServerState shows a value other than `Running` or `Unknown`.

It is possible to configure the OPC UA DAQ to attempt reconnection also upon disconnection to the active server through the configuration parameter `failoverDelay`. This parameter specifies the amount of time that an OPC UA DAQ will wait for a server to respond after losing connection before attempting to connect to redundant servers.

# Metric Exposure

The OPC UA DAQ exposes health, dumps, and info through Spring actuator endpoints, accessible through JMX and HTTP via Jolokia.

In addition to Spring actuators, the following metrics are exposed:

* `c2mon_daq_opcua_tag_updates_valid`
* `c2mon_daq_opcua_tag_updates_invalid`
* `c2mon_daq_opcua_tags_per_subscription`
* `system_network_bytes_received`
* `system_network_bytes_sent`
* `system_network_packets_received`
* `system_network_packets_sent`

Those metrics prefixed by `system.network` are gathered through the Operating System and Hardware Information library [OSHI](https://github.com/oshi/oshi).
The Grafana dashboard included in the file `src/resources/grafana_dashboard.json` provides an overview over relevant system and DAQ metrics.  

## JMX

By default, JMX is exposed through port 8913. To change this port, modify the included Dockerfile and build a tarball. 
When running with Docker, this port must be exposed.
Set the following application property:

  ```bash
  spring.jmx.enabled=true
  ```

The process can now be accessed through JMX under **service:jmx:rmi:///jndi/rmi://[YOUR HOST]:8913/jmxrmi**

## HTTP

To expose the application through HTTP, set the following application properties and expose [HTTP PORT].

  ```bash
  management.endpoints.web.exposure.include=[TO EXPOSE],jolokia
  management.server.port=[HTTP PORT] 
  ```
The process can now be accessed through HTTP under **http://[YOUR HOST]:[HTTP PORT]/actuator/jolokia**
See the official [Jolokia documentation](https://jolokia.org/documentation.html) for more details.


## Operations 

The following operations are exposed through JMX and Jolokia, and can be triggered remotely:

*   reading out values from single DataTags
*   starting and stopping the AliveWriter
*   examining the type of redundancy Controller currently in use
*   triggering a failover process in the case of a redundant server setup