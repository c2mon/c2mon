#!/bin/bash

set -e

cd /${basedir}

# Add daq process as command if at least one argument was provided to the C2MON DAQ entrypoint
# with a "-" flag (otherwise we assume another command like /bin/bash etc...
if [ "${1:0:1}" = '-' ]; then
	set -- "bin/C2MON-DAQ-STARTUP.jvm" "$@"
fi

# Run as user "c2mon" if the command is "logstash"
#if [ "$1" = 'bin/C2MON-DAQ-STARTUP.jvm' ]; then
#	set -- gosu c2mon "$@"
#fi

echo "Running $@"
exec "$@"
