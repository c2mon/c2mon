---
layout:   post
title:    Client Configuration Shell
summary:  Learn how to configure C2MON using the Client Configuration Shell
---
 
Reconfiguration to a C2MON server instance can be requested at runtime using the dedicated  Client Configuration Shell. 
The interactive shell offers the following methods:

* `get-tags <URI>` searches for a DataTag corresponding to the given URI within the existing C2MON configuration based on the Tag name. 
   If no corresponding Tag can be found, a request for creating a DataTag corresponding to the URI is sent. 
   If no Process or Equipment exists that fit the configuration, they are created on-the-fly.
   Batch commands are supported by chaining multiple URIs together divided by a semicolon  `;`. 

* `delete-tag <URI>` requests the deletion of a DataTag corresponding to the given URI, if such a Tag exists.

The C2MON Client Configuration Shell supports DataTags only.

# URI

The Shell relies on URIs to reference and describe DataTags. The URI must be given in the form:

```bash
<SCHEME>://<HOST>:<PORT>/<PATH>?<QUERY=OPTION>&<QUERY2=OPTION>...
```

The first part of the URI `<SCHEME>://<HOST>:<PORT>/<PATH>` is equivalent to the Tag's "hardware address". 
The hardware address is described in more detail in [Creating a new DAQ module from scratch]({{ site.baseurl }}{% link  docs/user-guide/daq-api/daq-module-developer-guide.md %}).  


Queries can be used to add configuration parameters to the Tag, and can be chained using the ampersand `&`. 
For example, the name and descriptions of the tag can be specified using `?tagName=TAG_EXAMPLE&description=a-sample-tag`. 
Some configuration parameters depend on the DAQ module corresponding to the DataTag, while others are universal:
 

|        	    | Parameter         	| Value Description     | Required? | 
|--------------	|---------------------	| --------------------	| --------- | 
| **General**	| tagName           	| String         	    | no        |
|          	    | dataType          	| Java class name       | no        |
|          	    | description       	| String          	    | no        |
| **OPC UA** 	| itemName        	    | String         	    | yes       |
|          	    | commandType      	    | METHOD, CLASSIC  	    | no        |
|          	    | setNamespace    	    | Integer         	    | no        |
|          	    | setAddressType        | STRING, NUMERIC, GUID | no        |
| **DIP**     	| publicationName       | String         	    | yes       |
|          	    | fieldName      	    | String         	    | no        |
|          	    | fieldIndex    	    | Integer         	    | no        |
| **REST**     	| url                   | String         	    | yes       |
|             	| mode            	    | GET, POST        	    | no        |
|              	| getFrequency    	    | Integer         	    | no        |
|              	| postFrequency    	    | Integer         	    | no        |

In addition to the here listed query keys, it is possible to pass any query corresponding one of the following methods:
* the [DataTag.CreateBuilder](https://gitlab.cern.ch/c2mon/c2mon/-/blob/master/c2mon-shared/c2mon-shared-client/src/main/java/cern/c2mon/shared/client/configuration/api/tag/DataTag.java), 
* the [DataTagAddress](https://gitlab.cern.ch/c2mon/c2mon/-/blob/master/c2mon-shared/c2mon-shared-common/src/main/java/cern/c2mon/shared/common/datatag/DataTagAddress.java), 
* and the [protocol-specific HardwareAddress class](https://gitlab.cern.ch/c2mon/c2mon/-/blob/master/c2mon-shared/c2mon-shared-common/src/main/java/cern/c2mon/shared/common/datatag/DataTagAddress.java).

For example, to set namespace of a OPC UA tag, one may append `?setNamespace=4`.

# Mappings

We must specify the Process and Equipment that a new DataTag should be created for within the C2MON Client Configuration Shell. 
This is done my defining appropriate *Mappings* within the C2MON Client properties. 
Regular expressions allow the fine-grained which Processes and Equipments an URIs is mapped to:

The following example associates any URI starting with the scheme `opc.tcp` with an OPC UA DAQ Process "P_DYNOPCUA" and the Equipment "E_DYNOPCUA", and any URI starting with `dip` with the Process "P_DYNDIP" and equipment "E_DYNDIP".
If DataTag is configured for a Process or Equipment which does not currently exist, the required entities are created on-the-fly. 

```yaml
c2mon:
    client:
        dynconfig:
            mappings:
            -   processName: P_DYNDIP
                processID: 1001
                processDescription: DIP sample Process
                equipmentName: E_DYNDIP
                equipmentDescription: DIP sample Equipment
                uriPattern: ^dip.*

            -   processName: P_DYNOPCUA
                processID: 10002
                processDescription: OPC UA sample Process
                equipmentName: E_OPCUA
                equipmentDescription: OPC UA sample Equipment
                uriPattern: ^opc.tcp.*
```

# Remote interaction through JMX and HTTP

<!---- ## SSH  ## JMX and HTTP---->



The functions of the C2MON Client Configuration Shell are exposed through JMX and HTTP using [Jolokia](https://jolokia.org/) as MBean operations.

Once the shell starts, the available endpoints can be inspected and accessed under the following locators:

* JMX: `service:jmx:rmi:///jndi/rmi://<HOST'S_IP>:<PORT_NUMBER>/jmxrmi`
* HTTP: `<HOST-IP>:<HOST-PORT>/actuator`

The default port is 8912. This can be changed by setting the property `management.server.port`.

The MBean `cern.c2mon:name=ClientConfigurationShell,type=Config` offers the `getTagsForURI` and `deleteTagForURI`, which correspond to `get-tags` and `delete-tag` respectively.

An easy way to remotely interact with the Shell is through HTTP. A Tag can conveniently be created or deleted with the following GET requests:

* Create a Tag: `http://<HOST-IP>:<HOST-PORT>/actuator/jolokia/exec/cern.c2mon:name=ClientConfigurationShell,type=Config/getTagsForURI/<URI>` 
* Delete a Tag: `http://<HOST-IP>:<HOST-PORT>/actuator/jolokia/exec/cern.c2mon:name=ClientConfigurationShell,type=Config/deleteTagForURI/<URI>`

Please refer to the [Jolokia documentation](https://jolokia.org/reference/html/protocol.html) for more information about the protocol and constructing requests.