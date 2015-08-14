#!/bin/bash
#!/bin/bash
cd deploy

MAIN=cern.c2mon.publisher.rdaAlarms.SubscribeSourceUnfiltered
DEV=../../c2mon-publisher-rda-alarms/build

PARAMS="\
-Dlog4j.configuration=file:log4j.properties \
-XX:+UnlockCommercialFeatures -XX:+FlightRecorder \
-Dc2mon.client.conf.url=file:client.properties \
-Dprovider.properties=file:provider.properties \
-Dmgt.jmx.port=17017 \
-Dspring.profiles.active=PROD \
-Dapp.version=0.2 \
-Dapp.name=DmnRdaAlarmsPub \
-Dcmw.rda3.transport.server.multiThreadedPublisher=true \
-Doracle.net.tns_admin=/etc"

exec -a pub-alarm java ${PARAMS} -cp ".:${DEV}/classes:lib/*" ${MAIN} $1 $2 $3 $4 $5 $6 $7 $8 $9

