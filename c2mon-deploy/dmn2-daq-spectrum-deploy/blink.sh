#!/bin/bash
while true
do
	./localcmd.sh CLR CFC-CCR-ELFE1 65545 cs-srv-45
	sleep 30
	./localcmd.sh SET CFC-CCR-ELFE1 65545 cs-srv-45
	sleep 30
done

