---
layout:   post
title:    Database
summary:  How to configure an external relational database for C2MON.
---

By default C2MON start with an embedded HSQL database for prototyping and development purposes.

> **Please note!** <br>
The HSQL in-memory database is by default not persisted on disk.
Therefore, all configuration- and history data are lost when C2MON is stopped.
How-to persist the HSQL storage is explained here [below](#setup-c2mon-with-persistent-hsql).

# Setup the Database schema

Except for HSQL, C2MON **is not** automatically generating the database schema for the given account.
This step is manual!

All you have to do is to execute the two SQL scripts for your database type.
Currently, C2MON supports Oracle, MySQL and HSQL as relational database next to [Elasticsearch](elasticsearch) archiving.

| Database | Cache backup schema               | History schema                      |
|----------|-----------------------------------|-------------------------------------|
| Oracle   | [SQL script][oracle-cache-schema] | [SQL script][oracle-history-schema] |
| MySQL    | [SQL script][mysql-cache-schema]  | [SQL script][mysql-history-schema]  |
| HSQL     | automatically                     | automatically                       |

It is possible to separate the cache backup tables from the history tables by using two database accounts.
However, here we will focus on the simple approach with one account.

## Running C2MON with other databases

C2MON is using [MyBATIS](http://www.mybatis.org/mybatis-3/) as persistence framework,
which makes it relatively easy to support other databases.
Due to our limited resources we focus for now on these three, but would be happy to
add in a collaborative attempt more databases.

# Setup C2MON with persistent HSQL
To persist the HSQL data on hard disk the following property has to be set:

**C2MON properties**

```shell
c2mon.server.jdbc.url=jdbc:hsqldb:hsql://localhost/c2mondb;sql.syntax_ora=true
```
These settings can also be set as Java VM options with the ```-D``` parameter or as [Spring Boot environment variables](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

See also [Configuring the module](/user-guide/client-api/history/#configuring-the-module).

!!! warning "Be careful!"
    If the URL contains ``hsql://``, C2MON uses an in-memory HSQL with a hardcoded path at ``/tmp/c2mondb``.
    The open [Issue 158](https://gitlab.cern.ch/c2mon/c2mon/issues/158) addresses this problem.


# Setup C2MON with Oracle

> **Oracle drivers cannot be distributed under an open-source license.**

>To connect C2MON to an Oracle database, you must download the Oracle JDBC driver libraries (typically ojdbc.jar and orai18n.jar) and copy them under ```/c2mon-server/lib```.  
Oracle drivers are typically available on the [Oracle Tech Network website](http://www.oracle.com/technetwork/database/features/jdbc/index.html).



## Configuring

To persist data in an Oracle instance (v11 or later) at least the following properties have to be set:

```
c2mon.server.jdbc.driver-class-name=oracle.jdbc.driver.OracleDriver
c2mon.server.jdbc.url=jdbc:oracle:thin:@myhost:1521:orcl
c2mon.server.jdbc.username=scott
c2mon.server.jdbc.password=tiger
```
These settings can also be set as Java VM options with the ```-D``` parameter or as [Spring Boot environment variables](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).



# Setup C2MON with MySQL

To persist data in a MySQL instance (v 5.7 minimum) at least the following properties have to be set:

```
c2mon.server.jdbc.driver-class-name=com.mysql.jdbc.Driver
c2mon.server.jdbc.url=jdbc:mysql://localhost/c2mon
c2mon.server.jdbc.username=admin
c2mon.server.jdbc.password=<your password>
```

These settings can also be set as Java VM options with the ```-D``` parameter or as [Spring Boot environment variables](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).


# Database structure

A detailed overview about the different C2MON database tables.

---

Usually, the server makes only use of these tables at startup to initialise the In-Memory cache.
Furthermore, the database is used to backup every 10 seconds the latest tag updates for eventual cache recovery.

The tables are completely controlled by the server layer and administrators should not try to do manual changes at runtime.
In general, configuration changes should always be done through the server Configuration module.

However, for testing purposes it might be interesting to directly manipulate the tables.

> **Please note!** <br>
The listed tables are only read at startup in case the In-Memory cache has not yet been initialised.
Apart from that, the server does only depend on the database access for making data backups.



# The DATATAG table

The DATATAG table is a non-normalised table in order to facilitate the write of search queries.
The table contains all configured DataTags, ControlTags and RuleTags.
Only CommandTags are stored in a separate table.

| Columns Name | Description | Type | Not NULL? |
| ------------ | ----------- | ---- | --------- |
| TAGID | Unique tag identification key | INTEGER | x |
| TAGNAME | Unique tag name | VARCHAR(255) | x |
| TAGDESC | A short static description of the tag | VARCHAR(100) |
| TAGMODE | Indicates the mode of the tag. Only the following values are allowed: <br> 0 - OPERATIONAL<br> 1 - MAINTENANCE<br> 2 - TEST | INTEGER | x |
| TAGDATATYPE | The value type of the data which will be received by the given tag. The latest value will be stored as backup every 10 seconds in the TAGVALUE column. We support all raw data values, arrays and complex Beans (stored in JSON format) | VARCHAR(200) | x |
| TAGCONTROLTAG | This flag indicates whether the tag is a ControlTag or not. Only the following values are allowed: <br> 0 - No control tag<br> 1 - The tag is a control tag | INTEGER | x |
| TAGVALUE | The latest value that will be stored by C2MON as backup every 10 seconds. | VARCHAR(4000) |
| TAGVALUEDESC | The tag value description that was received from the DAQ for the given value. | VARCHAR(1000) |
| TAGTIMESTAMP | The source timestamp of the given value or null if the update was not generated by the source (e.g. in case of a communication fault) | TIMESTAMP(6) |
| TAGDAQTIMESTAMP | The time when the DAQ has received the tag value update or null if the update was not generated by the DAQ (e.g. in case of a communication fault) | TIMESTAMP(6) |
| TAGSRVTIMESTAMP | The time when the server has updated the tag value in the In-Memory cache. This timestamp normally always set. | TIMESTAMP(6) |
| TAGQUALITYCODE | **Obsolete!** Will be removed with the next revision | INTEGER |
| TAGQUALITYDESC | A JSON string set by the server containing all quality flags + description that are currently set. If everything is OK, this field will only contain empty brackets {}.<br>Example for a tag flagged with bad quality:<br>`{"VALUE_OUT_OF_BOUNDS":"source value is out of bounds (min: -10.0 max: 50.0)"}` | VARCHAR(1000) |
| TAGRULE | In case that the tag is a RuleTag this field will contain the rule as string. <br>Example:<br> `(#163174 > 6)[0], (#163174 <= 6) & (#163174 >= 5)[2], (#163174 < 5)[3]` | VARCHAR(4000) |
| TAGRULEIDS | In case the given tag is used within one or many rules, the server will store here the RuleTag IDs in a comma separated list.<br>Example:<br>`90534, 90406, 163442` | VARCHAR(500) |
| TAG_EQID | The unique ID of the equipment, to which the tag is associated. **This field is empty**, if the tag is:<br> a RuleTag<br> a Process AliveTag<br> a Process StatusTag | INTEGER |
| TAGMINVAL | In case the tag type is numeric it is possible to define a minimum value. The tag will then be invalidated, if the received value is below the defined minimum. | VARCHAR(4000) |
| TAGMAXVAL | In case the tag type is numeric it is possible to define a maximum value. The tag will then be invalidated, if the received value is above the defined maximum. | VARCHAR(4000) |
| TAGUNIT | A free text unit definition. <br>Examples: `kg`, `Celsius`, `km/h` | VARCHAR(50) |
| TAGSIMULATED | This flag indicates whether the current tag value is real or simulated:<br> 0 - Not simulated<br>1 - Simulated value | INTEGER |
| TAGLOGGED | This flag indicates whether the data updates received for this tag shall be kept in the history table or not. Usually this is not the case for AliveTags, since it is later on possible to compute the uptime statistic from the StatusTags<br>0 - tag values not logged<br> 1 - tag values logged | INTEGER |
| TAGADDRESS | The hardware address as XML of the given DataTag or AliveTag. Please notice that RuleTags, StatusTags and CommFaultTags do not have by definition an address. Even for (Sub-)Equipment AliveTags it is not absolutely mandatory to define an address, since the alive trigger could be implemented without an explicit subscription. <br>Here an example of how the hardware address XML string could look like:<br>`<DataTagAddress>`<br>&nbsp;&nbsp;&nbsp;&nbsp;`<HardwareAddress class="cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl">`<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`<block-type>5</block-type>`<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`<word-id>9</word-id>`<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`<bit-id>10</bit-id>`<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`<physical-min-val>0.0</physical-min-val>`<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`<physical-max-val>0.0</physical-max-val>`<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`<resolution-factor>1</resolution-factor>`<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`<native-address>INT999</native-address>`<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`<command-pulse-length>0</command-pulse-length>`<br>&nbsp;&nbsp;&nbsp;&nbsp;`</HardwareAddress>`<br>&nbsp;&nbsp;&nbsp;&nbsp;`<time-to-live>9999999</time-to-live>`<br>&nbsp;&nbsp;&nbsp;&nbsp;`<priority>7</priority>`<br>&nbsp;&nbsp;&nbsp;&nbsp;`<guaranteed-delivery>true</guaranteed-delivery>`<br>`</DataTagAddress>` | VARCHAR(4000) |
| TAGDIPADDRESS | *Only for CERN internal usage. Should be removed from this table one day* | VARCHAR(500) |
| TAGJAPCADDRESS | *Only for CERN internal usage. Should be removed from this table one day* | VARCHAR(500) |
| TAGMETADATA | The metadata of the tag, stored as JSON string | VARCHAR(4000) |





# The PROCESS table

| Columns Name | Description | Type | Not NULL? |
| ------------ | ----------- | ---- | --------- |
| PROCID | Unique Process identification key | INTEGER | x |
| PROCNAME | Unique Process name | VARCHAR(60) | x |
| PROCDESC | Process description | VARCHAR(100) |
| PROCSTATE_TAGID | ID of Process StatusTag | INTEGER | x |
| PROCALIVE_TAGID | ID of Process AliveTag | INTEGER |
| PROCALIVEINTERVAL | Alive interval (heartbeat) in milliseconds. | INTEGER |
| PROCMAXMSGSIZE |This value determines the amount of messages that the given DAQ Process is allowed to accumulate before sending it to the C2MON server tier. Please notice, that the message buffering mechanism is also influenced by the PROCMAXMSGDELAY value (see below).<br>A good default value is 100. | INTEGER | x |
| PROCMAXMSGDELAY | This value will set the maximum allowed delivery delay in milliseconds of the internal DAQ process message buffer for received source value updates.<br>The minimum is currently hard-coded to 200 milliseconds. A good default value is 1000. Please read also the section about Message prioritisation. | INTEGER | x |
| PROCCURRENTHOST | C2MON stores here the name of the host on which the DAQ Process is running. | VARCHAR(25) |
| PROCSTATE | The current Process status set by the server, which can also be retrieved from the corresponding StatusTag.<br>Possible status are:<br> DOWN, RUNNING, RUNNING_LOCAL, STARTUP |VARCHAR(20) |
| PROCSTARTUPTIME | The time, when the DAQ Process has been started | TIMESTAMP(6) |
| PROCREBOOT | Is set to 1 by the server in case the DAQ Process requires a reboot after re-configuration. Otherwise the value is 0. | INTEGER |
| PROCSTATUSTIME | The timestamp indicates, when the Process status has been changed the last time | TIMESTAMP(6) |
| PROCSTATUSDESC | The status description of the Process, which is similar to the value description of the Process StatusTag | VARCHAR(300) |
| PROCPIK | A unique Process communication key that gets generated during the handshake phase.<br>Every update message coming from the DAQ will contain this Process PIK in the message header. | INTEGER |
| PROCLOCALCONFIG | This field will be set by the server. <br>The following two states are possible:<br> 'N' - The DAQ process requested the configuration by the server<br> 'Y' - The DAQ process is running with a local configuration | VARCHAR(1) |





# The EQUIPMENT table

| Columns Name | Description | Type | Not NULL? |
| ------------ | ----------- | ---- | --------- |
| EQID | Unique Equipment identification key | INTEGER | x |
| EQNAME | Unique Equipment name | VARCHAR(60) | x |
| EQDESC | Equipment description | VARCHAR(100) |
| EQHANDLERCLASS | The Java Equipment handler Class name.<br>That one is passed to the DAQ Process which will create an instance through reflection by passing the Equipment address string (see EQADDRESS). | VARCHAR(100) | x |
| EQADDRESS | A free text field for passing additional parameters needed for connecting the Equipment handler class with the real equipment. It is up to the EquipmentMessageHandler implementation how this string has to look like.<br>Here an example from the JECMessageHandler:<br> `plc_name=APIMMD26;Protocol=SiemensISO;Time_sync=Jec;Port=102;S_tsap=TCP-1;D_tsap=TCP-1;Alive_handler_period=5000;Dp_slave_address=4,5;` | VARCHAR(900) |
| EQSTATE_TAGID | ID of Equipment StatusTag | INTEGER | x |
| EQALIVE_TAGID | ID of the Equipment AliveTag | INTEGER |
| EQALIVEINTERVAL | Alive interval (heartbeat) in milliseconds. | INTEGER |
| EQ_PROCID | The Process ID to which the Equipment is attached.<br>NNt used in case of a Sub-Equipment. | INTEGER |
| EQCOMMFAULT_TAGID | ID of the Equipment CommFaultTag | INTEGER |
| EQ_PARENT_ID | In case of a Sub-Equipment it contains the ID of the parent Equipment.<br>Please note, that Sub-Equipments do not have an EQ_PROCID and neither a EQADDRESS specified. | INTEGER |
| EQSTATE | The current Equipment status set by the server, which can also be retrieved from the corresponding StatusTag.<br>Possible status are:<br> DOWN, RUNNING | VARCHAR(20) |
| EQSTATUSTIME | The timestamp indicates, when the Equipment status has been changed the last time | TIMESTAMP(6) |
| EQSTATUSDESC |The status description of the Equipment, which is similar to the value description of the Equipment StatusTag |VARCHAR(300) |





# The ALARM table

Please read also [Alarms Overview](/overview/alarms) for more information about for instance the triplet concept of Fault Family / Fault Member / Fault Code

| Columns Name | Description |Type | Not NULL? |
| ------------ | ----------- |---- | --------- |
| ALARMID | Unique Alarm identification key | INTEGER | x |
| ALARM_TAGID | ID of the DATATAG to which this alarm is attached | INTEGER |
| ALARMFFAMILY | The alarm Fault family | VARCHAR(64) | x |
| ALARMFMEMBER | The alarm Fault member | VARCHAR(64) | x |
| ALARMFCODE | The alarm Fault code | INTEGER | x |
| ALARMSTATE | Either 1 (=Active) or 0 (=Terminate) | VARCHAR(10) |
| ALARMTIME | The time when the alarm was activated or terminated | VARCHAR(6) |
| ALARMINFO | A free text field that is used by C2MON to store additional alarm information, that is:<br> '[T]' : Indicates that the associated Tag is in TEST mode<br> '[?]' : Indicates that the associated Tag has bad quality (invalid) e.g. due to an Equipment communication failure| VARCHAR(100) |
| ALARMCONDITION | Configuration for the AlarmCondition class that is specified as XML.<br>Below you see examples for `ValueAlarmCondition` and  `RangeAlarmCondition`, which specify when an alarm shall be set to ACTIVE.<br>Please note, that only one definition will can be set per alarm:<br>`<AlarmCondition class="cern.c2mon.server.common.alarm.ValueAlarmCondition">`<br>&nbsp;&nbsp;&nbsp;&nbsp;`<alarm-value type="Boolean">true</alarm-value>`<br>`</AlarmCondition>`<br><br>`<AlarmCondition class="cern.c2mon.server.common.alarm.ValueAlarmCondition">`<br>&nbsp;&nbsp;&nbsp;&nbsp;`<alarm-value type="String">DOWN</alarm-value>`<br>`</AlarmCondition>`<br><br>`<AlarmCondition class="cern.c2mon.server.common.alarm.ValueAlarmCondition">`<br>&nbsp;&nbsp;&nbsp;&nbsp;`<alarm-value type="Integer">1</alarm-value>`<br><br>`</AlarmCondition>`<br>`<AlarmCondition class="cern.c2mon.server.common.alarm.RangeAlarmCondition">`<br>&nbsp;&nbsp;&nbsp;&nbsp;`<max-value type="Float">5.0</max-value>`<br>`</AlarmCondition>`<br><br>`<AlarmCondition class="cern.c2mon.server.common.alarm.RangeAlarmCondition">`<br>&nbsp;&nbsp;&nbsp;&nbsp;`<min-value type="Integer">10</max-value>`<br>`</AlarmCondition>`<br>| VARCHAR(500) |
| ALA_PUBLISHED | _Please note, that this field is only relevant, if the C2MON system has an alarm publication module implemented._<br>Flag indicating, whether the computed alarm has already been published through a dedicated publication module. Possible values are:<br> 0 - Alarm has not yet been sent<br> 1 - Alarm is published | INTEGER |
| ALA_PUB_STATE | _Please note, that this field is only relevant, if the C2MON system has an alarm publication module implemented._<br>The last state that was published. See also ALARMSTATE | VARCHAR(10) |
| ALA_PUB_TIME | _Please note, that this field is only relevant, if the C2MON system has an alarm publication module implemented._<br> The time of the last alarm publication | TIMESTAMP(6) |
| ALA_PUB_INFO | _Please note, that this field is only relevant, if the C2MON system has an alarm publication module implemented._<br>The last alarm information being published. See also ALARMINFO |VARCHAR(100) |
| ALARMMETADATA | The metadata of the alarm, stored as JSON string | VARCHAR(4000) |
| ALARMOSCILLATION | Either 1 (=Oscillating) or 0 (=Not oscillating) | INTEGER |






[oracle-cache-schema]: https://gitlab.cern.ch/c2mon/c2mon/blob/master/c2mon-server/c2mon-server-cachedbaccess/src/main/resources/sql/cache-schema-oracle.sql
[mysql-cache-schema]: https://gitlab.cern.ch/c2mon/c2mon/blob/master/c2mon-server/c2mon-server-cachedbaccess/src/main/resources/sql/cache-schema-mysql.sql
[oracle-history-schema]: https://gitlab.cern.ch/c2mon/c2mon/blob/master/c2mon-server/c2mon-server-history/src/main/resources/sql/history-schema-oracle.sql
[mysql-history-schema]: https://gitlab.cern.ch/c2mon/c2mon/blob/master/c2mon-server/c2mon-server-history/src/main/resources/sql/history-schema-mysql.sql
