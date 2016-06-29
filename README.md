# C2MON : CERN Control and Monitoring Platform
[![build status](https://gitlab.cern.ch/c2mon/c2mon/badges/master/build.svg)](https://gitlab.cern.ch/c2mon/c2mon/commits/master)

The CERN Control and Monitoring Platform (C2MON) is a heterogeneous data acquisition and monitoring framework. It contains many useful features
such as historical metric persistence and browsing, command execution and alerting. It can be suitable for building many different types
of monitoring and control system.

## Documentation
See the current [Javadoc][] and [reference docs][].

## Issue Tracking
Please report issues on GitLab via the [issue tracker][].

## Building from Source
C2MON uses a [Maven][]-based build system. In the instructions
below, `./mvnw` is invoked from the root of the source tree and serves as
a cross-platform, self-contained bootstrap mechanism for the build.

### Prerequisites

[Git][] and [JDK 8 update 20 or later][JDK8 build]

Be sure that your `JAVA_HOME` environment variable points to the `jdk1.8.0` folder
extracted from the JDK download.

### Check out sources
`git clone git@github.com:c2mon/c2mon.git`

### Compile and test; build all jars, distribution zips, and docs
`./mvnw build`

## Contributing
[Pull requests][] are welcome; see the [contributor guidelines][] for details.

## License
C2MON is released under the [GPLv3 License][].

[Javadoc]: https://c2mon.web.cern.ch/c2mon/javadoc/
[reference docs]: https://c2mon.web.cern.ch/c2mon/docs/build/
[issue tracker]: https://gitlab.cern.ch/c2mon/c2mon/issues
[Maven]: http://maven.apache.org
[Git]: http://help.github.com/set-up-git-redirect
[JDK8 build]: http://www.oracle.com/technetwork/java/javase/downloads
[Pull requests]: http://help.github.com/send-pull-requests
[contributor guidelines]: https://gitlab.cern.ch/c2mon/c2mon/blob/master/CONTRIBUTING.md
[GPLv3 License]: https://www.gnu.org/licenses/gpl-3.0.en.html
