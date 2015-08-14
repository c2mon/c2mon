#!/bin/bash
#!/bin/bash
cd deploy

MAIN=cern.c2mon.publisher.rdaAlarms.RdaAlarmsMain

PARAMS="\
-Dspring.profiles.active=PROD-DB \
-Dlog4j.configuration=file:log4j.properties \
-XX:+UnlockCommercialFeatures -XX:+FlightRecorder \
-Dc2mon.client.conf.url=file:client.properties \
-Dprovider.properties=file:provider.properties \
-Dmgt.jmx.port=17017 \
-Dapp.version=0.2 \
-Dapp.name=DmnRdaAlarmsPub \
-Dcmw.rda3.transport.server.multiThreadedPublisher=true \
-Doracle.net.tns_admin=/etc"

echo ${PARAMS}
exec -a alarm-publisher java ${PARAMS} -cp ".:../../c2mon-publisher-rda-alarms/build/classes:lib/*" ${MAIN}

