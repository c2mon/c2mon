docker run --rm --name c2mon --link eslog:eslog -ti -p 0.0.0.0:1099:1099 -p 0.0.0.0:9001:9001 -p 0.0.0.0:61616:61616 docker.cern.ch/c2mon-project/server
