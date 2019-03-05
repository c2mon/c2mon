# DAQ API - General Overview

Introduction to C2MON Data Acquisition (DAQ) API, used by all existing DAQ modules for the server communication.

---

C2MON was initially developed at CERN to acquire data from physical infrastructural equipment, such as PLCs or OPC servers.
As such, the word "Equipment" occurs frequently within the API.
However, this does not mean that you cannot write a DAQ for retrieving data from other sources, such as host monitoring processes, middleware protocols, or any other type of software services or physical hardware.

## Anatomy of a C2MON DAQ module

A C2MON DAQ module is a Java program responsible for connecting to and reading data from specific sources (software, hardware) and publishing data to the
C2MON server tier. It is also optionally in charge of data filtering and executing commands on sources.

A DAQ knows how to:

- Retrieve the centrally managed configuration from C2MON with the given DAQ Process name
- Connect to the source(s) (the `Equipment`)
- Bind to and read individual metrics from the source and publish them as Tags
- Execute commands on the source
- Send heartbeats and status flags about the connection status and quality of groups of metrics
- Filter the incoming raw data
- Add/Update/Delete Tags at runtime

Historically, the types of physical sources that C2MON was designed for were dynamic in nature, meaning that they did not have a well-defined set of output
metrics - rather, the set of metrics changed frequently. For example, CERN uses many types of [PLC](https://en.wikipedia.org/wiki/Programmable_logic_controller).
These PLCs have many generic input/output channels, which are in turn connected to industrial equipment, and are frequently re-wired.

For this reason, the design choice was made to pre-define the set of metrics that a particular DAQ would be responsible for acquiring, and to store this
configuration in the C2MON server itself. The DAQ API was designed to be dynamic in the fact that it would retrieve its pre-defined configuration at
startup and use it to read from the source. It would also be optionally capable of re-configuring its set of metrics at runtime.

## Data validation

Before the data is send to the server the DAQ core is applying several data validation checks, which can affect the Tag quality. The following checks are applied:

| Data validation check            | Description                                                                                                             |
|----------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| Source timestamp in the future?  | A source timestamp which is more than 5 minutes ahead of the DAQ system time will cause a Tag invalidation.             |
| Correct data value type?         | The Tag quality is set to invalid, if the value received cannot be cast into the configured tag value type.             |
| Value in defined range?          | If the value range is for instance configured to be between 0 and 100 but 223 is received, the Tag will be invalidated. |
| Data freshness (optional)        | The time in which a new value is expected. If this time expire the the quality of the tag changes to STALE. This is configurable on the `DataTagAddress` |


## Data filtering

Guaranteeing the availability of a monitoring system and its applications involves protecting it from overload during data avalanches. C2MON archives this through data filtering on the DAQ level without loosing any important events.

Beside filtering out redundant values (default behaviour) the DAQ layer provides a dynamic-filtering option for noise rejection. This is configurable per DAQ process and imposes time deadbands on individual Tags detected as feeding too much data to the C2MON server cluster. Various strategies on measuring the data throughput are provided.

Moreover, the following static filters can be configured on individual tags. This provides in addition to the dynamic time-deadband filtering a very fine grained filtering control.

| Static Filtering (configured per Tag) | Description |
|---------------------------------------|-------------|
| Time deadband  | A Tag configured with a static time deadband will only send updates in a given frequency. If the source is publishing values in a higher rate, the DAQ core will filter them out. Only the latest value is kept and then sent to the server after the time deadband expiration.  The behavior is similar to the dynamic time deadband, except that the static one is configured per Tag and always active. |
| Value deadband (or "change band") | Only values that represent significant changes from previous values are transmitted or stored.   Otherwise, the data values are assumed to be unchanged. This is similar to the deadband idea, but the deadband moves centered around the most recent data. The intent is to minimize the amount of data to be transmitted or stored. |

However, when setting the filter parameters, a balance must be achieved between noise rejection vs. speed of response.


### What happens to the filtered data?

The filtered data are kept in a rotating log file for debugging purposes. In addition, it is also possible to define a filter message queue to which the filtered values are sent. This can be useful for instance to generate statistics on the incoming raw data stream or to keep track of the full data history.


## Existing DAQ modules
A few DAQ modules are provided on [GitHub](https://github.com/c2mon?utf8=%E2%9C%93&q=c2mon-daq), which are actively maintained by the CERN C2MON team. We plan to add some more in the near future and hope the community will do the same!


### DAQ module configuration

To configure a new a DAQ Processes or just Tags on a given DAQ module make use of the [C2MON Configuration API](/user-guide/client-api/configuration).

Every tarball contains a [c2mon-daq.properties](https://github.com/c2mon/c2mon/blob/master/c2mon-daq/distribution/src/main/resources/tar/conf/c2mon-daq.properties) file in the `/conf` directory for changing the default setup to your needs. The properties listed in the file can just as well be set as Java system properties with the `-D` option.
