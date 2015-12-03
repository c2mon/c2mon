#!/bin/bash
#!/bin/bash
cd deploy

MAIN=cern.c2mon.publisher.mobicall.MobicallAlarmsMain

PARAMS="\
-Dlog4j.configuration=file:log4j.properties \
-Dc2mon.client.conf.url=file:client.properties \
-Doracle.net.tns_admin=/etc"

echo ${PARAMS}
exec -a alarm-publisher java ${PARAMS} -cp ".:lib/*" ${MAIN}

