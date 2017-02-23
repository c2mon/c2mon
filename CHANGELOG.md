# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

All issues referenced in parentheses can be consulted under [CERN GitLab](https://gitlab.cern.ch/c2mon/c2mon/issues).
For more details on a given release, please check also the [Milestone planning](https://gitlab.cern.ch/c2mon/c2mon/milestones?state=all).

## [Unreleased]
### Added

### Changed

### Fixed

## [1.8.10] - 2017-02-23
### Fixed
- Added missing search attributes on Ehcache cluster configuration
- Elasticseach module: Fixed NPE problem on metadata conversion


## [1.8.9] - 2017-02-22
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


[Unreleased]: https://gitlab.cern.ch/c2mon/c2mon/milestones/12
[1.8.10]: https://gitlab.cern.ch/c2mon/c2mon/milestones/11
[1.8.9]: https://gitlab.cern.ch/c2mon/c2mon/milestones/10
[1.8.8]: https://gitlab.cern.ch/c2mon/c2mon/milestones/9
[1.8.7]: https://gitlab.cern.ch/c2mon/c2mon/milestones/8
[1.8.6]: https://gitlab.cern.ch/c2mon/c2mon/milestones/7

