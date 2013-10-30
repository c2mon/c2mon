#!/bin/sh

CP=build/bin
if test "$1" = "--test"
then
	CP=../tim2-daq-cmwadmin-mock/build/bin:build/bin
fi

for i in lib/*.jar
do
	CP=${CP}:$i
done

VMARGS="-Xms1g -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=30000"
ARGS="-daqConf /user/mbuttner/eclipse/workspace_diamon/tim2-daq-cmwadmin/daq.conf"
LOG_CONF="-log4j /user/mbuttner/eclipse/workspace_diamon/tim2-daq-cmwadmin/log4j_test.xml"
CONF="-c /user/mbuttner/eclipse/workspace_diamon/tim2-daq-cmwadmin/cmwadmin_test_conf.xml"


echo "--------------------------------------------------------"
echo ${CP}
echo "--------------------------------------------------------"
echo ${ARGS}
echo "--------------------------------------------------------"
echo ${LOG_CONF}
echo "--------------------------------------------------------"
echo ${CONF}
echo "--------------------------------------------------------"

java -cp ${CP} ${VMARGS} cern.tim.driver.common.startup.DaqStartup ${ARGS} ${LOG_CONF} ${CONF} -processName XXX -testMode -noDeadband


