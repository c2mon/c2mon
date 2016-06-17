docker run --rm --name daq-dip -ti --net=host -e "DIPNS=dipnsgpn1,dipnsgpn2" -e "C2MON_PORT_61616_TCP=tcp://localhost:61616" docker.cern.ch/c2mon-project/daq-dip bin/C2MON-DAQ-STARTUP.jvm -f $@
