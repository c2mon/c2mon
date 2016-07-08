#!/usr/bin/env bash
docker run --rm --name c2mon -ti -p 0.0.0.0:1099:1099 -p 0.0.0.0:9001:9001 -p 0.0.0.0:61616:61616 -p 0.0.0.0:9200:9200 docker.cern.ch/c2mon-project/server
