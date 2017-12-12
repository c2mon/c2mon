# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

All issues referenced in parentheses can be consulted under [CERN GitLab](https://gitlab.cern.ch/c2mon/c2mon/issues).
For more details on a given release, please check also the [Milestone planning](https://gitlab.cern.ch/c2mon/c2mon/milestones?state=all).

## [Unreleased]
### Added

### Changed

### Fixed


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


[Unreleased]: https://gitlab.cern.ch/c2mon/c2mon/milestones/22
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

