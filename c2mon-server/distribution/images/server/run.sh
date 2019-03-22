#!/usr/bin/env bash
docker run --rm --name c2mon -ti -p 1099:1099 -p 9001:9001 -p 61616:61616 -p 9200:9200 cernoss/c2mon
