# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

All issues referenced in parentheses can be consulted under [CERN JIRA](https://its.cern.ch/jira/projects/CM).
For more details on a given release, please check also the [version planning](https://its.cern.ch/jira/projects/CM/versions).

## [Unreleased]
### Added
- Server: Added support for Elasticsearch 6.x
- Server: Integrated Elasticsearch REST client

### Changed
- Client API: Added possibility to define credentials for making queries to Elasticsearch (CM-232)
- Documentation is now build with Jekyll instead of MkDocs

### Fixed
- Client API: Fixed Elasticsearch tests for C2MON client API (CM-230)

### Deprecated
- Client API: Renamed some of the ElasticsearchService methods. Old method names still exist but are marked as deprecated.


## 1.9.2 - 2019-08-28
### Changed
- Server:  Increase sleeping time to 3 sec before trying to reconnect ES, if it is not yet in yellow or green state (CM-215)

### Fixed
- Server: Fixed a dependency problem introduced with [elasticsearch-cluster-runner](https://github.com/codelibs/elasticsearch-cluster-runner) that prevented to start Elasticsearch (CM-215)

## 1.9.1 - 2019-08-28
**Broken build**

## [1.9.0] - 2019-08-09

**C2MON depends now on Spring 5!** Please upgrade accordingly your client applications and make sure you use the same Spring version as specified in the [Maven POM](pom.xml).

We also migrated our issue tracking system from Gitlab to [JIRA](https://its.cern.ch/jira/projects/CM). Furthermore, users from outside of CERN have now the possiblity to report issues directly on [GitHub](https://github.com/c2mon/c2mon/issues).

### Changed
- Upgrade of Spring dependencies to 5.1.2.RELEASE and for Spring Boot to 2.1.0.RELEASE (#228)

### Fixed
- Client API: Since last ActiveMQ upgrade clients were not anymore correctly informed about JMS broker disconnection (#273)
- Client API: Fixed a JMS resource bug related to the configuration queue where consumers were never deleted (#266)
- Deployment: Updated C2MON Docker image and Kubernetes files to match the versions in production (#259, #269, #271)
- Server: Fixed ES Unit tests and switched to [elasticsearch-cluster-runner](https://github.com/codelibs/elasticsearch-cluster-runner) to start the embedded Elasticsearch instance (CM-215)
- Server: A major refactoring of the Alarm oscillation feature was done since is it was not correctly working when changing the default properties. Many addition tests were written to make sure that the logic is finally water-proof (CM-219).


## 1.8.45 - 2019-06-07
### Fixed
- Server: Prevent `NullPointerException` when cloning Metadata with `null` values (#270)


## 1.8.44 - 2019-06-06
### Fixed
- Server: Remove Terracotta bulk load calls as batch loading of data tag into the cache fails randomly due to this (#268)
- Server: When reloading the cache from the backup database the internal alarm oscillation fields are wrongly initialized (#267)


## [1.8.43] - 2019-06-04
### Added
- Server: Added a warning log message that informs every minute how many alarms are currently oscillating. In case of no oscillating alarms, only a debug message is logged.
- Deployment: Added Grafana to C2MON's Kubernetes configuration as well as predefined sample dashboards (!219)
- Client API: Added source timestamp to client alarm object (#237)
- Elasticsearch: Added alarm ids to ES tag document (#265)


## [1.8.42] - 2019-05-28

### Fixed
- Alarm oscillation feature introduced as alpha version in [1.8.40] passed now all internal CERN stress test criterias and can be considered as stable (#145)
- Corrected a misreferenced property name in `c2mon-server.properties` file (#261)


## [1.8.41] - 2019-05-09

### Added
- Client API: Added method to clear local command cache (22abcb48)
- Client API: Added methods to `SupervisionService` for retrieving process and (sub-)equipment information from local cache (#238)
- Server, Client API: Added source timestamp to alarm object (#237)
- Server: Added dedicated alarm logger to keep track of alarm updates in a separate log file (#229)

### Changed
- Cient API: Set ActiveMQ prefetch limit to 100 to avoid slow consumer problem (#240)
- Updated C2MON Docker examples for Kubernetes (#205)

### Fixed
- Server: Fixed `clone()` method of `AlarmCacheObject` (5bf7a831)
- Server: Fixed alarm state mapping to `Boolean` for database and source timestamp indexing for ElasticSearch (1627915c)
- Server: Fixed bugs in alarm oscillation control logic introduced with last release (#233, #243, #246, #248, #145)
- Server: Fixed problem of alarm history which logged active alarms several times with the same timestamp (#205)
- Client API: ActiveMQ prefetch limit is now set in the code to 100 to avoid slow consumer problems (#240)

### Removed
- Removed dependency to `javax.el` validator annotations (#249)

## [1.8.40] - 2019-02-01
**Please note**,  upgrading to this version requires changes in the database schema. This was necessary in order to implement the alarm oscillation control feature (see #145).

### Added
- Server: Added generic oscillation control for alarms (#145)

### Changed
- DAQ: Improved logging for multi-equipment DAQs by giving threads the name of the corresponding equipment (#224)

### Fixed
- Server: Fixed ElasticSearch mapping for Boolean tags on numeric value field, which was always set to 1 (#223)
- Server: Resolved bug preventing C2MON server to start without Elasticsearch logging (#218)


## [1.8.39] - 2018-10-16
### Added
- Added string data type support to JEC protocol address class (#214)
- Enabled JMX support for embedded JMS broker
- Added openshift template draft for Docker deployment

### Changed
- Client: Search by tag name in DAQ is now case insensitive
- Time-to-live of JMS messages is reduced to one day (and 10 minutes for alive messages) (#220)

### Fixed
- Server: Fixed value mapping problem in Elasticsearch plugin (#222)
- Documentation: Typo correction and fixes in example test code of DAQ module testing guide

## 1.8.38 - 2018-05-08
### Fixed
- Server: Fixed additional write lock problem related to rule configuration in cluster environment, which appeared during integration testing of [1.8.36]

## 1.8.37 - BROKEN!
Broken release!

## [1.8.36] - 2018-05-07
### Changed
- Documentation: Updated [Database](https://c2mon.web.cern.ch/c2mon/docs/user-guide/server/database/) section and added [About](https://c2mon.web.cern.ch/c2mon/docs/about/CHANGELOG/) chapter

### Fixed
- Client API: Fixed NPE in `ControlTagConfigurationManagerImpl`, which occured when sending a configuration update on a control tag
- Server: Removed write lock in `RuleTagCacheImpl`, which could cause in rare cases a deadlock during a rule configuration in cluster mode (only!)
- Server: Supressing invalid warning message during Process Commfault value persistence to Elasticsearch (#209)


## [1.8.35] - 2018-04-26
### Changed
- Documentation: Upgrade to MkDocs 0.17.3 and major refactoring of C2MON documentation structure (!182)

### Fixed
- Server, Elasticsearch: Added missing argument for re-index tag config operation (#194)
- Fixed equipment configuration bug by adding 'cache put' command (!184)


## [1.8.34] - 2018-04-13
### Fixed
-  Server: Fixed bug in DAQ process XML document generation related to deleted Equipments (#207)


## [1.8.33] - 2018-03-19
### Changed
- DAQ: Small improvements in logback.xml

### Fixed
- Server: Fixed minor issue in MySQL schema (#193)
- Client: Small code changes to fix problems with history player (c2mon-client-ext-history) package (#200)
- Client: Fix NPE in isAuthorized() method of CommandServiceImpl


## [1.8.32] - 2018-03-09
### Added
- Server: Added serial version UID to Metadata cache object in order to stay compliant with C2MON version 1.8.30

### Changed
- Documentation: Updated Elasticsearch documentation (#198)

### Fixed
- DAQ: Fixed precision error in value deadband filtering (#195)


## [1.8.31] - 2018-02-26
### Added
- Elasticsearch: Storing alarm configuration to Tag config document in c2mon-config index (#194)

### Changed
- Tidy temporary file handling in tests (#189)
- Upgraded ActiveMQ to version 5.15.2, which was tested by CERN and passed all stress tests

### Fixed
- Fixed problem for local protyping that DAQ process could not recover from a C2MON server restart with embedded ActiveMQ broker (#191)
- Server: Fixing MySQL support (#193)

### Removed
-  Removed activemq-openwire-legacy runtime dependency


## [1.8.30] - 2018-01-17
### Added
- Server: Documented all properties and default variables in the c2mon-server.properties file of the distribution tarball (#132)
- Server: Introduced a common property for setting database url for all connections (backup db, history db): `c2mon.server.jdbc.url`
- Documentation: Added introduction to 'DAQ API' section
- Added JMX call to persist all Tags to tag-config index (#185)

### Changed
- Documentation: Updated getting-started chapter (#133)
- Server: Reduced DAQ value updater executor thread pool size to 100 instead of 250

### Fixed
- Fixed dependency resolution problems when building against Maven Central
- Server: When a tag update is received and C2MON detects that this tag is not registered in the Elasticsearch config index it will now add the docuement instead of throwing an error (#178)
- Fix bug for Equipment deletion from Process, related to clustered cache
- Client API: Fixed NPE in `TagService.findByName(String)` when tag name was not found (#187)

### Removed
- Server: Removed Process name pattern matcher check constraint to allow any process name format (#186)


## [1.8.29] - 2017-12-20
### Changed
- Server: Updated MYSQL schema scripts for version 5.7 (#177)
- Deployment is now done against Nexus to make all artifacts available from outside of CERN (#182)
- Server: Avoids now sending CommandTag configuration updates to DAQ, if not required (#183)

### Fixed
- Client API: Tags were not invalidated after a process or equipment went down (#181). This bug got introduced by Client API refactoring (#53)
- DAQ: Fixed problem with initialitation of `FreshnessMonitor` for `GenericMessageHandlerTest` unit test class
- Server: Fixed issue that at creation of the Elasticsearch config index the mapping was not always taken into account (#170)

### Removed
- Server: Removed Oracle dependency (#179)


## 1.8.28 - 2017-12-12
### Changed
- Client API: Removed all unrequired dependencies from c2mon-client-core pom.xml, in particular Elasticsearch (#164)

## [1.8.27] - 2017-11-17
### Changed
- Server: Increased the size of field ALARMFAMILY in ALARM database table (#176)

### Fixed
- CERN specific: Converting alarm triplet to upper case string, which is the requested standard (c2mon-daq-alarmsource#5)


## [1.8.26] - 2017-10-20
### Fixed
- Fix thread lock in parallel configuration (#171)
- Fixed problem in creation of the Elasticsearch config index where the mapping was not always taken into account (#170)


## [1.8.25] - 2017-09-27
### Added
- Server: Added property to disable Elasticsearch module, if not required (#167)
- Added logic for SonarQube code analysis to Maven configuration. For now the SonarQube server is only accesible from inside of CERN. We will change this at a later stage.

### Changed
- Elasticsearch: Updated Elasticsearch version from 2.4.1 to 5.6.0 (#164)

### Fixed
- Server: Index prefix property is now correctly taken into acount for creating the tag-config index


## [1.8.24] - 2017-09-05
### Fixed
- Server: Resolved possible deadlock in tag quality object (#166)
- Server: Added again proper exception handling for (sub-)equipment metadata in elasticsearch data conversion (#165), which got accidentially removed by merge for issue #88


## [1.8.23] - 2017-09-01
### Added
- Client API: First version of tag subscription by metadata (#88)
- Client API: Improved Alarm configuration builder by adding more methods (#151)

### Fixed
- Server: Resolved `NullPointerException` during configuration when adding metadata with `null` value (#163)
- Server: Configuration requests containing updates for non-existing entities will report a warning but not fail anymore
- Documentation: Fixed dead link in the database section of the server documentation (#162)


## 1.8.22 - BROKEN!
Broken release!


## 1.8.21 - BROKEN!
Broken release!


## 1.8.20 - BROKEN!
Broken release!


## 1.8.19 - BROKEN!
Broken release!


## [1.8.18] - 2017-08-16
### Fixed
- Fixed problem of alarm metadata that were wrongly stored the alarm cache object (#160). Bug got introduced with #89


## [1.8.17] - 2017-07-31
### Added
- Documentation: Added information about how to configure the server database

### Fixed
- Added maven-wrapper.jar to fix compilation via `./mvnw` (#121)
- DAQ: Fixed runtime exception when creating a new Sub-Equipment (#146)
- Client API: Tag did not contain alarm value updates after refactoring of `TagImpl` (#159)


## [1.8.16] - 2017-05-10
### Added
- DAQ: Possibility to start/stop DAQ module from inside the JVM (!130). Many thanks to Martin Heck from TU Berlin!

### Fixed
- Client API: Fixed bug in `TagImpl` class, which returned the tag description instead of the value description (#147)
- Server: Runtime Exception when updating Metadata (#148)
- Docker: Fix configuration issues for ElasticSearch in Docker image (!148)


## [1.8.15] - 2017-03-31
### Added
- Runtime support for HTTP via ActiveMQ (#113)

### Changed
- Updated logback dependency from v1.1.7 to v1.1.8 due to a serious runtime bug

### Fixed
- Server: Fixed NPE problem when value of metadata is `null` (#139)
- Server: Fixed frequent configuration timeout when waiting for DAQ response (#140)
- Server: Prevent exception when trying to delete not existing entity (#141)


## [1.8.14] - 2017-03-14
### Added
- ActiveMQ openwire legacy support to DAQ Core and Client API

### Changed
- Updated Elasticsearch version from 2.3.3 to 2.4.1  (#138)

### Fixed
- DAQ: Fixed JMS initialization bug appearing in 'double' mode (#137)


## 1.8.13 - 2017-03-10
This patch contains bug fixes for the DAQ layer.

### Fixed
- DAQ: Corrected property variables in `C2MON-DAQ-STARTUP.jvm` script
- DAQ: Loading of local configuration file from default location `$DAQ_HOME/conf/local/<process-name>.xml`

### Removed
- DAQ: Removed obsolete `conf/log4j.properties` file from tarball


## [1.8.12] - 2017-03-10
### Added
- Added a fully documented list of all default variables to `conf/c2mon-daq.properties` file for DAQ tarball (#132)

### Fixed
-  DAQ Core: Fixed a possible exception on DAQ side when sending configuration update for an address field (#136)


## 1.8.11 - 2017-02-23
### Fixed
- Fixed critical lifecycle bug introduced with version [1.8.9] that prevented DAQs to startup


## 1.8.10 - 2017-02-23
**This version contains a critical bug that prevents DAQs to startup. Please use instead v1.8.11 or higher.**

### Fixed
- Added missing search attributes on Ehcache cluster configuration
- Elasticseach module: Fixed NPE problem on metadata conversion


## [1.8.9] - 2017-02-22
**This version contains a critical bug that prevents DAQs to startup. Please use instead v1.8.11 or higher.**

### Fixed
- Elasticsearch module: Exception is now caught in case of an error during the tag conversion for ES (#128)
- Fixed problem of double quotes in backup database for string values (#129)
- Server: Lifecycle problems and start/stop behaviour (!117)

### Removed
- Removed obsolete `video` package in `c2mon-shared-client`


## [1.8.8] - 2017-01-31
### Added
- Client layer: Data type information is now also available, if the value is `null` (uninitialised) (#52)

### Changed
- Client layer: Code refactoring of ClientDataTagImpl (#53)
- Major refactoring of Elasticsearch module (see [merge request #109](https://gitlab.cern.ch/c2mon/c2mon/merge_requests/109))
- Updating metadata is now additive, not destructive (#89)

### Fixed
- Server exception after device deletion (#119)

### Removed
- Client layer: Removed deprecated classes and interfaces (#51)


## [1.8.7] - 2017-01-19
### Fixed
- Alarm metadata were not sent through the alarm topic (#125)


## [1.8.6] - 2016-12-20
### Added
- Client layer: Introduce Domain variable for JMS variable simplification (#73)
- DAQ layer: Improved startup failing message in case PIK is rejected (#95)

### Changed
- Upgrade Spring dependencies to v4.3.4 and Spring Boot to v1.4.2 (#117)

### Fixed
- NPE in client configuration report (#123)
- Metadata in Elasticsearch indices are set to "not_analyzed" (#115)

### Removed
- Remove of EquipmentLogger concept from DAQ Core (#56)


[Unreleased]: https://its.cern.ch/jira/projects/CM/versions
[1.9.0]: https://its.cern.ch/jira/projects/CM/versions/31979
[1.8.43]: https://gitlab.cern.ch/c2mon/c2mon/milestones/36
[1.8.42]: https://gitlab.cern.ch/c2mon/c2mon/milestones/35
[1.8.41]: https://gitlab.cern.ch/c2mon/c2mon/milestones/34
[1.8.40]: https://gitlab.cern.ch/c2mon/c2mon/milestones/31
[1.8.39]: https://gitlab.cern.ch/c2mon/c2mon/milestones/30
[1.8.36]: https://gitlab.cern.ch/c2mon/c2mon/milestones/29
[1.8.35]: https://gitlab.cern.ch/c2mon/c2mon/milestones/28
[1.8.34]: https://gitlab.cern.ch/c2mon/c2mon/milestones/27
[1.8.33]: https://gitlab.cern.ch/c2mon/c2mon/milestones/26
[1.8.32]: https://gitlab.cern.ch/c2mon/c2mon/milestones/25
[1.8.31]: https://gitlab.cern.ch/c2mon/c2mon/milestones/24
[1.8.30]: https://gitlab.cern.ch/c2mon/c2mon/milestones/23
[1.8.29]: https://gitlab.cern.ch/c2mon/c2mon/milestones/22
[1.8.27]: https://gitlab.cern.ch/c2mon/c2mon/milestones/19
[1.8.26]: https://gitlab.cern.ch/c2mon/c2mon/milestones/21
[1.8.25]: https://gitlab.cern.ch/c2mon/c2mon/milestones/20
[1.8.24]: https://gitlab.cern.ch/c2mon/c2mon/milestones/18
[1.8.23]: https://gitlab.cern.ch/c2mon/c2mon/milestones/17
[1.8.18]: https://gitlab.cern.ch/c2mon/c2mon/milestones/16
[1.8.17]: https://gitlab.cern.ch/c2mon/c2mon/milestones/15
[1.8.16]: https://gitlab.cern.ch/c2mon/c2mon/milestones/14
[1.8.15]: https://gitlab.cern.ch/c2mon/c2mon/milestones/13
[1.8.14]: https://gitlab.cern.ch/c2mon/c2mon/milestones/12
[1.8.12]: https://gitlab.cern.ch/c2mon/c2mon/milestones/11
[1.8.9]: https://gitlab.cern.ch/c2mon/c2mon/milestones/10
[1.8.8]: https://gitlab.cern.ch/c2mon/c2mon/milestones/9
[1.8.7]: https://gitlab.cern.ch/c2mon/c2mon/milestones/8
[1.8.6]: https://gitlab.cern.ch/c2mon/c2mon/milestones/7
