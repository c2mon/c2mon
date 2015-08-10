#!/bin/bash
#!/bin/bash
cd deploy

MAIN=cern.c2mon.publisher.rdaAlarm.RdaAlarmsMain

PARAMS="\
-Dlog4j.configuration=file:conf/log4j.properties \
-XX:+UnlockCommercialFeatures -XX:+FlightRecorder \
-Dc2mon.client.conf.url=file:conf/client.properties \
-Dprovider.properties=file:provider.properties \
-Dmgt.jmx.port=17017 \
-Dspring.profiles.active=PROD \
-Dapp.version=0.2 \
-Dapp.name=DmnRdaAlarmsPub \
-Dcmw.rda3.transport.server.multiThreadedPublisher=true \
-Doracle.net.tns_admin=/etc"

exec -a pub-alarm java ${PARAMS} -cp ".:../../c2mon-publisher-rda-alarms/build/classes:lib/*" ${MAIN}

